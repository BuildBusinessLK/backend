package com.backend.service;

import com.backend.domain.AssistantActions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detects suggested actions from AI assistant replies.
 *
 * Only GENERATE_WEBSITE is active. The others are wired up for future activation
 * by simply adding their keyword lists — no structural changes required.
 */
@Component
public class AssistantActionDetector {

    // GENERATE_WEBSITE — active
    private static final List<String> WEBSITE_KEYWORDS = List.of(
            "create a website",
            "build a website",
            "create a site",
            "build a site",
            "online presence",
            "digital marketing",
            "landing page",
            "business site",
            "web site",
            "website",
            "sell online",
            "e-commerce",
            "ecommerce",
            "start selling online",
            "web page"
    );

    // EMAIL_CAMPAIGN — active
    private static final List<String> EMAIL_KEYWORDS = List.of(
            "email campaign",
            "send emails",
            "email marketing",
            "newsletter",
            "customer email",
            "email your customers",
            "email blast",
            "mail campaign"
    );

    // SOCIAL_MARKETING — active
    private static final List<String> SOCIAL_KEYWORDS = List.of(
            "social media post",
            "ad campaign",
            "facebook ad",
            "instagram ad",
            "social ad",
            "social marketing",
            "create an ad",
            "post generator",
            "advertisement"
    );

    // VIEW_PROFILE — active (future: navigate user to profile editor)
    private static final List<String> PROFILE_KEYWORDS = List.of(
            "update your business profile",
            "complete your profile",
            "fill in your profile",
            "add your products",
            "add your product list",
            "update your contact details"
    );

    // Patterns for whole-word matching where needed
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(
            "\\b(website|web\\s*site|landing\\s*page|ecommerce|e-commerce)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Returns the most relevant action constant or {@code null} if none applies.
     */
    public String detectActionForAssistantMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String normalized = message.toLowerCase(Locale.ROOT);

        // GENERATE_WEBSITE
        for (String keyword : WEBSITE_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return AssistantActions.GENERATE_WEBSITE;
            }
        }
        if (WEBSITE_PATTERN.matcher(message).find()) {
            return AssistantActions.GENERATE_WEBSITE;
        }

        // EMAIL_CAMPAIGN
        for (String keyword : EMAIL_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return AssistantActions.EMAIL_CAMPAIGN;
            }
        }

        // SOCIAL_MARKETING
        for (String keyword : SOCIAL_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return AssistantActions.SOCIAL_MARKETING;
            }
        }

        // VIEW_PROFILE (lower priority)
        for (String keyword : PROFILE_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return AssistantActions.VIEW_PROFILE;
            }
        }

        return null;
    }
}
