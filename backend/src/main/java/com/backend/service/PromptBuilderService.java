package com.backend.service;

import com.backend.domain.Sector;
import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.business.BusinessDetailDto;
import com.backend.dto.business.BusinessProductDto;
import com.backend.dto.business.BusinessSocialLinkDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PromptBuilderService {

    public String buildAdPrompt(
    BusinessDetailDto business,
    AdsGenerationRequest request
    
) {
        return buildAdPrompt(
            business,
            request,
            null,
            null
    );
    }

    public String buildAdPrompt(
        BusinessDetailDto business,
        AdsGenerationRequest request,
        List<BusinessSocialLinkDto> socialLinks,
        Map<String, Object> userProfile) {

        StringBuilder products = new StringBuilder();

        if (business.getProducts() != null) {
            for (BusinessProductDto product : business.getProducts()) {
                products.append("- ")
                        .append(product.getName());

                if (product.getCategory() != null) {
                    products.append(" (Category: ")
                            .append(product.getCategory())
                            .append(")");
                }

                if (product.getDescription() != null) {
                    products.append(" - ")
                            .append(product.getDescription());
                }

                products.append("\n");
            }
        }

        StringBuilder socialContext = new StringBuilder();
        if (socialLinks != null && !socialLinks.isEmpty()) {
            socialContext.append("Social Accounts:\n");
            for (BusinessSocialLinkDto link : socialLinks) {
                if (link != null && link.getPlatform() != null && link.getUrl() != null) {
                    socialContext.append("- ")
                            .append(link.getPlatform())
                            .append(": ")
                            .append(link.getUrl())
                            .append("\n");
                }
            }
        } else {
            socialContext.append("Social Accounts: none provided\n");
        }

        String tone = (request.getTone() != null && !request.getTone().isBlank()) ? request.getTone() : "professional";
        String platform = (request.getPlatform() != null && !request.getPlatform().isBlank()) ? request.getPlatform() : "social media";
        String website = (request.getWebsite() != null && !request.getWebsite().isBlank()) ? request.getWebsite() : "";
        String idea = (request.getIdea() != null && !request.getIdea().isBlank()) ? request.getIdea() : "a compelling offer";

        String businessName = orDefault(business.getBusinessName(), "the business");
        Sector sectorValue = business.getSector();
        String sector = orDefault(sectorValue != null ? sectorValue.name() : null, "General");
        String description = orDefault(business.getBusinessDescription(), "Not specified");
        String targetMarket = orDefault(business.getTargetMarket(), "General audience");
        String marketingGoals = orDefault(business.getMarketingGoals(), "Not specified");
        String monthlyProduction = business.getMonthlyProduction() != null
                ? business.getMonthlyProduction().toString()
                : "Not specified";
        String productsText = products.length() > 0 ? products.toString().trim() : "Not specified";
        String websiteLine = website.isBlank() ? "" : "\nWebsite: " + website;

        // NOTE: This is a concise creative brief, not a full standalone prompt.
        // The AI service (Python) owns the system-level instructions and formatting
        // rules; this brief only supplies the facts the model needs to personalize
        // the ad. Keeping this short avoids nesting a second, conflicting prompt
        // inside the one the AI service builds, which was causing the model to
        // echo instructions back as ad copy instead of writing actual ads.
        return String.format("""
You are a professional social media marketing expert.

Generate platform-specific social media content for the following business.

Business Information
--------------------
Business Name: %s
Sector: %s
Description: %s
Target Market: %s
Marketing Goal: %s
Monthly Production: %s

Products
--------
%s

Social Accounts
---------------
%s

Campaign Idea
-------------
%s

Tone
----
%s

Website
-------
%s

IMPORTANT:

Generate DIFFERENT content for EACH platform.

Each platform should be optimized according to best practices.

Facebook:
- Friendly
- Long post
- CTA
- 5-8 hashtags

Instagram:
- Short caption
- Emoji
- 8-15 hashtags

LinkedIn:
- Professional
- Business tone
- No emojis unless appropriate

Twitter:
- Maximum 280 characters
- Few hashtags

WhatsApp:
- Promotional message
- Friendly
- Easy to read

Return ONLY valid JSON.

Example:

{
  "facebook":"...",
  "instagram":"...",
  "linkedin":"...",
  "twitter":"...",
  "whatsapp":"..."
}

Do NOT include markdown.

Do NOT include explanations.

Do NOT wrap JSON inside ```.

""",
        businessName,
        sector,
        description,
        targetMarket,
        marketingGoals,
        monthlyProduction,
        productsText,
        socialContext.toString().trim(),
        idea,
        tone,
        website

        ).stripIndent();
    }

    private String orDefault(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

}