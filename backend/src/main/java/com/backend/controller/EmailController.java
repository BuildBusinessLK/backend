package com.backend.controller;

import com.backend.dto.EmailContent;
import com.backend.dto.EmailGenerationRequest;
import com.backend.dto.EmailGenerationResponse;
import com.backend.dto.EmailRecipientGroup;
import com.backend.dto.SendEmailRequest;
import com.backend.service.EmailService;
import com.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class EmailController {
    
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;
    
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Generates an email based on the provided idea.
     * POST /generate-email
     * Request: { "idea": "Welcome new customers to our service" }
     * Response: { "email": { "subject": "...", "body": "..." } }
     */
    @PostMapping("/generate-email")
    public ResponseEntity<EmailGenerationResponse> generateEmail(
            @Valid @RequestBody EmailGenerationRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails principal) {
        log.info("Received email generation request with idea: {}", request.getIdea());
        
        try {
            EmailContent email = emailService.generateEmail(request, principal == null ? null : principal.getId());
            EmailGenerationResponse response = new EmailGenerationResponse(email);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Sends emails to specified recipients.
     * POST /send-email
     */
    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails principal) {
        log.info("Received send email request for {} recipient groups", request.getGroupIds().size());
        
        try {
            int sentCount = emailService.sendEmail(request, principal.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Emails processed successfully");
            response.put("count", String.valueOf(sentCount));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending emails: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to send emails: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/email-recipient-groups")
    public ResponseEntity<List<EmailRecipientGroup>> recipientGroups(
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(emailService.getRecipientGroups(principal.getId()));
    }
}
