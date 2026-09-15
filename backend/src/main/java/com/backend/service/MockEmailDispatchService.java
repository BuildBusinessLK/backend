package com.backend.service;

import com.backend.domain.CampaignStatus;
import com.backend.domain.DeliveryStatus;
import com.backend.dto.email.CampaignSimulationResponse;
import com.backend.entity.EmailCampaign;
import com.backend.entity.EmailCampaignRecipient;
import com.backend.repository.EmailCampaignRecipientRepository;
import com.backend.repository.EmailCampaignRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MockEmailDispatchService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailDispatchService.class);

    // Strictly isolated authorized test mailboxes as per production safety requirement
    public static final List<String> AUTHORIZED_TEST_EMAILS = List.of(
            "havindufonseka@gmail.com",
            "havinduhesara21@gmail.com"
    );

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignRecipientRepository campaignRecipientRepository;
    private final ResendEmailClient resendEmailClient;
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String mailHost;

    public MockEmailDispatchService(
            EmailCampaignRepository campaignRepository,
            EmailCampaignRecipientRepository campaignRecipientRepository,
            ResendEmailClient resendEmailClient,
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${spring.mail.host:}") String mailHost) {
        this.campaignRepository = campaignRepository;
        this.campaignRecipientRepository = campaignRecipientRepository;
        this.resendEmailClient = resendEmailClient;
        this.mailSender = mailSender.getIfAvailable();
        this.fromAddress = fromAddress;
        this.mailHost = mailHost;
    }

    /**
     * Executes a realistic stochastic campaign simulation.
     * External parties are NEVER contacted.
     */
    @Transactional
    public CampaignSimulationResponse simulateCampaign(EmailCampaign campaign) {
        List<EmailCampaignRecipient> recipients = campaignRecipientRepository.findByCampaign_Id(campaign.getId());
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Campaign has no recipients attached to simulate.");
        }

        campaign.setStatus(CampaignStatus.SIMULATING);
        campaignRepository.save(campaign);

        Random rand = new Random();
        int delivered = 0;
        int opened = 0;
        int clicked = 0;
        int failed = 0;

        LocalDateTime now = LocalDateTime.now();

        for (EmailCampaignRecipient recipient : recipients) {
            // ~97% delivered probability
            boolean isDelivered = rand.nextDouble() < 0.97;
            if (isDelivered) {
                delivered++;
                // ~50-60% open probability
                boolean isOpened = rand.nextDouble() < 0.55;
                if (isOpened) {
                    opened++;
                    LocalDateTime openTime = now.minusMinutes(rand.nextInt(180) + 5);
                    recipient.setOpenedAt(openTime);
                    // ~40-45% click of opens
                    boolean isClicked = rand.nextDouble() < 0.42;
                    if (isClicked) {
                        clicked++;
                        recipient.setClickedAt(openTime.plusMinutes(rand.nextInt(25) + 1));
                        recipient.setStatus(DeliveryStatus.CLICKED);
                    } else {
                        recipient.setStatus(DeliveryStatus.OPENED);
                    }
                } else {
                    recipient.setStatus(DeliveryStatus.DELIVERED);
                }
            } else {
                failed++;
                recipient.setStatus(DeliveryStatus.FAILED);
                recipient.setErrorMessage("Mailbox unavailable or simulated rate limit");
            }
        }

        campaignRecipientRepository.saveAll(recipients);

        campaign.setTotalRecipients(recipients.size());
        campaign.setDeliveredCount(delivered);
        campaign.setOpenedCount(opened);
        campaign.setClickedCount(clicked);
        campaign.setFailedCount(failed);
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaign.setSentAt(now);
        campaign.setMock(true);

        campaignRepository.save(campaign);

        log.info("SAFETY SIMULATION: Completed mock campaign '{}' (#{}). Total: {}, Delivered: {}, Opened: {}, Clicked: {}, Failed: {}",
                campaign.getTitle(), campaign.getId(), recipients.size(), delivered, opened, clicked, failed);

        CampaignSimulationResponse response = new CampaignSimulationResponse();
        response.setCampaignId(campaign.getId());
        response.setStatus(campaign.getStatus());
        response.setTotalRecipients(campaign.getTotalRecipients());
        response.setDeliveredCount(delivered);
        response.setOpenedCount(opened);
        response.setClickedCount(clicked);
        response.setFailedCount(failed);

        double total = recipients.size();
        response.setDeliveryRate(total > 0 ? Math.round((delivered / total) * 1000.0) / 10.0 : 0.0);
        response.setOpenRate(delivered > 0 ? Math.round(((double) opened / delivered) * 1000.0) / 10.0 : 0.0);
        response.setClickRate(opened > 0 ? Math.round(((double) clicked / opened) * 1000.0) / 10.0 : 0.0);
        response.setMessage("Mock campaign simulated successfully. No real external companies were emailed.");

        return response;
    }

    /**
     * Sends a real test email copy ONLY to authorized developer / tester mailboxes.
     */
    public int sendTestEmail(EmailCampaign campaign, String overrideTestEmail) {
        List<String> targetEmails = new ArrayList<>();
        if (overrideTestEmail != null && !overrideTestEmail.isBlank()) {
            String sanitized = overrideTestEmail.trim().toLowerCase(Locale.ROOT);
            if (AUTHORIZED_TEST_EMAILS.contains(sanitized)) {
                targetEmails.add(sanitized);
            } else {
                log.warn("SAFETY OVERRIDE: Requested email '{}' is not in authorized list. Routing to {} instead.",
                        sanitized, AUTHORIZED_TEST_EMAILS);
                targetEmails.addAll(AUTHORIZED_TEST_EMAILS);
            }
        } else {
            targetEmails.addAll(AUTHORIZED_TEST_EMAILS);
        }

        String subject = "[TEST CAMPAIGN] " + campaign.getSubject();
        String resolvedFrom = (fromAddress != null && !fromAddress.isBlank() && !fromAddress.contains("resend.dev"))
                ? fromAddress.trim()
                : "havindufonseka@gmail.com";

        String htmlBody = buildHtmlEmail(campaign, campaign.getBody(), resolvedFrom);
        String plainBody = buildPlainEmail(campaign, campaign.getBody());

        int sent = 0;
        for (String testTo : targetEmails) {
            if (isMailConfigured()) {
                sendSingleEmail(testTo, subject, plainBody, htmlBody, resolvedFrom);
                sent++;
            } else {
                logLocalTestDelivery(testTo, subject, plainBody);
                sent++;
            }
        }

        log.info("SAFETY AUDIT: Test email for campaign '{}' dispatched to {} authorized test inbox(es).",
                campaign.getTitle(), sent);
        return sent;
    }

    private void sendSingleEmail(String recipient, String subject, String plainText, String htmlText, String from) {
        boolean resendSuccess = false;
        if (resendEmailClient != null && resendEmailClient.isConfigured()) {
            resendSuccess = resendEmailClient.sendHtml(recipient, subject, htmlText);
            if (resendSuccess) {
                log.info("Sent HTML email via Resend to {}", recipient);
                return;
            }
            log.info("Resend dispatch was not completed; attempting SMTP fallback to {}", recipient);
        }

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                helper.setFrom(from, "BuildBusinessLK Platform");
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(plainText, htmlText); // Multipart: plain text + HTML
                mailSender.send(message);
                log.info("Successfully sent HTML email via SMTP to {}", recipient);
            } catch (MessagingException ex) {
                log.error("Failed to construct test email to {}: {}", recipient, ex.getMessage());
            } catch (Exception ex) {
                log.error("Failed to deliver test email via SMTP to {}: {}", recipient, ex.getMessage());
            }
        }
    }

    public String buildHtmlEmail(EmailCampaign campaign, String body, String fromEmail) {
        String goalLabel = switch (campaign.getGoal()) {
            case WHOLESALE_PITCH -> "Wholesale Supply Pitch";
            case EXPORTER_SAMPLE_OFFER -> "Exporter Sample Offer";
            case RETAIL_DISCOUNT -> "Retail Promotional Discount";
            case HARVEST_ANNOUNCEMENT -> "Fresh Harvest Announcement";
            default -> "Commercial Announcement";
        };

        String sectorLabel = campaign.getTargetSector() != null
                ? campaign.getTargetSector().name().replace('_', ' ')
                : "Agribusiness";

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
    .test-banner { background: #f0fdf4; border-bottom: 1px solid #bbf7d0; padding: 10px 20px; font-size: 12px; color: #15803d; font-weight: 700; text-align: center; }
    .header { background: linear-gradient(135deg, #0284c7, #0ea5e9); padding: 28px 24px; text-align: center; color: #ffffff; }
    .header h1 { margin: 0; font-size: 20px; font-weight: 800; letter-spacing: -0.02em; color: #ffffff; }
    .header p { margin: 6px 0 0 0; font-size: 13px; opacity: 0.92; color: #e0f2fe; }
    .meta-bar { background: #f8fafc; border-bottom: 1px solid #e2e8f0; padding: 12px 24px; font-size: 12px; }
    .badge { display: inline-block; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
    .badge-goal { background: #e0f2fe; color: #0369a1; }
    .badge-sector { background: #f1f5f9; color: #475569; margin-left: 6px; }
    .content { padding: 28px 24px; background: #ffffff; }
    .cta-box { background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 18px; margin: 20px 0 10px 0; text-align: center; }
    .cta-btn { display: inline-block; padding: 12px 24px; background: #0284c7; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 14px; }
    .footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; }
  </style>
</head>
<body>
  <div class="container">
    <div class="test-banner">
      🛡️ Test Campaign Preview &bull; Dispatched to Authorized Tester Inboxes
    </div>
    <div class="header">
      <h1>%s</h1>
      <p>BuildBusinessLK Commercial Outreach Studio</p>
    </div>
    <div class="meta-bar">
      <span class="badge badge-goal">%s</span>
      <span class="badge badge-sector">%s</span>
      <span style="color: #64748b; font-size: 11px; float: right; margin-top: 3px;">Audience: %d recipients</span>
    </div>
    <div class="content">
      %s
      <div class="cta-box">
        <p style="margin: 0 0 10px 0; font-size: 13px; color: #0369a1; font-weight: 700;">Interested in this commercial batch?</p>
        <a href="mailto:%s?subject=Re: %s" class="cta-btn">Reply &amp; Request Sample Pack</a>
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
            escapeHtml(campaign.getTitle()),
            escapeHtml(goalLabel),
            escapeHtml(sectorLabel),
            campaign.getTotalRecipients(),
            formattedContent.toString(),
            escapeHtml(fromEmail),
            escapeHtml(campaign.getSubject())
        );
    }

    private String buildPlainEmail(EmailCampaign campaign, String body) {
        String goalLabel = switch (campaign.getGoal()) {
            case WHOLESALE_PITCH -> "Wholesale Supply Pitch";
            case EXPORTER_SAMPLE_OFFER -> "Exporter Sample Offer";
            case RETAIL_DISCOUNT -> "Retail Promotional Discount";
            case HARVEST_ANNOUNCEMENT -> "Fresh Harvest Announcement";
            default -> "Commercial Announcement";
        };

        return """
[BuildBusinessLK Test Campaign Preview]
Campaign: %s
Objective: %s
Audience: %d recipients

%s

---
Sent via BuildBusinessLK Platform
Authorized recipients: havindufonseka@gmail.com, havinduhesara21@gmail.com
""".formatted(campaign.getTitle(), goalLabel, campaign.getTotalRecipients(), body);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void logLocalTestDelivery(String recipient, String subject, String body) {
        log.info("=== LOCAL SAFE TEST EMAIL DELIVERY (SMTP NOT CONFIGURED) ===");
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("============================================================");
    }

    private boolean isMailConfigured() {
        return (resendEmailClient != null && resendEmailClient.isConfigured())
                || (mailSender != null && mailHost != null && !mailHost.isBlank());
    }
}
