package com.backend.service;

import com.backend.entity.Business;
import com.backend.entity.ConnectedSocialAccount;
import com.backend.repository.ConnectedSocialAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InstagramService {

    private static final Logger log = LoggerFactory.getLogger(InstagramService.class);

    private final String appId;
    private final String appSecret;
    private final String redirectUri;
    private final String scopes;
    private final String apiVersion = "v19.0";
    private final String graphBase = "https://graph.facebook.com";

    private final ConnectedSocialAccountRepository connectedSocialAccountRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public InstagramService(
            @Value("${meta.app-id:}") String appId,
            @Value("${meta.app-secret:}") String appSecret,
            @Value("${meta.redirect-uri:http://localhost:8083/api/social/instagram/callback}") String redirectUri,
            @Value("${meta.scopes:instagram_basic,instagram_content_publish,pages_show_list,pages_read_engagement}") String scopes,
            ConnectedSocialAccountRepository connectedSocialAccountRepository,
            ObjectMapper objectMapper) {
        String resolvedAppId = appId != null ? appId.trim() : "";
        String resolvedAppSecret = appSecret != null ? appSecret.trim() : "";
        if (resolvedAppId.isBlank() && resolvedAppSecret.contains("_")) {
            String[] parts = resolvedAppSecret.split("_");
            if (parts.length >= 2 && parts[1].matches("\\d+")) {
                resolvedAppId = parts[1];
            }
        }
        if (resolvedAppId.startsWith("LLM_") && resolvedAppId.contains("_")) {
            String[] parts = resolvedAppId.split("_");
            if (parts.length >= 2 && parts[1].matches("\\d+")) {
                resolvedAppId = parts[1];
            }
        }
        this.appId = resolvedAppId;
        this.appSecret = resolvedAppSecret;
        this.redirectUri = redirectUri != null ? redirectUri.trim() : "";
        this.scopes = scopes != null && !scopes.isBlank() ? scopes.trim() : "instagram_basic,instagram_content_publish,pages_show_list,pages_read_engagement";
        this.connectedSocialAccountRepository = connectedSocialAccountRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public boolean isConfigured() {
        return !appId.isBlank() && !appSecret.isBlank();
    }

    public Optional<ConnectedSocialAccount> getConnectedAccount(Long businessId) {
        return connectedSocialAccountRepository.findByBusiness_IdAndPlatform(businessId, "INSTAGRAM");
    }

    public void disconnectAccount(Long businessId) {
        connectedSocialAccountRepository.findByBusiness_IdAndPlatform(businessId, "INSTAGRAM")
                .ifPresent(connectedSocialAccountRepository::delete);
    }

    /**
     * Builds the Meta OAuth authorization URL to initiate login.
     */
    public String buildAuthorizationUrl(String state) {
        if (!isConfigured()) {
            throw new IllegalStateException("Meta App ID and Secret are not configured on the server.");
        }
        return "https://www.facebook.com/" + apiVersion + "/dialog/oauth"
                + "?client_id=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&auth_type=rerequest"
                + (state != null && !state.isBlank() ? "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) : "");
    }

    /**
     * Handles the OAuth callback: exchanges code for tokens, resolves Instagram Business Account,
     * and saves to ConnectedSocialAccount.
     */
    public ConnectedSocialAccount handleOAuthCallback(String code, Business business) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Meta App ID and Secret are not configured.");
        }

        // 1. Exchange code for short-lived access token
        String tokenUrl = graphBase + "/" + apiVersion + "/oauth/access_token"
                + "?client_id=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("[IG-OAuth] Step 1 - Token exchange: HTTP {} -> {}", res.statusCode(), res.body());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to exchange OAuth code: " + res.body());
        }

        JsonNode tokenJson = objectMapper.readTree(res.body());
        String shortLivedToken = tokenJson.path("access_token").asText();

        // 2. Exchange for 60-day Long-Lived User Access Token
        String longLivedUrl = graphBase + "/" + apiVersion + "/oauth/access_token"
                + "?grant_type=fb_exchange_token"
                + "&client_id=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                + "&fb_exchange_token=" + URLEncoder.encode(shortLivedToken, StandardCharsets.UTF_8);

        HttpResponse<String> longLivedRes = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(longLivedUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("[IG-OAuth] Step 2 - Long-lived token: HTTP {} -> {}", longLivedRes.statusCode(), longLivedRes.body());
        String longLivedToken = shortLivedToken;
        long expiresInSeconds = 5184000; // ~60 days
        if (longLivedRes.statusCode() / 100 == 2) {
            JsonNode llJson = objectMapper.readTree(longLivedRes.body());
            if (llJson.has("access_token")) {
                longLivedToken = llJson.path("access_token").asText();
                expiresInSeconds = llJson.path("expires_in").asLong(5184000);
            }
        }

        // 2b. Query /me to verify token and check user identity
        String meUrl = graphBase + "/" + apiVersion + "/me"
                + "?fields=" + URLEncoder.encode("id,name", StandardCharsets.UTF_8)
                + "&access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);
        HttpResponse<String> meRes = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(meUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("[IG-OAuth] Step 2b - /me identity: HTTP {} -> {}", meRes.statusCode(), meRes.body());

        // 2c. Check granted permissions
        String permUrl = graphBase + "/" + apiVersion + "/me/permissions"
                + "?access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);
        HttpResponse<String> permRes = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(permUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("[IG-OAuth] Step 2c - Permissions: HTTP {} -> {}", permRes.statusCode(), permRes.body());

        // 3. Query Facebook Pages managed by user
        String pagesUrl = graphBase + "/" + apiVersion + "/me/accounts?access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);

        HttpResponse<String> pagesRes = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(pagesUrl)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("[IG-OAuth] Step 3 - /me/accounts: HTTP {} -> {}", pagesRes.statusCode(), pagesRes.body());
        if (pagesRes.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to query Facebook pages (HTTP " + pagesRes.statusCode() + "): " + pagesRes.body());
        }

        JsonNode pagesJson = objectMapper.readTree(pagesRes.body());
        JsonNode pagesData = pagesJson.path("data");

        // 4. Find connected Instagram Business Account across pages
        String foundIgAccountId = null;
        String foundIgUsername = null;
        String foundPageId = null;
        String foundPageName = null;
        String pageAccessToken = null;

        // If /me/accounts returned empty (common with granular scopes or New Pages Experience),
        // inspect debug_token to retrieve the exact target_ids granted by the user.
        if (!pagesData.isArray() || pagesData.isEmpty()) {
            log.info("[IG-OAuth] /me/accounts returned empty. Inspecting debug_token for granular scope targets...");
            try {
                String debugTokenUrl = graphBase + "/debug_token"
                        + "?input_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8)
                        + "&access_token=" + URLEncoder.encode(appId + "|" + appSecret, StandardCharsets.UTF_8);

                HttpResponse<String> debugRes = httpClient.send(
                        HttpRequest.newBuilder().uri(URI.create(debugTokenUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                log.info("[IG-OAuth] debug_token response: HTTP {} -> {}", debugRes.statusCode(), debugRes.body());

                if (debugRes.statusCode() / 100 == 2) {
                    JsonNode debugJson = objectMapper.readTree(debugRes.body());
                    JsonNode granularScopes = debugJson.path("data").path("granular_scopes");

                    if (granularScopes.isArray()) {
                        for (JsonNode gs : granularScopes) {
                            String scopeName = gs.path("scope").asText();
                            JsonNode targetIds = gs.path("target_ids");

                            if (targetIds.isArray()) {
                                for (JsonNode tidNode : targetIds) {
                                    String tid = tidNode.asText();

                                    // If target is for Instagram scope
                                    if (scopeName.startsWith("instagram") && foundIgAccountId == null) {
                                        try {
                                            String igDirectUrl = graphBase + "/" + apiVersion + "/" + tid
                                                    + "?fields=id,username&access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);
                                            HttpResponse<String> igDirectRes = httpClient.send(
                                                    HttpRequest.newBuilder().uri(URI.create(igDirectUrl)).GET().build(),
                                                    HttpResponse.BodyHandlers.ofString()
                                            );
                                            log.info("[IG-OAuth] Direct Instagram target {} query: HTTP {} -> {}", tid, igDirectRes.statusCode(), igDirectRes.body());
                                            if (igDirectRes.statusCode() / 100 == 2) {
                                                JsonNode igNode = objectMapper.readTree(igDirectRes.body());
                                                if (igNode.has("id")) {
                                                    foundIgAccountId = igNode.path("id").asText();
                                                    foundIgUsername = igNode.path("username").asText("");
                                                    log.info("[IG-OAuth] Successfully resolved Instagram account from granular target: @{} ({})", foundIgUsername, foundIgAccountId);
                                                }
                                            }
                                        } catch (Exception ex) {
                                            log.warn("[IG-OAuth] Error resolving direct IG target {}: {}", tid, ex.getMessage());
                                        }
                                    }

                                    // If target is for Page scope
                                    if (scopeName.startsWith("pages") && foundPageId == null) {
                                        try {
                                            String pageDirectUrl = graphBase + "/" + apiVersion + "/" + tid
                                                    + "?fields=" + URLEncoder.encode("id,name,access_token,instagram_business_account{id,username}", StandardCharsets.UTF_8)
                                                    + "&access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);
                                            HttpResponse<String> pDirectRes = httpClient.send(
                                                    HttpRequest.newBuilder().uri(URI.create(pageDirectUrl)).GET().build(),
                                                    HttpResponse.BodyHandlers.ofString()
                                            );
                                            log.info("[IG-OAuth] Direct Page target {} query: HTTP {} -> {}", tid, pDirectRes.statusCode(), pDirectRes.body());
                                            if (pDirectRes.statusCode() / 100 == 2) {
                                                JsonNode pNode = objectMapper.readTree(pDirectRes.body());
                                                if (pNode.has("id")) {
                                                    foundPageId = pNode.path("id").asText();
                                                    foundPageName = pNode.path("name").asText();
                                                    if (pNode.has("access_token")) {
                                                        pageAccessToken = pNode.path("access_token").asText();
                                                    }
                                                    JsonNode igOnPage = pNode.path("instagram_business_account");
                                                    if (!igOnPage.isMissingNode() && igOnPage.has("id") && foundIgAccountId == null) {
                                                        foundIgAccountId = igOnPage.path("id").asText();
                                                        foundIgUsername = igOnPage.path("username").asText("");
                                                    }
                                                }
                                            }
                                        } catch (Exception ex) {
                                            log.warn("[IG-OAuth] Error resolving direct Page target {}: {}", tid, ex.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("[IG-OAuth] Error while inspecting debug_token: {}", ex.getMessage());
            }

            // Fallback: If page still not found, try the known Page ID 1287498957782058
            if (foundPageId == null) {
                try {
                    String fallbackPageId = "1287498957782058";
                    String pageFallbackUrl = graphBase + "/" + apiVersion + "/" + fallbackPageId
                            + "?fields=" + URLEncoder.encode("id,name,access_token,instagram_business_account{id,username}", StandardCharsets.UTF_8)
                            + "&access_token=" + URLEncoder.encode(longLivedToken, StandardCharsets.UTF_8);
                    HttpResponse<String> fbRes = httpClient.send(
                            HttpRequest.newBuilder().uri(URI.create(pageFallbackUrl)).GET().build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
                    log.info("[IG-OAuth] Known page fallback query: HTTP {} -> {}", fbRes.statusCode(), fbRes.body());
                    if (fbRes.statusCode() / 100 == 2) {
                        JsonNode pNode = objectMapper.readTree(fbRes.body());
                        if (pNode.has("id")) {
                            foundPageId = pNode.path("id").asText();
                            foundPageName = pNode.path("name").asText("Buildbiz Dev Page");
                            if (pNode.has("access_token")) {
                                pageAccessToken = pNode.path("access_token").asText();
                            }
                            JsonNode igNode = pNode.path("instagram_business_account");
                            if (!igNode.isMissingNode() && igNode.has("id") && foundIgAccountId == null) {
                                foundIgAccountId = igNode.path("id").asText();
                                foundIgUsername = igNode.path("username").asText("");
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("[IG-OAuth] Fallback page check failed: {}", ex.getMessage());
                }
            }

            // If neither page nor instagram account was found anywhere
            if (foundPageId == null && foundIgAccountId == null) {
                throw new IllegalStateException(
                        "No Facebook Pages found. Your Facebook account ('" + objectMapper.readTree(meRes.body()).path("name").asText("unknown") 
                        + "') does not manage any Facebook Pages. Please create a Facebook Page first (facebook.com -> Menu -> Pages -> Create), then try connecting again.");
            }
        } else {

        for (JsonNode page : pagesData) {
            String pId = page.path("id").asText();
            String pName = page.path("name").asText();
            String pToken = page.has("access_token") ? page.path("access_token").asText() : longLivedToken;

            // Check if returned directly in /me/accounts
            JsonNode directIg = page.path("instagram_business_account");
            if (directIg.isMissingNode() || !directIg.has("id")) {
                directIg = page.path("connected_instagram_account");
            }
            if (!directIg.isMissingNode() && directIg.has("id")) {
                foundIgAccountId = directIg.path("id").asText();
                foundIgUsername = directIg.path("username").asText("");
                foundPageId = pId;
                foundPageName = pName;
                pageAccessToken = pToken;
                log.info("Discovered Instagram account directly from /me/accounts: @{} ({}) on page {}", foundIgUsername, foundIgAccountId, pName);
                break;
            }

            // Otherwise query the page specifically
            String igCheckUrl = graphBase + "/" + apiVersion + "/" + pId
                    + "?fields=" + URLEncoder.encode("instagram_business_account{id,username},connected_instagram_account{id,username}", StandardCharsets.UTF_8)
                    + "&access_token=" + URLEncoder.encode(pToken, StandardCharsets.UTF_8);

            try {
                HttpResponse<String> igRes = httpClient.send(
                        HttpRequest.newBuilder().uri(URI.create(igCheckUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                log.info("Page query response for {} ({}): HTTP {} -> {}", pName, pId, igRes.statusCode(), igRes.body());

                if (igRes.statusCode() / 100 == 2) {
                    JsonNode igData = objectMapper.readTree(igRes.body());
                    JsonNode igAccount = igData.path("instagram_business_account");
                    if (igAccount.isMissingNode() || !igAccount.has("id")) {
                        igAccount = igData.path("connected_instagram_account");
                    }
                    if (!igAccount.isMissingNode() && igAccount.has("id")) {
                        foundIgAccountId = igAccount.path("id").asText();
                        foundIgUsername = igAccount.path("username").asText("");
                        foundPageId = pId;
                        foundPageName = pName;
                        pageAccessToken = pToken;
                        log.info("Found connected Instagram account: @{} ({}) on page {}", foundIgUsername, foundIgAccountId, pName);
                        break;
                    }
                }
            } catch (Exception ex) {
                log.warn("Error querying page {} for Instagram accounts: {}", pId, ex.getMessage());
            }

            // Remember first page as fallback
            if (foundPageId == null) {
                foundPageId = pId;
                foundPageName = pName;
                pageAccessToken = pToken;
            }
        }

        // If no Instagram account is attached yet, use the first discovered Facebook Page
        if (foundPageId == null && !pagesData.isEmpty()) {
            JsonNode firstPage = pagesData.get(0);
            foundPageId = firstPage.path("id").asText();
            foundPageName = firstPage.path("name").asText();
            pageAccessToken = firstPage.has("access_token") ? firstPage.path("access_token").asText() : longLivedToken;
        }
    }

        // 5. Persist or update ConnectedSocialAccount
        ConnectedSocialAccount account = connectedSocialAccountRepository
                .findByBusiness_IdAndPlatform(business.getId(), "INSTAGRAM")
                .orElseGet(() -> {
                    ConnectedSocialAccount newAcc = new ConnectedSocialAccount();
                    newAcc.setBusiness(business);
                    newAcc.setPlatform("INSTAGRAM");
                    return newAcc;
                });

        account.setInstagramAccountId(foundIgAccountId);
        account.setInstagramUsername(foundIgUsername);
        account.setFacebookPageId(foundPageId);
        account.setFacebookPageName(foundPageName);
        account.setAccessToken(pageAccessToken != null ? pageAccessToken : longLivedToken);
        account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));

        return connectedSocialAccountRepository.save(account);
    }

    /**
     * Connects an account directly using a Meta Access Token (e.g. from Graph API Explorer or System User).
     * Auto-discovers Pages and linked Instagram accounts, or saves provided details.
     */
    public ConnectedSocialAccount connectWithToken(String token, String manualIgAccountId, String manualIgUsername, Business business) throws Exception {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be empty.");
        }
        String cleanToken = token.trim();

        String foundIgAccountId = manualIgAccountId != null && !manualIgAccountId.isBlank() ? manualIgAccountId.trim() : null;
        String foundIgUsername = manualIgUsername != null && !manualIgUsername.isBlank() ? manualIgUsername.trim() : null;
        String foundPageId = null;
        String foundPageName = null;
        String validToken = cleanToken;

        // Try to auto-discover page and instagram account via /me/accounts
        try {
            String pagesUrl = graphBase + "/" + apiVersion + "/me/accounts"
                    + "?fields=" + URLEncoder.encode("id,name,access_token,instagram_business_account{id,username}", StandardCharsets.UTF_8)
                    + "&access_token=" + URLEncoder.encode(cleanToken, StandardCharsets.UTF_8);

            HttpResponse<String> pagesRes = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(pagesUrl)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (pagesRes.statusCode() / 100 == 2) {
                JsonNode pagesJson = objectMapper.readTree(pagesRes.body());
                JsonNode pagesData = pagesJson.path("data");
                if (pagesData.isArray()) {
                    for (JsonNode page : pagesData) {
                        String pId = page.path("id").asText();
                        String pName = page.path("name").asText();
                        String pToken = page.has("access_token") ? page.path("access_token").asText() : cleanToken;

                        JsonNode igAccount = page.path("instagram_business_account");
                        if (!igAccount.isMissingNode() && igAccount.has("id")) {
                            foundIgAccountId = igAccount.path("id").asText();
                            foundIgUsername = igAccount.path("username").asText("");
                            foundPageId = pId;
                            foundPageName = pName;
                            validToken = pToken;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to auto-discover pages from direct token: {}", ex.getMessage());
        }

        // Try querying /me directly in case it's a Page Access Token
        if (foundIgAccountId == null) {
            try {
                String meUrl = graphBase + "/" + apiVersion + "/me"
                        + "?fields=" + URLEncoder.encode("id,name,instagram_business_account{id,username}", StandardCharsets.UTF_8)
                        + "&access_token=" + URLEncoder.encode(cleanToken, StandardCharsets.UTF_8);

                HttpResponse<String> meRes = httpClient.send(
                        HttpRequest.newBuilder().uri(URI.create(meUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                if (meRes.statusCode() / 100 == 2) {
                    JsonNode meJson = objectMapper.readTree(meRes.body());
                    foundPageId = meJson.path("id").asText(null);
                    foundPageName = meJson.path("name").asText(null);
                    JsonNode igAccount = meJson.path("instagram_business_account");
                    if (!igAccount.isMissingNode() && igAccount.has("id")) {
                        foundIgAccountId = igAccount.path("id").asText();
                        foundIgUsername = igAccount.path("username").asText("");
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to query /me with direct token: {}", ex.getMessage());
            }
        }

        if (foundIgAccountId == null) {
            if (manualIgAccountId != null && !manualIgAccountId.isBlank()) {
                foundIgAccountId = manualIgAccountId.trim();
                foundIgUsername = (manualIgUsername != null && !manualIgUsername.isBlank()) ? manualIgUsername.trim() : "instagram_user";
            } else {
                throw new IllegalStateException("Could not automatically find an Instagram Business Account for this token. Please make sure the token has 'pages_show_list' and 'instagram_basic' / 'instagram_business_basic' permissions, or enter the Instagram Account ID directly.");
            }
        }

        ConnectedSocialAccount account = connectedSocialAccountRepository
                .findByBusiness_IdAndPlatform(business.getId(), "INSTAGRAM")
                .orElseGet(() -> {
                    ConnectedSocialAccount newAcc = new ConnectedSocialAccount();
                    newAcc.setBusiness(business);
                    newAcc.setPlatform("INSTAGRAM");
                    return newAcc;
                });

        account.setInstagramAccountId(foundIgAccountId);
        account.setInstagramUsername(foundIgUsername != null ? foundIgUsername : "instagram_business");
        account.setFacebookPageId(foundPageId);
        account.setFacebookPageName(foundPageName != null ? foundPageName : "Facebook Page");
        account.setAccessToken(validToken);
        account.setTokenExpiresAt(LocalDateTime.now().plusDays(60));

        return connectedSocialAccountRepository.save(account);
    }

    /**
     * Publishes a single image post to Instagram via Meta Graph API 2-step media container.
     */
    public PublishResult publishSingleImage(ConnectedSocialAccount account, String imageUrl, String caption) throws Exception {
        if (imageUrl == null || !imageUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Instagram requires a publicly accessible HTTPS image URL. Upload image to Cloudinary first.");
        }

        String igUserId = account.getInstagramAccountId();
        if (igUserId == null || igUserId.isBlank()) {
            throw new IllegalStateException("Your Facebook Page ('" + (account.getFacebookPageName() != null ? account.getFacebookPageName() : "Page") + "') is connected, but no Instagram Business account is linked to it yet. Please go to Facebook -> Page Settings -> Linked Accounts -> Instagram to connect your profile.");
        }
        String token = account.getAccessToken();

        // Step 1: Create Media Container
        String containerUrl = graphBase + "/" + apiVersion + "/" + igUserId + "/media";
        String postData = "image_url=" + URLEncoder.encode(imageUrl, StandardCharsets.UTF_8)
                + "&caption=" + URLEncoder.encode(caption != null ? caption : "", StandardCharsets.UTF_8)
                + "&access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        HttpRequest containerReq = HttpRequest.newBuilder()
                .uri(URI.create(containerUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> containerRes = httpClient.send(containerReq, HttpResponse.BodyHandlers.ofString());
        if (containerRes.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to create Instagram media container (HTTP " + containerRes.statusCode() + "): " + containerRes.body());
        }

        JsonNode containerJson = objectMapper.readTree(containerRes.body());
        String creationId = containerJson.path("id").asText();
        if (creationId == null || creationId.isBlank()) {
            throw new IllegalStateException("Instagram did not return a creation container ID: " + containerRes.body());
        }

        // Wait brief moment for Instagram to download & process container
        Thread.sleep(2500);

        // Step 2: Publish Media Container
        String publishUrl = graphBase + "/" + apiVersion + "/" + igUserId + "/media_publish";
        String publishData = "creation_id=" + URLEncoder.encode(creationId, StandardCharsets.UTF_8)
                + "&access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        HttpRequest publishReq = HttpRequest.newBuilder()
                .uri(URI.create(publishUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(publishData, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> publishRes = httpClient.send(publishReq, HttpResponse.BodyHandlers.ofString());
        if (publishRes.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to publish Instagram media (HTTP " + publishRes.statusCode() + "): " + publishRes.body());
        }

        JsonNode publishJson = objectMapper.readTree(publishRes.body());
        String mediaId = publishJson.path("id").asText();

        // Step 3: Fetch post permalink
        String permalink = "https://www.instagram.com/";
        try {
            String permalinkUrl = graphBase + "/" + apiVersion + "/" + mediaId
                    + "?fields=permalink&access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
            HttpResponse<String> permalinkRes = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(permalinkUrl)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (permalinkRes.statusCode() / 100 == 2) {
                permalink = objectMapper.readTree(permalinkRes.body()).path("permalink").asText(permalink);
            }
        } catch (Exception ex) {
            log.warn("Could not fetch Instagram permalink for media {}: {}", mediaId, ex.getMessage());
        }

        return new PublishResult(mediaId, permalink);
    }

    /**
     * Publishes a photo post directly to the connected Facebook Page via Meta Graph API.
     */
    public PublishResult publishToFacebookPage(ConnectedSocialAccount account, String imageUrl, String message) throws Exception {
        String pageId = account.getFacebookPageId();
        String token = account.getAccessToken();

        if (pageId == null || pageId.isBlank()) {
            throw new IllegalStateException("No Facebook Page is linked to this account.");
        }

        String postUrl = graphBase + "/" + apiVersion + "/" + pageId + "/photos";
        HttpResponse<String> postRes;

        // Check if image is base64 data URL
        if (imageUrl != null && imageUrl.startsWith("data:")) {
            int commaIdx = imageUrl.indexOf(',');
            String mimeType = "image/jpeg";
            String base64Data = imageUrl;
            if (commaIdx != -1) {
                String header = imageUrl.substring(0, commaIdx);
                if (header.contains("image/png")) mimeType = "image/png";
                else if (header.contains("image/webp")) mimeType = "image/webp";
                base64Data = imageUrl.substring(commaIdx + 1);
            }
            byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data.trim());

            String boundary = "----BuildBizFormBoundary" + System.currentTimeMillis();
            java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();

            // Part 1: message
            byteStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            byteStream.write("Content-Disposition: form-data; name=\"message\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            byteStream.write((message != null ? message : "").getBytes(StandardCharsets.UTF_8));
            byteStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // Part 2: access_token
            byteStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            byteStream.write("Content-Disposition: form-data; name=\"access_token\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            byteStream.write(token.getBytes(StandardCharsets.UTF_8));
            byteStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // Part 3: source (binary file)
            byteStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            byteStream.write(("Content-Disposition: form-data; name=\"source\"; filename=\"photo." 
                    + (mimeType.contains("png") ? "png" : mimeType.contains("webp") ? "webp" : "jpg") + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            byteStream.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            byteStream.write(fileBytes);
            byteStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // End boundary
            byteStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create(postUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray()))
                    .build();

            postRes = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
        } else {
            // Standard public URL (e.g. Cloudinary)
            String postData = "url=" + URLEncoder.encode(imageUrl != null ? imageUrl : "", StandardCharsets.UTF_8)
                    + "&message=" + URLEncoder.encode(message != null ? message : "", StandardCharsets.UTF_8)
                    + "&access_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create(postUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postData, StandardCharsets.UTF_8))
                    .build();

            postRes = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
        }

        if (postRes.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to publish to Facebook Page (HTTP " + postRes.statusCode() + "): " + postRes.body());
        }

        JsonNode postJson = objectMapper.readTree(postRes.body());
        String postId = postJson.path("post_id").asText(postJson.path("id").asText(""));
        String permalink = "https://www.facebook.com/" + (postId != null && !postId.isBlank() ? postId : pageId);

        return new PublishResult(postId, permalink);
    }

    public static class PublishResult {
        private final String mediaId;
        private final String permalink;

        public PublishResult(String mediaId, String permalink) {
            this.mediaId = mediaId;
            this.permalink = permalink;
        }

        public String getMediaId() {
            return mediaId;
        }

        public String getPermalink() {
            return permalink;
        }
    }
}
