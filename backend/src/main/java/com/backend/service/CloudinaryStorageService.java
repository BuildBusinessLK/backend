package com.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class CloudinaryStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            ObjectMapper objectMapper) {
        this.cloudName = cloudName != null ? cloudName.trim().toLowerCase() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiSecret = apiSecret != null ? apiSecret.trim() : "";
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public boolean isConfigured() {
        return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    /**
     * Uploads an image (base64 Data URL or raw bytes) to Cloudinary and returns the permanent HTTPS URL.
     */
    public Optional<String> uploadImage(String imageDataOrUrl) {
        if (!isConfigured()) {
            log.info("Cloudinary is not configured. Skipping cloud image upload.");
            return Optional.empty();
        }

        if (imageDataOrUrl == null || imageDataOrUrl.isBlank()) {
            return Optional.empty();
        }

        // If it's already a public HTTPS URL (e.g. already uploaded), return it
        if (imageDataOrUrl.startsWith("https://res.cloudinary.com/")) {
            return Optional.of(imageDataOrUrl);
        }

        try {
            long timestamp = Instant.now().getEpochSecond();
            String stringToSign = "timestamp=" + timestamp + apiSecret;
            String signature = sha1Hex(stringToSign);

            String formBody = "file=" + URLEncoder.encode(imageDataOrUrl, StandardCharsets.UTF_8)
                    + "&timestamp=" + timestamp
                    + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                    + "&signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.warn("Cloudinary upload failed (HTTP {}): {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String secureUrl = root.path("secure_url").asText(null);
            if (secureUrl != null && !secureUrl.isBlank()) {
                log.info("Image successfully uploaded to Cloudinary: {}", secureUrl);
                return Optional.of(secureUrl);
            }

            return Optional.empty();
        } catch (Exception ex) {
            log.error("Error uploading image to Cloudinary: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private String sha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
