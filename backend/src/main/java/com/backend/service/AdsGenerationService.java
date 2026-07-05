package com.backend.service;

import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.AdsGenerationResponse;
import com.backend.dto.ai.AdGenerationRequest;
import com.backend.dto.ai.AdGenerationResponse;
import com.backend.dto.business.BusinessDetailDto;
import com.backend.dto.business.BusinessSocialLinkDto;
import com.backend.entity.Business;
import com.backend.entity.User;
import com.backend.entity.UserProfile;
import com.backend.repository.BusinessProfileRepository;
import com.backend.repository.BusinessRepository;
import com.backend.repository.BusinessSocialLinkRepository;
import com.backend.repository.UserProfileRepository;
import com.backend.repository.UserRepository;
import com.backend.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdsGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AdsGenerationService.class);

    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;
    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessSocialLinkRepository businessSocialLinkRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public AdsGenerationService(
            PromptBuilderService promptBuilderService,
            AiClientService aiClientService,
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository) {
        this.promptBuilderService = promptBuilderService;
        this.aiClientService = aiClientService;
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public AdsGenerationResponse generateAds(AdsGenerationRequest request) {

        try {

            Long userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("No authenticated user found");
            }

            BusinessDetailDto business = resolveBusinessContext(userId);
List<BusinessSocialLinkDto> socialLinks = business.getSocialLinks();
Map<String, Object> userProfile = resolveUserProfileContext(userId);

log.info("========== BUSINESS DATA ==========");
log.info("Business Name: {}", business.getBusinessName());
log.info("Sector: {}", business.getSector());
log.info("Description: {}", business.getBusinessDescription());
log.info("Target Market: {}", business.getTargetMarket());
log.info("Marketing Goals: {}", business.getMarketingGoals());
log.info("Monthly Production: {}", business.getMonthlyProduction());
log.info("Products: {}", business.getProducts());
log.info("Social Links: {}", business.getSocialLinks());
log.info("==================================");

            String prompt =
                    promptBuilderService.buildAdPrompt(
                            business,
                            request,
                            socialLinks,
                            userProfile
                    );

            log.info("Generated Prompt: {}", prompt);

            AdGenerationRequest aiRequest = new AdGenerationRequest();
            aiRequest.setPrompt(prompt);
aiRequest.setBusinessProfile(buildBusinessProfilePayload(business));
aiRequest.setUserProfile(userProfile);

log.info("========== AI REQUEST ==========");
log.info("Prompt:\n{}", prompt);
log.info("Business Profile: {}", aiRequest.getBusinessProfile());
log.info("User Profile: {}", aiRequest.getUserProfile());
log.info("================================");

AdGenerationResponse aiResponse = aiClientService.generateAdCopy(aiRequest);
            String generatedAds = "";

if(aiResponse != null){

    generatedAds = aiResponse.getGeneratedAds();

}

            AdsGenerationResponse.ShareLinks shareLinks = generateShareLinks(generatedAds);

            return new AdsGenerationResponse(
                    prompt,
                    generatedAds,
                    shareLinks,
                    "success",
                    "Ads generated successfully"
            );

        } catch (Exception e) {

            log.error("Error generating ads", e);

            return new AdsGenerationResponse(
                    "",
                    "",
                    new AdsGenerationResponse.ShareLinks("", "", "", ""),
                    "error",
                    e.getMessage()
            );
        }
    }

    private Map<String, Object> buildBusinessProfilePayload(BusinessDetailDto business) {

    Map<String, Object> profile = new LinkedHashMap<>();

    profile.put("businessName", business.getBusinessName());
    profile.put("sector", business.getSector());
    profile.put("businessDescription", business.getBusinessDescription());
    profile.put("targetMarket", business.getTargetMarket());
    profile.put("marketingGoals", business.getMarketingGoals());
    profile.put("monthlyProduction", business.getMonthlyProduction());
    profile.put("monthlyIncome", business.getMonthlyIncome());

    profile.put("products", business.getProducts());

    profile.put("socialLinks", business.getSocialLinks());

    return profile;
}

    private BusinessDetailDto resolveBusinessContext(Long userId) {
        Business business = businessRepository.findByOwner_Id(userId).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No business found for the current user")
        );

        BusinessDetailDto dto = new BusinessDetailDto();
        dto.setId(business.getId());
        dto.setBusinessName(business.getBusinessName());
        dto.setSector(business.getSector());
        dto.setWebsiteSlug(business.getWebsiteSlug());

        businessProfileRepository.findByBusiness_Id(business.getId()).ifPresent(profile -> {
            dto.setBusinessDescription(profile.getBusinessDescription());
            dto.setTargetMarket(profile.getTargetMarket());
            dto.setMonthlyIncome(profile.getMonthlyIncome());
            dto.setMonthlyProduction(profile.getMonthlyProduction());
            dto.setMarketingGoals(profile.getMarketingGoals());
        });

        List<BusinessSocialLinkDto> socialLinks = businessSocialLinkRepository.findByBusiness_IdOrderByIdAsc(business.getId()).stream()
                .map(link -> {
                    BusinessSocialLinkDto dtoLink = new BusinessSocialLinkDto();
                    dtoLink.setId(link.getId());
                    dtoLink.setPlatform(link.getPlatform());
                    dtoLink.setUrl(link.getUrl());
                    return dtoLink;
                })
                .toList();
        dto.setSocialLinks(socialLinks);
        return dto;
    }

    private Map<String, Object> resolveUserProfileContext(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);
        Map<String, Object> map = new LinkedHashMap<>();
        if (user != null) {
            map.put("email", user.getEmail());
            map.put("role", user.getRole().name());
            map.put("status", user.getStatus().name());
        }
        if (profile != null) {
            map.put("fullName", profile.getFullName());
            map.put("phone", profile.getPhone());
            map.put("district", profile.getDistrict());
            map.put("experienceLevel", profile.getExperienceLevel());
            map.put("preferredLanguage", profile.getPreferredLanguage());
        }
        return map;
    }

    private Long getCurrentUserId() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    System.out.println("Authentication = " + authentication);

    if (authentication == null) {
        System.out.println("Authentication is NULL");
        return null;
    }

    System.out.println("Authenticated = " + authentication.isAuthenticated());

    System.out.println("Principal = " + authentication.getPrincipal());

    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
        System.out.println("User ID = " + userDetails.getId());
        return userDetails.getId();
    }

    System.out.println("Principal class = " +
            authentication.getPrincipal().getClass());

    return null;
}

    private String generatePrompt(AdsGenerationRequest request) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("Create a creative advertisement for ");

        if (request.getIdea() != null) {
            prompt.append(request.getIdea());
        }

        if (request.getProductType() != null && !request.getProductType().isBlank()) {
            prompt.append(". Product Type: ").append(request.getProductType());
        }

        if (request.getTargetAudience() != null && !request.getTargetAudience().isBlank()) {
            prompt.append(". Target Audience: ").append(request.getTargetAudience());
        }

        if (request.getTone() != null && !request.getTone().isBlank()) {
            prompt.append(". Tone: ").append(request.getTone());
        } else {
            prompt.append(". Tone: Professional");
        }

        prompt.append(". Generate Facebook, Instagram, TikTok and WhatsApp ad copies.");

        return prompt.toString();
    }

    private String generateAdsContent(String prompt) {

        return """
                Facebook Ad
                --------------------
                🚀 Grow your business today!
                Discover amazing products and services designed for you.

                Instagram Ad
                --------------------
                ✨ Your success starts here.
                Join thousands of happy customers today!

                TikTok Ad
                --------------------
                🎥 Don't miss out!
                Try it today and see the difference.

                WhatsApp Ad
                --------------------
                Hello 👋
                We have an exclusive offer for you.
                Contact us today for more information.
                """;
    }

    private AdsGenerationResponse.ShareLinks generateShareLinks(String adsText) {

        try {

            String encoded = URLEncoder.encode(adsText, StandardCharsets.UTF_8);

            String facebook =
                    "https://www.facebook.com/sharer/sharer.php?quote=" + encoded;

            String instagram =
                    "https://www.instagram.com/";

            String tiktok =
                    "https://www.tiktok.com/";

            String whatsapp =
                    "https://api.whatsapp.com/send?text=" + encoded;

            return new AdsGenerationResponse.ShareLinks(
                    facebook,
                    instagram,
                    tiktok,
                    whatsapp
            );

        } catch (Exception e) {

            log.error("Error generating share links", e);

            return new AdsGenerationResponse.ShareLinks(
                    "",
                    "",
                    "",
                    ""
            );
        }
    }
}