package com.backend.service;

import com.backend.dto.WebsiteGenerateRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebsiteGeneratorService {

    private static final String GEMINI_MODEL = "gemini-2.0-flash";

    private final RestTemplate restTemplate;

    public WebsiteGeneratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> generateWebsite(WebsiteGenerateRequest request) {
        String prompt = request != null ? request.getPrompt() : null;
        boolean useFallback = request != null && Boolean.TRUE.equals(request.getUseFallback());

        if (prompt == null || prompt.trim().isEmpty()) {
            return Map.of("error", "Missing prompt in request");
        }

        String apiKey = Optional.ofNullable(System.getenv("GOOGLE_AI_API_KEY"))
                .orElseGet(() -> System.getenv("GEMINI_API_KEY"));

        if (useFallback || apiKey == null || apiKey.isBlank()) {
            return fallbackResponse(prompt, apiKey == null || apiKey.isBlank()
                    ? "GOOGLE_AI_API_KEY is not configured. Using fallback generator."
                    : null);
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = buildGeminiRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {
            };

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    typeRef
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Pass-through response body (it already contains candidates[])
                return response.getBody();
            }

            return fallbackResponse(prompt, "API error occurred. Using fallback generator. A complete website template has been generated.");

        } catch (HttpClientErrorException.TooManyRequests ex) {
            return fallbackResponse(prompt, "Using fallback generator due to API quota limits. A complete website template has been generated.");
        } catch (HttpClientErrorException ex) {
            String body = safeBody(ex);
            if (body.contains("RESOURCE_EXHAUSTED") || body.toLowerCase(Locale.ROOT).contains("quota")) {
                return fallbackResponse(prompt, "Using fallback generator due to API quota limits. A complete website template has been generated.");
            }
            return fallbackResponse(prompt, "API error occurred. Using fallback generator. A complete website template has been generated.");
        } catch (ResourceAccessException ex) {
            return fallbackResponse(prompt, "API connection failed. Using fallback generator. A complete website template has been generated.");
        } catch (Exception ex) {
            return fallbackResponse(prompt, "API error occurred. Using fallback generator. A complete website template has been generated.");
        }
    }

    private static Map<String, Object> buildGeminiRequestBody(String prompt) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 8192);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(parts));
        body.put("generationConfig", generationConfig);
        return body;
    }

    private static Map<String, Object> fallbackResponse(String prompt, String message) {
        Map<String, String> fallback = WebsiteTemplateGenerator.generateFallbackWebsite(prompt);

        String combined = "```html\n" + fallback.get("html") + "\n```\n\n" +
                "```css\n" + fallback.get("css") + "\n```\n\n" +
                "```javascript\n" + fallback.get("js") + "\n```";

        Map<String, Object> response = new LinkedHashMap<>();

        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", combined);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> candidate = new LinkedHashMap<>();
        candidate.put("content", content);

        response.put("candidates", List.of(candidate));
        response.put("fallback", true);
        if (message != null && !message.isBlank()) {
            response.put("message", message);
        }

        return response;
    }

    private static String safeBody(HttpClientErrorException ex) {
        try {
            return ex.getResponseBodyAsString() == null ? "" : ex.getResponseBodyAsString();
        } catch (Exception ignored) {
            return "";
        }
    }
}
