package com.backend.service;

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

        StringBuilder userContext = new StringBuilder();
        if (userProfile != null && !userProfile.isEmpty()) {
            userContext.append("User Profile:\n");
            userProfile.forEach((key, value) -> {
                if (value != null) {
                    userContext.append("- ")
                            .append(key)
                            .append(": ")
                            .append(value)
                            .append("\n");
                }
            });
        } else {
            userContext.append("User Profile: not provided\n");
        }

        return String.format("""
You are an expert AI marketing strategist and advertising copywriter.

Your task is to generate a professional advertisement that matches the user's business and audience.

==============================
BUSINESS INFORMATION
==============================

Business Name:
%s

Industry:
%s

Business Description:
%s

Target Market:
%s

Marketing Goals:
%s

Monthly Production:
%s

Products:
%s

%s

%s

==============================
USER REQUEST
==============================

%s

==============================
INSTRUCTIONS
==============================

Understand the business before writing.

Use the business information and available social account details to personalize the advertisement.

Write naturally and make the ad feel relevant to the user’s actual profile.

Highlight the business strengths.

Focus on the target market.

Create a persuasive call-to-action.

Generate:

1. Facebook Advertisement

2. Instagram Caption

3. Google Advertisement

4. Five Headlines

5. Five Marketing Hashtags

6. Marketing Suggestions
""",

                business.getBusinessName(),
                business.getSector(),
                business.getBusinessDescription(),
                business.getTargetMarket(),
                business.getMarketingGoals(),
                business.getMonthlyProduction(),
                products.toString(),
                socialContext.toString(),
                userContext.toString(),
                request.getIdea()
        );

    }

}