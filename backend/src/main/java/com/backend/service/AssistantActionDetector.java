package com.backend.service;

import com.backend.domain.AssistantActions;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.List;

@Component
public class AssistantActionDetector {

    private static final List<String> WEBSITE_KEYWORDS = List.of(
            "online presence",
            "digital marketing",
            "landing page",
            "web site",
            "website",
            "business site",
            "create a site",
            "build a site",
            "sell online",
            "e-commerce",
            "ecommerce"
    );

    public String detectActionForAssistantMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String n = message.toLowerCase(Locale.ROOT);
        for (String k : WEBSITE_KEYWORDS) {
            if (n.contains(k)) {
                return AssistantActions.GENERATE_WEBSITE;
            }
        }
        return null;
    }
}
