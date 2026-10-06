package com.backend.controller;

import com.backend.dto.social.*;
import com.backend.entity.Business;
import com.backend.entity.ConnectedSocialAccount;
import com.backend.entity.SocialPost;
import com.backend.repository.BusinessRepository;
import com.backend.security.CustomUserDetails;
import com.backend.service.AdsGenerationService;
import com.backend.service.CloudinaryStorageService;
import com.backend.service.InstagramService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
public class SocialMarketingController {

    private static final Logger log = LoggerFactory.getLogger(SocialMarketingController.class);

    private final AdsGenerationService adsGenerationService;
    private final InstagramService instagramService;
    private final BusinessRepository businessRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Value("${app.client.url:http://localhost:3000}")
    private String clientBaseUrl;

    public SocialMarketingController(
            AdsGenerationService adsGenerationService,
            InstagramService instagramService,
            BusinessRepository businessRepository,
            CloudinaryStorageService cloudinaryStorageService) {
        this.adsGenerationService = adsGenerationService;
        this.instagramService = instagramService;
        this.businessRepository = businessRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private Business getCurrentBusiness() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return businessRepository.findByOwner_Id(userId).stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No business registered for current user")
        );
    }

    /**
     * Generate an AI social marketing post (copy + image).
     */
    @PostMapping("/generate")
    public ResponseEntity<SocialGenerateResponse> generatePost(
            @RequestBody SocialGenerateRequest request,
            @RequestParam(defaultValue = "true") boolean generateImage) {
        SocialGenerateResponse response = adsGenerationService.generateSocialMarketingPost(request, generateImage);
        return ResponseEntity.ok(response);
    }

    /**
     * Get post generation history.
     */
    @GetMapping("/posts")
    public ResponseEntity<List<SocialPost>> getPosts() {
        try {
            return ResponseEntity.ok(adsGenerationService.getPostHistory());
        } catch (Exception ex) {
            log.error("Error loading post history: {}", ex.getMessage(), ex);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Update an existing social post draft.
     */
    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody SocialPost updated) {
        SocialPost post = adsGenerationService.getPostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        if (updated.getCaption() != null) post.setCaption(updated.getCaption());
        if (updated.getHeadline() != null) post.setHeadline(updated.getHeadline());
        if (updated.getCallToAction() != null) post.setCallToAction(updated.getCallToAction());
        if (updated.getHashtags() != null) post.setHashtags(updated.getHashtags());
        if (updated.getImageUrl() != null) post.setImageUrl(updated.getImageUrl());
        return ResponseEntity.ok(adsGenerationService.saveOrUpdatePost(post));
    }

    /**
     * Check Meta / Instagram connection status.
     */
    @GetMapping("/instagram/status")
    public ResponseEntity<InstagramStatusResponse> getInstagramStatus() {
        boolean configured = instagramService.isConfigured();
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(new InstagramStatusResponse(configured, false, null, null, null));
        }

        try {
            Business business = getCurrentBusiness();
            Optional<ConnectedSocialAccount> accountOpt = instagramService.getConnectedAccount(business.getId());
            if (accountOpt.isPresent()) {
                ConnectedSocialAccount account = accountOpt.get();
                return ResponseEntity.ok(new InstagramStatusResponse(
                        configured,
                        true,
                        account.getInstagramAccountId(),
                        account.getInstagramUsername(),
                        account.getFacebookPageName()
                ));
            }
        } catch (Exception ex) {
            log.debug("No business found for user: {}", ex.getMessage());
        }

        return ResponseEntity.ok(new InstagramStatusResponse(configured, false, null, null, null));
    }

    /**
     * Get Meta OAuth authorization URL.
     */
    @GetMapping("/instagram/connect")
    public ResponseEntity<?> getConnectUrl() {
        if (!instagramService.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Meta credentials are not configured on the server. Please set META_APP_ID and META_APP_SECRET."
            ));
        }

        try {
            Business business = getCurrentBusiness();
            String authUrl = instagramService.buildAuthorizationUrl(String.valueOf(business.getId()));
            return ResponseEntity.ok(Map.of("url", authUrl));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Connect Instagram directly using a Meta Access Token (Graph API Explorer token).
     */
    @PostMapping("/instagram/connect-token")
    public ResponseEntity<?> connectInstagramWithToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("accessToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Access token is required."));
        }
        String igAccountId = payload.get("instagramAccountId");
        String igUsername = payload.get("instagramUsername");

        try {
            Business business = getCurrentBusiness();
            ConnectedSocialAccount account = instagramService.connectWithToken(token, igAccountId, igUsername, business);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "instagramAccountId", account.getInstagramAccountId(),
                    "instagramUsername", account.getInstagramUsername(),
                    "facebookPageName", account.getFacebookPageName() != null ? account.getFacebookPageName() : "Facebook Page"
            ));
        } catch (Exception ex) {
            log.error("Failed to connect Instagram via token: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Meta OAuth redirect callback endpoint.
     */
    @GetMapping("/instagram/callback")
    public void handleOAuthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse response) throws IOException {

        String redirectTarget = clientBaseUrl.replaceAll("/+$", "") + "/dashboard/marketing/social";

        if (error != null || code == null) {
            log.warn("Instagram OAuth callback returned error: {}", error);
            response.sendRedirect(redirectTarget + "?instagram_error=" + (error != null ? error : "access_denied"));
            return;
        }

        try {
            Long businessId = null;
            if (state != null && !state.isBlank() && state.matches("\\d+")) {
                businessId = Long.parseLong(state);
            }

            Business business;
            if (businessId != null) {
                business = businessRepository.findById(businessId).orElseThrow(
                        () -> new IllegalStateException("Invalid business state")
                );
            } else {
                business = getCurrentBusiness();
            }

            instagramService.handleOAuthCallback(code, business);
            response.sendRedirect(redirectTarget + "?instagram_connected=true");
        } catch (Exception ex) {
            log.error("Failed to process Instagram OAuth callback: {}", ex.getMessage(), ex);
            response.sendRedirect(redirectTarget + "?instagram_error=" + ex.getMessage());
        }
    }

    /**
     * Disconnect linked Instagram account.
     */
    @DeleteMapping("/instagram/disconnect")
    public ResponseEntity<?> disconnectInstagram() {
        try {
            Business business = getCurrentBusiness();
            instagramService.disconnectAccount(business.getId());
            return ResponseEntity.ok(Map.of("message", "Instagram account disconnected successfully"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Publish post to Instagram.
     */
    @PostMapping("/instagram/publish")
    public ResponseEntity<?> publishToInstagram(@RequestBody InstagramPublishRequest request) {
        try {
            Business business = getCurrentBusiness();
            ConnectedSocialAccount account = instagramService.getConnectedAccount(business.getId()).orElseThrow(
                    () -> new IllegalStateException("Instagram is not connected. Connect an account first.")
            );

            SocialPost post = null;
            if (request.getPostId() != null) {
                post = adsGenerationService.getPostById(request.getPostId());
            }

            String imageUrl = request.getImageUrl() != null && !request.getImageUrl().isBlank()
                    ? request.getImageUrl()
                    : (post != null ? post.getImageUrl() : null);

            String caption = request.getCaption() != null && !request.getCaption().isBlank()
                    ? request.getCaption()
                    : (post != null ? post.getCaption() : null);

            if (imageUrl == null || imageUrl.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No image URL provided for Instagram publishing."));
            }

            // Auto-upload base64 to Cloudinary if needed
            if (imageUrl.startsWith("data:")) {
                log.info("Base64 image detected before Instagram publish. Uploading to Cloudinary...");
                var cloudOpt = cloudinaryStorageService.uploadImage(imageUrl);
                if (cloudOpt.isPresent()) {
                    imageUrl = cloudOpt.get();
                    if (post != null) {
                        post.setImageUrl(imageUrl);
                        adsGenerationService.saveOrUpdatePost(post);
                    }
                }
            }

            if (!imageUrl.startsWith("https://")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Instagram publishing requires a publicly accessible HTTPS image URL (Cloudinary). The current image is a local or base64 preview."
                ));
            }

            InstagramService.PublishResult result = instagramService.publishSingleImage(account, imageUrl, caption);

            if (post != null) {
                post.setStatus("PUBLISHED");
                post.setExternalPostId(result.getMediaId());
                post.setPermalink(result.getPermalink());
                post.setPublishedAt(LocalDateTime.now());
                adsGenerationService.saveOrUpdatePost(post);
            }

            return ResponseEntity.ok(new InstagramPublishResponse(
                    true,
                    post != null ? post.getId() : null,
                    result.getMediaId(),
                    result.getPermalink(),
                    "Successfully published to Instagram!"
            ));
        } catch (Exception ex) {
            log.error("Failed to publish to Instagram: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", ex.getMessage() != null ? ex.getMessage() : "Publishing to Instagram failed"
            ));
        }
    }

    /**
     * Publish post directly to connected Facebook Page.
     */
    @PostMapping("/facebook/publish")
    public ResponseEntity<?> publishToFacebook(@RequestBody InstagramPublishRequest request) {
        try {
            Business business = getCurrentBusiness();
            ConnectedSocialAccount account = instagramService.getConnectedAccount(business.getId()).orElseThrow(
                    () -> new IllegalStateException("Facebook is not connected. Connect an account first.")
            );

            SocialPost post = null;
            if (request.getPostId() != null) {
                post = adsGenerationService.getPostById(request.getPostId());
            }

            String imageUrl = request.getImageUrl() != null && !request.getImageUrl().isBlank()
                    ? request.getImageUrl()
                    : (post != null ? post.getImageUrl() : null);

            String caption = request.getCaption() != null && !request.getCaption().isBlank()
                    ? request.getCaption()
                    : (post != null ? post.getCaption() : null);

            // Auto-upload base64 to Cloudinary if possible
            if (imageUrl != null && imageUrl.startsWith("data:")) {
                log.info("Base64 image detected before Facebook publish. Uploading to Cloudinary...");
                var cloudOpt = cloudinaryStorageService.uploadImage(imageUrl);
                if (cloudOpt.isPresent()) {
                    imageUrl = cloudOpt.get();
                    if (post != null) {
                        post.setImageUrl(imageUrl);
                        adsGenerationService.saveOrUpdatePost(post);
                    }
                }
            }

            InstagramService.PublishResult result = instagramService.publishToFacebookPage(account, imageUrl, caption);

            if (post != null) {
                post.setStatus("PUBLISHED");
                post.setExternalPostId(result.getMediaId());
                post.setPermalink(result.getPermalink());
                post.setPublishedAt(LocalDateTime.now());
                adsGenerationService.saveOrUpdatePost(post);
            }

            return ResponseEntity.ok(new InstagramPublishResponse(
                    true,
                    post != null ? post.getId() : null,
                    result.getMediaId(),
                    result.getPermalink(),
                    "Successfully published to Facebook Page!"
            ));
        } catch (Exception ex) {
            log.error("Failed to publish to Facebook: {}", ex.getMessage(), ex);
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Publishing to Facebook failed";
            if (errorMsg.contains("pages_manage_posts")) {
                errorMsg = "Direct API posting to Facebook Page requires Meta App Review for 'pages_manage_posts'. Please use the 'Share on FB' button or publish directly to your connected Instagram account!";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", errorMsg
            ));
        }
    }
}
