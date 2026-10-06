package com.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdVisualService {

    private static final Logger log = LoggerFactory.getLogger(AdVisualService.class);

    private final AiHordeService aiHordeService;
    private final HttpClient httpClient;

    public AdVisualService(AiHordeService aiHordeService) {
        this.aiHordeService = aiHordeService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Generates a high-quality marketing visual.
     * Primary: Pollinations.ai (FLUX.1) - fast (3-5s), stunning commercial quality, free.
     * Fallback: AI Horde.
     */
    public String generate(String prompt, String size) throws Exception {
        // Try Pollinations.ai for instant public HTTPS image URL
        try {
            log.info("Generating marketing visual with Pollinations.ai for prompt: {}", prompt);
            long seed = ThreadLocalRandom.current().nextLong(1, 999999999);
            // Clean prompt for URL
            String cleanPrompt = prompt.replaceAll("[^a-zA-Z0-9 ,.-]", " ").replaceAll("\\s+", " ").trim();
            if (cleanPrompt.length() > 200) {
                cleanPrompt = cleanPrompt.substring(0, 200).trim();
            }
            String encodedPrompt = URLEncoder.encode(cleanPrompt, StandardCharsets.UTF_8);
            String pollinationsUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt
                    + ".jpg?width=1024&height=1024&model=turbo&nologo=true&seed=" + seed;

            log.info("Direct public image URL ready: {}", pollinationsUrl);
            return pollinationsUrl;
        } catch (Exception ex) {
            log.warn("Pollinations URL generation failed ({}), falling back to AI Horde", ex.getMessage());
        }

        // Fallback: AI Horde
        return aiHordeService.generateImage(prompt, size);
    }
}