package com.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends transactional emails via the Resend REST API (HTTPS/443).
 * Replaces SMTP which is blocked on Render free tier (port 587 timeout).
 * Requires RESEND_API_KEY env var on Render.
 * Falls back to console logging if no key is configured (local dev).
 */
@Service
public class ResendEmailClient {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailClient.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromAddress;

    public boolean sendPlainText(String to, String subject, String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[ResendEmailClient] No RESEND_API_KEY — logging locally.");
            log.info("[EMAIL-LOCAL] To: {} | Subject: {} | Body: {}", to, subject, text);
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("from", fromAddress);
            ArrayNode toArray = mapper.createArrayNode();
            toArray.add(to);
            payload.set("to", toArray);
            payload.put("subject", subject);
            payload.put("text", text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[ResendEmailClient] Sent to {} status {}", to, response.statusCode());
                return true;
            }
            log.warn("[ResendEmailClient] Resend error {} for {}: {}", response.statusCode(), to, response.body());
            return false;
        } catch (Exception ex) {
            log.warn("[ResendEmailClient] Failed to send to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    public boolean sendHtml(String to, String subject, String html) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[ResendEmailClient] No RESEND_API_KEY — logging locally.");
            log.info("[EMAIL-LOCAL] To: {} | Subject: {}", to, subject);
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("from", fromAddress);
            ArrayNode toArray = mapper.createArrayNode();
            toArray.add(to);
            payload.set("to", toArray);
            payload.put("subject", subject);
            payload.put("html", html);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[ResendEmailClient] HTML sent to {} status {}", to, response.statusCode());
                return true;
            }
            log.warn("[ResendEmailClient] Resend error {} for {}: {}", response.statusCode(), to, response.body());
            return false;
        } catch (Exception ex) {
            log.warn("[ResendEmailClient] Failed HTML send to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
