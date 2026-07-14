package com.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class AdVisualService {
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final HttpClient httpClient;

    public AdVisualService(
            @Value("${app.pollinations.api-key:}") String apiKey,
            @Value("${app.pollinations.image-model:flux}") String model,
            @Value("${app.pollinations.base-url:https://gen.pollinations.ai}") String baseUrl) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "flux" : model.trim();
        this.baseUrl = (baseUrl == null || baseUrl.isBlank() ? "https://gen.pollinations.ai" : baseUrl.trim())
                .replaceAll("/+$", "");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    /**
     * Generates an image through Pollinations and returns the existing data-URL contract expected by the frontend.
     */
    public String generate(String prompt, String size) throws Exception {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Image generation is not configured. Set POLLINATIONS_API_KEY in backend/backend/.env.");
        }

        int[] dimensions = parseDimensions(size);
        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8).replace("+", "%20");
        String imageUrl = baseUrl + "/image/" + encodedPrompt
                + "?model=" + URLEncoder.encode(model, StandardCharsets.UTF_8)
                + "&width=" + dimensions[0]
                + "&height=" + dimensions[1]
                + "&nologo=true";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException(describePollinationsError(response));
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("image/png");
        if (!contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalStateException("Pollinations returned an unexpected response instead of an image.");
        }
        if (response.body() == null || response.body().length == 0) {
            throw new IllegalStateException("Pollinations returned no image data.");
        }
        return "data:" + contentType.split(";", 2)[0] + ";base64,"
                + Base64.getEncoder().encodeToString(response.body());
    }

    private int[] parseDimensions(String size) {
        if (size == null || !size.matches("\\d{2,4}x\\d{2,4}")) {
            return new int[]{1024, 1024};
        }
        String[] parts = size.split("x");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private String describePollinationsError(HttpResponse<byte[]> response) {
        String body = new String(response.body(), StandardCharsets.UTF_8).trim();
        if (body.length() > 300) body = body.substring(0, 300);
        String suffix = body.isBlank() ? "" : " " + body;
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            return "Pollinations rejected the API key. Check POLLINATIONS_API_KEY." + suffix;
        }
        if (response.statusCode() == 429) {
            return "Pollinations is temporarily rate-limiting image generation. Please try again shortly.";
        }
        return "Pollinations image generation failed (HTTP " + response.statusCode() + ")." + suffix;
    }
}