package com.backend.service;

import com.backend.dto.EmailContent;
import com.backend.dto.EmailGenerationRequest;
import com.backend.dto.SendEmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String mailHost;
    private final String fromAddress;
    private final String openaiApiKey;
    private final String openaiModel;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${app.openai.apiKey:}") String openaiApiKey,
            @Value("${app.openai.model:gpt-4o-mini}") String openaiModel) {
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.fromAddress = fromAddress;
        this.openaiApiKey = openaiApiKey == null ? "" : openaiApiKey.trim();
        this.openaiModel = openaiModel == null ? "gpt-4o-mini" : openaiModel.trim();
    }
    
    /**
     * Generates an email based on the provided idea/purpose.
     * If a sessionId is provided, uses the refined brief from the chat conversation.
     */
    /**
     * Generates an email based on the provided idea/purpose.
     * This is a basic implementation that can be extended with AI integration.
     */
    public EmailContent generateEmail(EmailGenerationRequest request) {
        log.info("Generating email from idea: {}", request.getIdea());
        String idea = request.getIdea().trim();
        
        // Basic email generation logic
        EmailContent email = createEmailFromIdea(idea);
        
        log.info("Email generated successfully with subject: {}", email.getSubject());
        return email;
    }

    /**
     * Sends a generated email to each recipient.
     */
    public int sendEmail(SendEmailRequest request) {
        int sentCount = 0;
        for (String recipient : request.getRecipients()) {
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
     * Creates a professional email based on the provided idea.
     * Can be replaced with AI API calls (e.g., OpenAI, Claude) for more sophisticated generation.
     */
    private EmailContent createEmailFromIdea(String idea) {
        // If OpenAI key configured, attempt AI-powered generation (professional tone)
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                EmailContent aiEmail = generateEmailWithAI(idea);
                if (aiEmail != null && aiEmail.getSubject() != null && aiEmail.getBody() != null) {
                    return aiEmail;
                }
            } catch (Exception ex) {
                log.warn("AI email generation failed, falling back to templates: {}", ex.getMessage());
            }
        }

        // Default templates and logic (fallback)
        String subject = generateSubject(idea);
        String body = generateBody(idea);

        return new EmailContent(subject, body);
    }

    /**
     * Calls OpenAI Chat Completions to generate a professional subject and body.
     * Returns null on failure so caller can fall back.
     */
    private EmailContent generateEmailWithAI(String idea) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String systemPrompt = "You are an expert copywriter who writes concise, professional, and clear business emails. Respond in JSON with exactly two fields: \"subject\" and \"body\". Do not include any extra commentary.";
        String userPrompt = "Write a professional, polite email based on this idea: \"" + idea.replaceAll("\"", "\\\"") + "\". Keep subject concise (6-10 words) and body 3-6 short paragraphs. Use formal salutations and a clear call-to-action when relevant.";

        // Build chat request body
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", openaiModel);
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode sys = mapper.createObjectNode(); sys.put("role", "system"); sys.put("content", systemPrompt);
        ObjectNode usr = mapper.createObjectNode(); usr.put("role", "user"); usr.put("content", userPrompt);
        messages.add(sys); messages.add(usr);
        payload.set("messages", messages);
        payload.put("temperature", 0.25);
        payload.put("max_tokens", 600);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("OpenAI API returned status " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            throw new IllegalStateException("No choices in OpenAI response");
        }

        String content = choices.get(0).path("message").path("content").asText();

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
     * TODO: Replace with AI-powered subject generation.
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
    private String generateBody(String idea) {
        if (idea == null) idea = "";
        String trimmed = idea.trim();

        StringBuilder body = new StringBuilder();
        body.append("Hello,").append("\n\n");

        // Intro paragraph: what this email is about
        if (!trimmed.isEmpty()) {
            body.append("I hope you're well. I'm reaching out regarding: ")
                .append(trimmed)
                .append(". ")
                .append("Below is a brief overview and the next steps.")
                .append("\n\n");
        } else {
            body.append("I hope you're well. I wanted to share an update from our team that may be of interest to you.")
                .append("\n\n");
        }

        // Value paragraph: benefits or details
        body.append("What this means for you:\n");
        body.append("- Clear benefits or key points related to the message.\n");
        body.append("- How this can help or affect the recipient.\n\n");

        // Call-to-action paragraph
        body.append("Next steps:\n");
        body.append("Please reply to this email if you'd like more details or to arrange a quick call to discuss further.\n\n");

        // Closing
        body.append("Kind regards,\n");
        body.append("The Team");

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
        try {
            String resolvedFrom = fromAddress == null ? "" : fromAddress.trim();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(resolvedFrom);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
            log.info("Sent email to {} with subject {}", recipient, subject);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to create email message for recipient " + recipient + ": " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to send email to " + recipient + ": " + ex.getMessage(), ex);
        }
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
        return mailSender != null
            && mailHost != null
            && !mailHost.isBlank()
            && fromAddress != null
            && !fromAddress.isBlank();
    }
}
