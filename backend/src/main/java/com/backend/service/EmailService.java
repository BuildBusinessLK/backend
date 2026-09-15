package com.backend.service;

import com.backend.dto.EmailContent;
import com.backend.dto.EmailGenerationRequest;
import com.backend.dto.SendEmailRequest;
import com.backend.repository.BusinessRepository;
import com.backend.repository.BusinessProfileRepository;
import com.backend.repository.UserProfileRepository;
import com.backend.repository.UserRepository;
import com.backend.entity.Business;
import com.backend.entity.BusinessProfile;
import com.backend.entity.User;
import com.backend.entity.UserProfile;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.backend.dto.EmailRecipientGroup;
import com.backend.user.UserStatus;

import com.backend.repository.CustomerRepository;
import com.backend.entity.Customer;

@Service
public class EmailService {

    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CustomerRepository customerRepository;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final ResendEmailClient resendEmailClient;
    private final String mailHost;
    private final String fromAddress;
    private final AiHordeService aiHordeService;
    private final AiClientService aiClientService;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSender,
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            CustomerRepository customerRepository,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.mail.from:}") String fromAddress,
            ResendEmailClient resendEmailClient,
            AiHordeService aiHordeService,
            AiClientService aiClientService) {
        this.mailSender = mailSender.getIfAvailable();
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.customerRepository = customerRepository;
        this.mailHost = mailHost;
        this.fromAddress = fromAddress;
        this.resendEmailClient = resendEmailClient;
        this.aiHordeService = aiHordeService;
        this.aiClientService = aiClientService;
    }
    
    /**
     * Generates an email based on the provided idea/purpose.
     * If a sessionId is provided, uses the refined brief from the chat conversation.
     */
    /**
     * Generates an email based on the provided idea/purpose.
     * This is a basic implementation that can be extended with AI integration.
     */
    public EmailContent generateEmail(EmailGenerationRequest request, Long userId) {
        log.info("Generating email from idea: {}", request.getIdea());
        String idea = request.getIdea() == null ? "" : request.getIdea().trim();

        populateProfileContext(request, userId);

        EmailContent email = createEmailFromIdea(request, idea);

        log.info("Email generated successfully with subject: {}", email.getSubject());
        return email;
    }

    /**
     * Sends a generated email to each recipient.
     */
    @Transactional(readOnly = true)
    public int sendEmail(SendEmailRequest request, Long senderUserId) {
        Map<String, RecipientGroup> groups = buildRecipientGroups(senderUserId);
        Set<String> recipients = new LinkedHashSet<>();
        for (String groupId : request.getGroupIds()) {
            RecipientGroup group = groups.get(groupId);
            if (group != null) recipients.addAll(group.emails);
        }
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("The selected groups do not currently contain any recipients");
        }

        int sentCount = 0;
        for (String recipient : recipients) {
            if (isMailConfigured()) {
                sendSingleEmail(recipient, request.getSubject(), request.getBody());
            } else {
                logLocalDelivery(recipient, request.getSubject(), request.getBody());
            }
            sentCount++;
        }

        if (isMailConfigured()) {
            log.info("Successfully sent {} email(s) via SMTP", sentCount);
        } else {
            log.warn("SMTP is not configured, so {} email(s) were only logged locally", sentCount);
        }
        return sentCount;
    }

    /**
     * Builds groups at request time, so group membership automatically includes
     * newly registered users as soon as they complete their profile/business data.
     */
    @Transactional(readOnly = true)
    public List<EmailRecipientGroup> getRecipientGroups(Long senderUserId) {
        return buildRecipientGroups(senderUserId).values().stream()
                .map(group -> new EmailRecipientGroup(group.id, group.label, group.emails.size()))
                .toList();
    }

    private Map<String, RecipientGroup> buildRecipientGroups(Long senderUserId) {
        Map<String, RecipientGroup> groups = new LinkedHashMap<>();

        // If the sender has a business with seeded customers, use them instead of the platform-wide fallback.
        Business business = businessRepository.findByOwner_Id(senderUserId).stream().findFirst().orElse(null);
        if (business != null) {
            List<Customer> customers = customerRepository.findByBusiness_Id(business.getId());
            if (!customers.isEmpty()) {
                for (Customer customer : customers) {
                    String email = customer.getEmail();
                    if (isBlank(email)) continue;

                    if (customer.isVip()) {
                        addGroup(groups, "vip", "VIP Customers", email);
                    }
                    if (customer.isNew()) {
                        addGroup(groups, "new", "New Customers", email);
                    }
                    if (customer.isFrequentBuyer()) {
                        addGroup(groups, "frequent", "Frequent Buyers", email);
                    }
                    if (customer.isHighSpending()) {
                        addGroup(groups, "high_spending", "High Spending Customers", email);
                    }
                    if (customer.isInactive()) {
                        addGroup(groups, "inactive", "Inactive Customers", email);
                    }
                    if (customer.isInterestedInDiscounts()) {
                        addGroup(groups, "discounts", "Interested in Discounts", email);
                    }
                    if (customer.isInterestedInNewProducts()) {
                        addGroup(groups, "new_products", "Interested in New Products", email);
                    }
                    if (!isBlank(customer.getProductCategory())) {
                        addGroup(groups, "category:" + normalize(customer.getProductCategory()), "Category: " + customer.getProductCategory(), email);
                    }
                    if (business.getSector() != null) {
                        String sectorLabel = capitalizeWords(business.getSector().name().replace('_', ' '));
                        addGroup(groups, "sector:" + normalize(business.getSector().name()), "Sector: " + sectorLabel, email);
                    }
                    if (!isBlank(customer.getDistrict())) {
                        addGroup(groups, "location:" + normalize(customer.getDistrict()), "Location: " + customer.getDistrict(), email);
                    }
                    int age = customer.getAge();
                    if (age > 0) {
                        String ageGroup = age < 25 ? "Age: Under 25" : (age <= 45 ? "Age: 25 - 45" : "Age: Over 45");
                        addGroup(groups, "age:" + normalize(ageGroup), ageGroup, email);
                    }
                    int freq = customer.getPurchaseFrequency();
                    String freqGroup = freq <= 2 ? "Frequency: Low (1-2 orders)" : (freq <= 5 ? "Frequency: Medium (3-5 orders)" : "Frequency: High (6+ orders)");
                    addGroup(groups, "freq:" + normalize(freqGroup), freqGroup, email);
                }
                return groups;
            }
        }

        Map<Long, UserProfile> profiles = new LinkedHashMap<>();
        for (UserProfile profile : userProfileRepository.findAll()) {
            profiles.put(profile.getUser().getId(), profile);
        }

        for (User user : userRepository.findAll()) {
            if (user.getStatus() != UserStatus.ACTIVE || user.getId().equals(senderUserId) || isBlank(user.getEmail())) continue;
            UserProfile profile = profiles.get(user.getId());
            if (profile != null && !isBlank(profile.getDistrict())) {
                addGroup(groups, "location:" + normalize(profile.getDistrict()), "Location: " + profile.getDistrict(), user.getEmail());
            }
        }

        for (Business b : businessRepository.findAll()) {
            User owner = b.getOwner();
            if (owner == null || owner.getStatus() != UserStatus.ACTIVE || owner.getId().equals(senderUserId) || isBlank(owner.getEmail())) continue;
            if (b.getSector() != null) {
                String sector = b.getSector().name();
                addGroup(groups, "sector:" + normalize(sector), "Sector: " + sector, owner.getEmail());
            }
            businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(profile -> {
                if (!isBlank(profile.getTargetMarket())) {
                    addGroup(groups, "market:" + normalize(profile.getTargetMarket()), "Target market: " + profile.getTargetMarket(), owner.getEmail());
                }
                for (String keyword : descriptionKeywords(profile.getBusinessDescription())) {
                    addGroup(groups, "description:" + keyword, "Business focus: " + keyword, owner.getEmail());
                }
            });
        }
        return groups;
    }

    private void addGroup(Map<String, RecipientGroup> groups, String id, String label, String email) {
        groups.computeIfAbsent(id, ignored -> new RecipientGroup(id, label)).emails.add(email);
    }

    private List<String> descriptionKeywords(String description) {
        if (isBlank(description)) return List.of();
        Set<String> ignored = Set.of("about", "after", "and", "business", "from", "have", "into", "more", "offers", "provides", "that", "their", "they", "this", "with", "your");
        Set<String> words = new LinkedHashSet<>();
        for (String word : description.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() >= 4 && !ignored.contains(word)) words.add(word);
            if (words.size() == 5) break;
        }
        return new ArrayList<>(words);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static class RecipientGroup {
        private final String id;
        private final String label;
        private final Set<String> emails = new LinkedHashSet<>();

        private RecipientGroup(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
    
    /**
     * Creates a professional email based on the provided idea.
     * Can be replaced with AI API calls (e.g., OpenAI, Claude) for more sophisticated generation.
     */
    private EmailContent createEmailFromIdea(EmailGenerationRequest request, String idea) {
        try {
            EmailContent aiEmail = generateEmailWithAI(request);
            if (aiEmail != null && aiEmail.getSubject() != null && aiEmail.getBody() != null) {
                return aiEmail;
            }
        } catch (Exception ex) {
            log.warn("AI email generation failed, falling back to templates: {}", ex.getMessage());
        }

        String subject = generateSubject(idea);
        String body = generateBodyFromContext(request, idea);

        return new EmailContent(subject, body);
    }

    /**
     * Calls OpenAI Chat Completions to generate a professional subject and body.
     * Returns null on failure so caller can fall back.
     */
    private void populateProfileContext(EmailGenerationRequest request, Long userId) {
        if (userId == null) {
            setEmailDefaults(request);
            return;
        }

        userProfileRepository.findByUser_Id(userId).ifPresent(profile -> {
            if (isBlank(request.getUserName())) request.setUserName(profile.getFullName());
        });

        // Only use businesses owned by the signed-in user. This prevents profile data
        // belonging to another user from ever being included in an AI prompt.
        Business business = businessRepository.findByOwner_Id(userId).stream().findFirst().orElse(null);
        if (business != null) {
            if (isBlank(request.getCompanyName())) request.setCompanyName(business.getBusinessName());
            if (isBlank(request.getIndustry()) && business.getSector() != null) {
                request.setIndustry(business.getSector().name());
            }
            businessProfileRepository.findByBusiness_Id(business.getId()).ifPresent(profile -> {
                if (isBlank(request.getTargetAudience())) request.setTargetAudience(profile.getTargetMarket());
                request.setBusinessDescription(firstPresent(request.getBusinessDescription(), profile.getBusinessDescription()));
                request.setMarketingGoals(firstPresent(request.getMarketingGoals(), profile.getMarketingGoals()));
            });
        }
        setEmailDefaults(request);
    }

    private void setEmailDefaults(EmailGenerationRequest request) {
        if (isBlank(request.getUserName())) request.setUserName("Your Name");
        if (isBlank(request.getCompanyName())) request.setCompanyName("your business");
        if (isBlank(request.getIndustry())) request.setIndustry("Not provided");
        if (isBlank(request.getTargetAudience())) request.setTargetAudience("customers and prospects");
        if (isBlank(request.getTone())) request.setTone("professional and approachable");
        if (isBlank(request.getSignature())) request.setSignature(request.getUserName() + "\n" + request.getCompanyName());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String firstPresent(String preferred, String fallback) {
        return isBlank(preferred) ? fallback : preferred;
    }

    private EmailContent generateEmailWithAI(EmailGenerationRequest request) throws Exception {
        try {
            com.backend.dto.email.EmailGenerateRequest aiReq = new com.backend.dto.email.EmailGenerateRequest();
            aiReq.setGoal("GENERAL_ANNOUNCEMENT");
            aiReq.setCompanyName(request.getCompanyName());
            aiReq.setUserName(request.getUserName());
            aiReq.setSector(request.getIndustry());
            aiReq.setTargetAudience(request.getTargetAudience());
            aiReq.setTone(request.getTone());
            aiReq.setKeyOffer(request.getIdea());
            aiReq.setIdea(request.getIdea());

            com.backend.dto.email.EmailGenerateResponse resp = aiClientService.generateEmail(aiReq);
            if (resp != null && resp.getSubject() != null && !resp.getSubject().isBlank() && resp.getBody() != null && !resp.getBody().isBlank()) {
                return new EmailContent(resp.getSubject(), resp.getBody());
            }
        } catch (Exception ex) {
            log.warn("FastAPI email generation call encountered exception, trying fallback: {}", ex.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();

String systemPrompt =
"""
You are a senior business email writer.

Generate a concise, suitable, professional email using only the supplied idea
and user business profile. Do not invent offers, prices, dates, links, or facts.

Return ONLY JSON:

{
  "subject":"...",
  "body":"..."
}
""";
String userPrompt =
"""
Business Information

User Name: %s
Company Name: %s
Industry: %s
Target Audience: %s
Business Description: %s
Marketing Goals: %s
Tone: %s

Email Idea:
%s

Signature:
%s

Instructions:

1. Create a professional subject.
2. Mention company name naturally.
3. Match the requested tone.
4. Write 3-5 professional paragraphs.
5. Include a call to action.
6. End with the signature.

Return JSON only.
"""
.formatted(
        request.getUserName(),
        request.getCompanyName(),
        request.getIndustry(),
        request.getTargetAudience(),
        request.getBusinessDescription(),
        request.getMarketingGoals(),
        request.getTone(),
        request.getIdea(),
        request.getSignature()
);

        String prompt = systemPrompt + "\n\n" + userPrompt;
        String content = aiHordeService.generateText(prompt);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI Horde returned empty response");
        }
        content = content.trim();
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }

        // Expecting JSON string like {"subject":"...","body":"..."}
        try {
            JsonNode parsed = mapper.readTree(content);
            String subject = parsed.path("subject").asText(null);
            String body = parsed.path("body").asText(null);
            if (subject != null && body != null) {
                return new EmailContent(subject, body);
            }
        } catch (Exception ex) {
            // If the model didn't return strict JSON, try simple heuristics
            String[] parts = content.split("\n\n", 2);
            String subj = parts.length > 0 ? parts[0].trim() : null;
            String bod = parts.length > 1 ? parts[1].trim() : content.trim();
            if (subj != null && !subj.isBlank() && bod != null && !bod.isBlank()) {
                return new EmailContent(subj, bod);
            }
        }

        return null;
    }
    
    /**
     * Generates a subject line based on the idea.
     * Uses heuristic-based patterns when AI is unavailable.
     */
    private String generateSubject(String idea) {
        // Improved heuristic-based subject generation: concise, professional, no emojis
        if (idea == null || idea.isBlank()) {
            return "A Message from Our Team";
        }

        String lower = idea.toLowerCase();
        if (lower.contains("welcome")) {
            return "Welcome to Our Service";
        } else if (lower.contains("promotional") || lower.contains("sale") || lower.contains("discount")) {
            return "Special Offer — Limited Time";
        } else if (lower.contains("product") || lower.contains("launch")) {
            return "Introducing Our New Product";
        } else if (lower.contains("update") || lower.contains("changes") || lower.contains("notice")) {
            return "Important Update Regarding Your Account";
        }

        // Fallback: create a concise subject from the idea (max ~8 words)
        String[] words = idea.trim().split("\\s+");
        StringBuilder subj = new StringBuilder();
        int maxWords = Math.min(8, words.length);
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) subj.append(' ');
            subj.append(words[i].replaceAll("[^a-zA-Z0-9\\-]", ""));
        }
        String result = subj.toString().trim();
        if (result.length() == 0) {
            return "A Message from Our Team";
        }
        // Capitalize first letters
        return capitalizeWords(result);
    }
    
    /**
     * Generates an email body based on the idea.
     * TODO: Replace with AI-powered body generation.
     */
    private String generateBodyFromContext(EmailGenerationRequest request, String idea) {
        if (idea == null) idea = "";
        String trimmed = idea.trim();
        String companyName = (request.getCompanyName() != null && !request.getCompanyName().isBlank())
                ? request.getCompanyName()
                : "our team";
        String tone = (request.getTone() != null && !request.getTone().isBlank())
                ? request.getTone()
                : "professional";
        String audience = (request.getTargetAudience() != null && !request.getTargetAudience().isBlank())
                ? request.getTargetAudience()
                : "our valued clients";
        String userName = (request.getUserName() != null && !request.getUserName().isBlank())
                ? request.getUserName()
                : "Dear Customer";

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(userName).append(",").append("\n\n");

        if (!trimmed.isEmpty()) {
            body.append("I hope you are well. I am reaching out regarding ")
                    .append(trimmed)
                    .append(". This message is written in a ")
                    .append(tone)
                    .append(" tone to make sure it feels clear, relevant, and useful for ")
                    .append(audience)
                    .append(".\n\n");
        } else {
            body.append("I hope you are well. I wanted to share a helpful update from ")
                    .append(companyName)
                    .append(" that may be of interest to you.\n\n");
        }

        body.append("At ")
                .append(companyName)
                .append(", we focus on delivering value with care and consistency. This message is designed to highlight the benefit of our offering and support the next step in your journey.\n\n");
        body.append("Please take a moment to review this update and let us know if you would like to discuss it further. We would be happy to answer any questions and help you move forward with confidence.\n\n");
        body.append("Thank you for your time and continued interest.\n\n");
        body.append("Kind regards,\n");
        body.append(companyName);

        return body.toString();
    }
    
    /**
     * Utility method to capitalize words in a string.
     */
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase())
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }

    private void sendSingleEmail(String recipient, String subject, String body) {
        String resolvedFrom = (fromAddress != null && !fromAddress.isBlank() && !fromAddress.contains("resend.dev"))
                ? fromAddress.trim()
                : "havindufonseka@gmail.com";
        String htmlText = buildHtmlCard(subject, body, resolvedFrom);

        // Try Resend first (works on Render free tier via HTTPS/443)
        if (resendEmailClient.isConfigured()) {
            boolean ok = resendEmailClient.sendHtml(recipient, subject, htmlText);
            if (ok) return;
        }

        // Fallback: SMTP
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(resolvedFrom, "BuildBusinessLK Platform");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, htmlText);
            mailSender.send(message);
            log.info("Sent rich HTML email to {} with subject {}", recipient, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException("Failed to create email message for recipient " + recipient + ": " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send email to " + recipient + ": " + ex.getMessage(), ex);
        }
    }

    private String buildHtmlCard(String subject, String body, String fromEmail) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = body.split("\n");
        boolean inList = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (inList) {
                    formattedContent.append("</ul>\n");
                    inList = false;
                }
                continue;
            }

            if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                if (!inList) {
                    formattedContent.append("<ul style=\"margin: 12px 0; padding-left: 20px; color: #334155;\">\n");
                    inList = true;
                }
                String itemText = line.substring(1).trim();
                formattedContent.append("<li style=\"margin-bottom: 6px; font-size: 14px; line-height: 1.6;\">")
                        .append(escapeHtml(itemText))
                        .append("</li>\n");
            } else {
                if (inList) {
                    formattedContent.append("</ul>\n");
                    inList = false;
                }
                formattedContent.append("<p style=\"margin: 0 0 14px 0; font-size: 14px; line-height: 1.7; color: #1e293b;\">")
                        .append(escapeHtml(line))
                        .append("</p>\n");
            }
        }
        if (inList) {
            formattedContent.append("</ul>\n");
        }

        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
    .container { max-width: 620px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 14px rgba(0,0,0,0.06); }
    .header { background: linear-gradient(135deg, #0284c7, #0ea5e9); padding: 28px 24px; text-align: center; color: #ffffff; }
    .header h1 { margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.02em; color: #ffffff; }
    .header p { margin: 6px 0 0 0; font-size: 13px; opacity: 0.92; color: #e0f2fe; }
    .content { padding: 28px 24px; background: #ffffff; }
    .cta-box { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 18px; margin: 20px 0 10px 0; text-align: center; }
    .cta-btn { display: inline-block; padding: 12px 24px; background: #0284c7; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 14px; }
    .footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>%s</h1>
      <p>BuildBusinessLK &bull; Commercial Outreach Platform</p>
    </div>
    <div class="content">
      %s
      <div class="cta-box">
        <p style="margin: 0 0 10px 0; font-size: 13px; color: #0369a1; font-weight: 700;">Have questions or need more details?</p>
        <a href="mailto:%s?subject=Re: %s" class="cta-btn">Reply to this Message</a>
      </div>
    </div>
    <div class="footer">
      Sent via BuildBusinessLK &bull; Sri Lankan MSME Agribusiness Platform<br>
      Authorized recipients: havindufonseka@gmail.com &bull; havinduhesara21@gmail.com
    </div>
  </div>
</body>
</html>
""".formatted(
            escapeHtml(subject),
            formattedContent.toString(),
            escapeHtml(fromEmail),
            escapeHtml(subject)
        );
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void logLocalDelivery(String recipient, String subject, String body) {
        log.info("=== LOCAL EMAIL DELIVERY (SMTP NOT CONFIGURED) ===");
        log.info("To: {}", recipient);
        log.info("From: {}", fromAddress == null || fromAddress.isBlank() ? "noreply@localhost" : fromAddress);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("===================================================");
    }

    private boolean isMailConfigured() {
        return resendEmailClient.isConfigured()
            || (mailSender != null
                && mailHost != null
                && !mailHost.isBlank()
                && fromAddress != null
                && !fromAddress.isBlank());
    }
}
