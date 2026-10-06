package com.backend.service;

import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.AdsGenerationResponse;
import com.backend.dto.AdVisualRequest;
import com.backend.dto.AdVisualResponse;
import com.backend.dto.ai.AdGenerationRequest;
import com.backend.dto.ai.AdGenerationResponse;
import com.backend.dto.business.BusinessDetailDto;
import com.backend.dto.business.BusinessSocialLinkDto;
import com.backend.entity.Business;
import com.backend.entity.User;
import com.backend.entity.UserProfile;
import com.backend.dto.social.SocialGenerateRequest;
import com.backend.dto.social.SocialGenerateResponse;
import com.backend.entity.SocialPost;
import com.backend.repository.BusinessProfileRepository;
import com.backend.repository.BusinessRepository;
import com.backend.repository.BusinessSocialLinkRepository;
import com.backend.repository.SocialPostRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final AdVisualService adVisualService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final SocialPostRepository socialPostRepository;

    public AdsGenerationService(
            PromptBuilderService promptBuilderService,
            AiClientService aiClientService,
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            AdVisualService adVisualService,
            CloudinaryStorageService cloudinaryStorageService,
            SocialPostRepository socialPostRepository) {
        this.promptBuilderService = promptBuilderService;
        this.aiClientService = aiClientService;
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.adVisualService = adVisualService;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.socialPostRepository = socialPostRepository;
    }

    /** Generates three professional visual formats while preserving the existing text-ad flow. */
    public AdVisualResponse generateVisuals(AdVisualRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) throw new IllegalStateException("No authenticated user found");
        BusinessDetailDto business = resolveBusinessContext(userId);
        String prompt = buildVisualPrompt(request, business);
        try {
            Map<String, String> images = new LinkedHashMap<>();
            images.put("facebook_linkedin", adVisualService.generate(prompt + " Landscape social-media ad composition; keep generous clear space for a headline. Do not render words in the image.", "1536x1024"));
            images.put("instagram_whatsapp", adVisualService.generate(prompt + " Square social-media ad composition; keep generous clear space for a headline. Do not render words in the image.", "1024x1024"));
            images.put("tiktok", adVisualService.generate(prompt + " Vertical short-video cover composition; keep generous clear space for a headline. Do not render words in the image.", "1024x1536"));
            return new AdVisualResponse(images, "Visual ads generated for all platforms.");
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private String buildVisualPrompt(AdVisualRequest request, BusinessDetailDto business) {
        String revision = request.getInstruction() == null || request.getInstruction().isBlank() ? "" : " Revision request: " + request.getInstruction();
        return "Create a premium, realistic advertising visual for " + business.getBusinessName()
                + ". Business sector: " + business.getSector()
                + ". Business description: " + business.getBusinessDescription()
                + ". Campaign idea: " + request.getIdea()
                + ". Use a polished commercial photography or illustration style that suits the product/service and target audience."
                + revision
                + " No logos, no watermark, no readable text, no invented prices or claims.";
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
aiRequest.setIdea(request.getIdea());
aiRequest.setTone(request.getTone());
aiRequest.setPlatform(request.getPlatform());
aiRequest.setWebsite(request.getWebsite());
aiRequest.setBusinessProfile(buildBusinessProfilePayload(business));
aiRequest.setUserProfile(userProfile);

log.info("========== AI REQUEST ==========");
log.info("Prompt:\n{}", prompt);
log.info("Business Profile: {}", aiRequest.getBusinessProfile());
log.info("User Profile: {}", aiRequest.getUserProfile());
log.info("================================");

            AdGenerationResponse aiResponse = aiClientService.generateAdCopy(aiRequest);
System.out.println("============== AI RESPONSE ==============");
System.out.println(aiResponse);
System.out.println("Generated Ads:");
System.out.println(aiResponse.getGeneratedAds());
System.out.println("=========================================");
            String generatedAds = extractGeneratedAds(aiResponse, request, business, userProfile);
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

    private String extractGeneratedAds(
            AdGenerationResponse aiResponse,
            AdsGenerationRequest request,
            BusinessDetailDto business,
            Map<String, Object> userProfile) {
        if (aiResponse != null && aiResponse.getGeneratedAds() != null && !aiResponse.getGeneratedAds().isBlank()) {
            return aiResponse.getGeneratedAds();
        }

        return buildFallbackAdCopy(request, business, userProfile);
    }

    private String buildFallbackAdCopy(
            AdsGenerationRequest request,
            BusinessDetailDto business,
            Map<String, Object> userProfile) {
        String businessName = business != null && business.getBusinessName() != null
                ? business.getBusinessName()
                : "your business";
        String description = business != null && business.getBusinessDescription() != null
                ? business.getBusinessDescription()
                : "quality products and services";
        String idea = (request.getIdea() != null && !request.getIdea().isBlank())
                ? request.getIdea()
                : "your offer";
        String tone = (request.getTone() != null && !request.getTone().isBlank())
                ? request.getTone()
                : "professional";
        String platform = (request.getPlatform() != null && !request.getPlatform().isBlank())
                ? request.getPlatform()
                : "social media";
        String website = (request.getWebsite() != null && !request.getWebsite().isBlank())
                ? request.getWebsite()
                : "";
        String ownerContext = userProfile != null && userProfile.get("fullName") != null
                ? " for " + userProfile.get("fullName")
                : "";
        String websiteMention = website.isBlank() ? "" : " Visit " + website + " to learn more.";

        return String.format("""
Facebook Ad
--------------------
%s is ready to help you grow with %s.%s
%s

Instagram Caption
--------------------
%s is bringing a fresh idea to life with %s.%s
Make it yours today and experience the difference.

WhatsApp Advertisement
--------------------
Hi! We are %s and we are excited to share %s with you.%s
Contact us today to get started.

Headline Ideas
--------------------
- %s for %s
- Discover %s today
- Better solutions from %s
- Trusted by customers in %s
- A fresh approach to %s

Hashtags
--------------------
#%s #BusinessGrowth #SME #Marketing #DigitalMarketing
""",
                businessName,
                idea,
                websiteMention,
                description,
                businessName,
                idea,
                websiteMention,
                businessName,
                idea,
                websiteMention,
                idea,
                tone,
                idea,
                businessName,
                platform,
                businessName.replace(" ", "")
        );
    }

    private String generatePrompt(AdsGenerationRequest request) {

        StringBuilder prompt = new StringBuilder();

        if (request.getIdea() != null) {
            prompt.append(request.getIdea());
        }

        if (request.getProductType() != null && !((String) request.getProductType()).isBlank()) {
            prompt.append(". Product Type: ").append(request.getProductType());
        }

        if (request.getTargetAudience() != null && !((String) request.getTargetAudience()).isBlank()) {
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

    public SocialGenerateResponse generateSocialMarketingPost(SocialGenerateRequest request, boolean generateImage) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Business business = businessRepository.findByOwner_Id(userId).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No business found for the current user")
        );

        BusinessDetailDto businessDto = resolveBusinessContext(userId);
        Map<String, Object> userProfile = resolveUserProfileContext(userId);

        AdGenerationRequest aiRequest = new AdGenerationRequest();
        aiRequest.setPrompt(request.getPrompt() != null ? request.getPrompt() : "");
        aiRequest.setIdea(request.getIdea());
        aiRequest.setTone(request.getTone());
        aiRequest.setPlatform(request.getPlatform());
        aiRequest.setBusinessProfile(buildBusinessProfilePayload(businessDto));
        aiRequest.setUserProfile(userProfile);

        log.info("Generating social marketing post for business={} platform={}", business.getBusinessName(), request.getPlatform());
        AdGenerationResponse aiResponse = aiClientService.generateAdCopy(aiRequest);

        SocialGenerateResponse response = new SocialGenerateResponse();
        response.setPlatform(request.getPlatform());
        response.setHeadline(aiResponse.getHeadline() != null && !aiResponse.getHeadline().isBlank() 
                ? aiResponse.getHeadline() : "Quality Sri Lankan Products");
        response.setCaption(aiResponse.getCaption() != null && !aiResponse.getCaption().isBlank() 
                ? aiResponse.getCaption() : aiResponse.getGeneratedAds());
        response.setCallToAction(aiResponse.getCallToAction() != null && !aiResponse.getCallToAction().isBlank() 
                ? aiResponse.getCallToAction() : "Connect with us today!");
        response.setHashtags(aiResponse.getHashtags() != null && !aiResponse.getHashtags().isEmpty() 
                ? aiResponse.getHashtags() : List.of("#SriLankanBusiness", "#CeylonQuality", "#SME"));
        response.setImagePrompt(aiResponse.getImagePrompt() != null && !aiResponse.getImagePrompt().isBlank() 
                ? aiResponse.getImagePrompt() : "Commercial product photography of authentic Sri Lankan product with soft natural lighting");
        response.setGeneratedAds(aiResponse.getGeneratedAds());

        // Handle Image Generation & Cloudinary Upload
        String rawImageUrl = null;
        boolean isCloud = false;
        if (generateImage) {
            try {
                log.info("Generating marketing visual with prompt: {}", response.getImagePrompt());
                String visualPrompt = response.getImagePrompt() + " Clean commercial product photography; clear space; no text; no watermark.";
                String imageBytesOrBase64 = adVisualService.generate(visualPrompt, "1024x1024");
                if (imageBytesOrBase64 != null && !imageBytesOrBase64.isBlank()) {
                    var cloudUrlOpt = cloudinaryStorageService.uploadImage(imageBytesOrBase64);
                    if (cloudUrlOpt.isPresent()) {
                        rawImageUrl = cloudUrlOpt.get();
                        isCloud = true;
                        log.info("Image stored on Cloudinary: {}", rawImageUrl);
                    } else {
                        rawImageUrl = imageBytesOrBase64;
                        isCloud = imageBytesOrBase64.startsWith("https://");
                        log.info("Image URL ready: {}", rawImageUrl);
                    }
                }
            } catch (Exception ex) {
                log.warn("Visual generation failed or timed out: {}", ex.getMessage());
                response.setMessage("AI post drafted successfully. Image generation timed out, but you can retry image creation anytime.");
            }
        }

        response.setImageUrl(rawImageUrl);
        response.setCloudImage(isCloud);

        // Save SocialPost draft in database
        try {
            SocialPost post = new SocialPost();
            post.setBusiness(business);
            post.setPlatform(request.getPlatform().toUpperCase());
            post.setHeadline(response.getHeadline());
            post.setCaption(response.getCaption());
            post.setCallToAction(response.getCallToAction());
            post.setHashtags(String.join(" ", response.getHashtags()));
            post.setImageUrl(rawImageUrl);
            post.setImagePrompt(response.getImagePrompt());
            post.setStatus("DRAFT");
            SocialPost saved = socialPostRepository.save(post);
            response.setId(saved.getId());
            response.setStatus(saved.getStatus());
        } catch (Exception ex) {
            log.warn("Could not persist social post draft: {}", ex.getMessage());
        }

        return response;
    }

    public List<SocialPost> getPostHistory() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return businessRepository.findByOwner_Id(userId).stream()
                .findFirst()
                .map(b -> socialPostRepository.findByBusiness_IdOrderByCreatedAtDesc(b.getId()))
                .orElse(List.of());
    }

    public SocialPost getPostById(Long id) {
        return socialPostRepository.findById(id).orElse(null);
    }

    public SocialPost saveOrUpdatePost(SocialPost post) {
        return socialPostRepository.save(post);
    }
}
