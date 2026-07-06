package com.backend.service;

import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.AdsGenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AdsGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AdsGenerationService.class);

    /**
     * Generate creative ads based on user's idea
     */
    public AdsGenerationResponse generateAds(AdsGenerationRequest request) {
        try {
            // Generate prompt from user's idea
            String prompt = generatePrompt(request);
            log.info("Generated prompt: {}", prompt);

            // Generate ads content (simulated - you can integrate actual API)
            String generatedAds = generatedAdsContent(prompt);

            // Generate social media share links
            AdsGenerationResponse.ShareLinks shareLinks = generateShareLinks(generatedAds);

            // Create response
            AdsGenerationResponse response = new AdsGenerationResponse(
                    prompt,
                    generatedAds,
                    shareLinks,
                    "success",
                    "Ads generated successfully"
            );

            log.info("Ads generation completed successfully");
            return response;

        } catch (Exception e) {
            log.error("Error generating ads", e);
            return new AdsGenerationResponse(
                    null,
                    null,
                    null,
                    "error",
                    "Error generating ads: " + e.getMessage()
            );
        }
    }

    /**
     * Generate an optimized prompt for creative ads
     */
    private String generatePrompt(AdsGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a creative and engaging ad for: ").append(request.getIdea());

        if (request.getProductType() != null && !request.getProductType().isEmpty()) {
            prompt.append(" | Product Type: ").append(request.getProductType());
        }

        if (request.getTargetAudience() != null && !request.getTargetAudience().isEmpty()) {
            prompt.append(" | Target Audience: ").append(request.getTargetAudience());
        }

        if (request.getTone() != null && !request.getTone().isEmpty()) {
            prompt.append(" | Tone: ").append(request.getTone());
        } else {
            prompt.append(" | Tone: Professional and Engaging");
        }

        prompt.append(". Create multiple variations for different platforms (Facebook, Instagram, TikTok, WhatsApp).");
        return prompt.toString();
    }

    /**
     * Generate ads content (mock implementation)
     * In production, this would call the external ads generation API
     */
    private String generatedAdsContent(String prompt) {
        return """
                **Ad Variation 1 (Facebook/Instagram):**
                "Transform your business with innovative solutions! 🚀 Join thousands of satisfied customers today. Limited time offer!"
                
                **Ad Variation 2 (TikTok):**
                "Did you know? Our solution saves 50% of your time! Check out how → [Link]"
                
                **Ad Variation 3 (WhatsApp):**
                "Hi! 👋 We have an exclusive offer just for you. Get 20% off on your first purchase. Use code: SPECIAL20"
                
                **Ad Variation 4 (Short & Catchy):**
                "Discover the future. Experience the difference. Be part of our community!"
                """;
    }

    /**
     * Generate social media sharing links
     */
    private AdsGenerationResponse.ShareLinks generateShareLinks(String adsText) {
        try {
            String encodedText = URLEncoder.encode(adsText, StandardCharsets.UTF_8);

            // Facebook share link
            String facebookLink = "https://www.facebook.com/sharer/sharer.php?quote=" + encodedText;

            // Instagram doesn't support direct sharing via URL, but we can provide link
            String instagramLink = "https://www.instagram.com/";

            // TikTok share
            String tiktokLink = "https://www.tiktok.com/";

            // WhatsApp share link
            String whatsappLink = "https://api.whatsapp.com/send?text=" + encodedText;

            return new AdsGenerationResponse.ShareLinks(
                    facebookLink,
                    instagramLink,
                    tiktokLink,
                    whatsappLink
            );
        } catch (Exception e) {
            log.error("Error generating share links", e);
            return new AdsGenerationResponse.ShareLinks("", "", "", "");
        }
    }
}
