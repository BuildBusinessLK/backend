package com.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Base64;
import java.util.List;

@Service
public class AiHordeService {

    private static final Logger log = LoggerFactory.getLogger(AiHordeService.class);

    private final String apiKey;
    private final String baseUrl;
    private final List<String> imageModels;
    private final List<String> textModels;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiHordeService(
            @Value("${app.aihorde.api-key:0000000000}") String apiKey,
            @Value("${app.aihorde.base-url:https://aihorde.net/api}") String baseUrl,
            @Value("${app.aihorde.image-models:AlbedoBase XL (SDXL),Juggernaut XL,stable_diffusion}") String imageModelsRaw,
            @Value("${app.aihorde.text-models:aphrodite/TheDrummer/Cydonia-24B-v4.3,aphrodite/TheDrummer/Skyfall-31B-v4.2,koboldcpp/L3-8B-Stheno-v3.2-Q5_K_M}") String textModelsRaw,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey == null || apiKey.isBlank() ? "0000000000" : apiKey.trim();
        this.baseUrl = (baseUrl == null || baseUrl.isBlank() ? "https://aihorde.net/api" : baseUrl.trim()).replaceAll("/+$", "");
        this.imageModels = parseList(imageModelsRaw);
        this.textModels = parseList(textModelsRaw);
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    private List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(","));
    }

    public String generateImage(String prompt, String size) throws Exception {
        int[] dims = parseDimensions(size);
        int width = dims[0];
        int height = dims[1];

        // Cap to max SDXL pixels (approx 1024x1024) to avoid Horde rejection for anonymous requests
        long totalPixels = (long) width * height;
        if (totalPixels > 1048576) {
            double scale = Math.sqrt(1048576.0 / totalPixels);
            width = (int) (width * scale);
            height = (int) (height * scale);
            // round to multiple of 64 or 8 for stable diffusion
            width = (width / 64) * 64;
            height = (height / 64) * 64;
            if (width < 256) width = 256;
            if (height < 256) height = 256;
            log.info("Scaled down image generation dimensions to {}x{} to fit AI Horde SDXL limits", width, height);
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("prompt", prompt);
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("width", width);
        params.put("height", height);
        params.put("steps", 20);
        params.put("n", 1);
        payload.set("params", params);

        ArrayNode modelsNode = objectMapper.createArrayNode();
        for (String m : imageModels) {
            modelsNode.add(m.trim());
        }
        payload.set("models", modelsNode);

        String jsonRequest = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/generate/async"))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .header("Client-Agent", "BuildBusinessLK:1.0:antigravity@gemini.google.com")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                .build();

        log.info("Submitting async image generation to AI Horde...");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("AI Horde async image generation request failed (HTTP " 
                    + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String id = root.path("id").asText();
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("AI Horde returned no request ID: " + response.body());
        }

        log.info("AI Horde request submitted. Job ID: {}. Polling status...", id);

        // Poll status endpoint
        String imageUrl = null;
        int maxAttempts = 60; // 2 minutes max
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Thread.sleep(2000); // Wait 2s (cache limit is 1s)

            HttpRequest statusRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/generate/status/" + id))
                    .header("apikey", apiKey)
                    .header("Client-Agent", "BuildBusinessLK:1.0:antigravity@gemini.google.com")
                    .GET()
                    .build();

            HttpResponse<String> statusResponse = httpClient.send(statusRequest, HttpResponse.BodyHandlers.ofString());
            if (statusResponse.statusCode() == 200) {
                JsonNode statusRoot = objectMapper.readTree(statusResponse.body());
                boolean isDone = statusRoot.path("done").asBoolean() || statusRoot.path("finished").asInt(0) > 0;
                if (isDone) {
                    JsonNode generations = statusRoot.path("generations");
                    if (generations.isArray() && generations.size() > 0) {
                        imageUrl = generations.get(0).path("img").asText();
                        log.info("AI Horde image generation completed! Image URL: {}", imageUrl);
                        break;
                    }
                } else {
                    int waitTime = statusRoot.path("wait_time").asInt(0);
                    int queuePosition = statusRoot.path("queue_position").asInt(0);
                    log.info("AI Horde generation in progress... Queue pos: {}, Wait time: {}s", queuePosition, waitTime);
                }
            } else {
                log.warn("AI Horde status polling HTTP error: {}", statusResponse.statusCode());
            }
        }

        if (imageUrl == null) {
            throw new IllegalStateException("AI Horde image generation timed out or failed.");
        }

        // Fetch image bytes
        log.info("Downloading generated image from URL: {} ...", imageUrl);
        HttpRequest imgRequest = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
        HttpResponse<byte[]> imgResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (imgResponse.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to download generated image from AI Horde (HTTP " + imgResponse.statusCode() + ")");
        }

        String contentType = imgResponse.headers().firstValue("Content-Type").orElse("image/png");
        return "data:" + contentType.split(";", 2)[0] + ";base64,"
                + Base64.getEncoder().encodeToString(imgResponse.body());
    }

    public String generateText(String prompt) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("prompt", prompt);

        ObjectNode params = objectMapper.createObjectNode();
        params.put("max_length", 600);
        params.put("temperature", 0.7);
        payload.set("params", params);

        ArrayNode modelsNode = objectMapper.createArrayNode();
        for (String m : textModels) {
            modelsNode.add(m.trim());
        }
        payload.set("models", modelsNode);

        String jsonRequest = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/generate/text/async"))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .header("Client-Agent", "BuildBusinessLK:1.0:antigravity@gemini.google.com")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                .build();

        log.info("Submitting async text generation to AI Horde...");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("AI Horde async text generation request failed (HTTP " 
                    + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String id = root.path("id").asText();
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("AI Horde returned no request ID: " + response.body());
        }

        log.info("AI Horde text request submitted. Job ID: {}. Polling status...", id);

        // Poll status endpoint
        String generatedText = null;
        int maxAttempts = 60; // 2 minutes max
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Thread.sleep(2000); // Wait 2s

            HttpRequest statusRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/generate/text/status/" + id))
                    .header("apikey", apiKey)
                    .header("Client-Agent", "BuildBusinessLK:1.0:antigravity@gemini.google.com")
                    .GET()
                    .build();

            HttpResponse<String> statusResponse = httpClient.send(statusRequest, HttpResponse.BodyHandlers.ofString());
            if (statusResponse.statusCode() == 200) {
                JsonNode statusRoot = objectMapper.readTree(statusResponse.body());
                boolean isDone = statusRoot.path("done").asBoolean() || statusRoot.path("finished").asInt(0) > 0;
                if (isDone) {
                    JsonNode generations = statusRoot.path("generations");
                    if (generations.isArray() && generations.size() > 0) {
                        generatedText = generations.get(0).path("text").asText();
                        log.info("AI Horde text generation completed!");
                        break;
                    }
                } else {
                    int waitTime = statusRoot.path("wait_time").asInt(0);
                    int queuePosition = statusRoot.path("queue_position").asInt(0);
                    log.info("AI Horde text generation in progress... Queue pos: {}, Wait time: {}s", queuePosition, waitTime);
                }
            } else {
                log.warn("AI Horde status polling HTTP error: {}", statusResponse.statusCode());
            }
        }

        if (generatedText == null) {
            throw new IllegalStateException("AI Horde text generation timed out or failed.");
        }

        return generatedText;
    }

    private int[] parseDimensions(String size) {
        if (size == null || !size.matches("\\d{2,4}x\\d{2,4}")) {
            return new int[]{1024, 1024};
        }
        String[] parts = size.split("x");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}
