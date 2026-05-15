package com.backend.service;

import com.backend.dto.SmeContactDto;
import com.backend.dto.SmeProductItem;
import com.backend.dto.SmeSocialDto;
import com.backend.dto.SmeWebsiteGenerateRequest;
import com.backend.dto.SmeWebsiteGenerateResponse;
import com.backend.dto.WebsiteGenerateRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmeWebsiteOrchestrationService {

    private final WebsiteGeneratorService websiteGeneratorService;

    public SmeWebsiteOrchestrationService(WebsiteGeneratorService websiteGeneratorService) {
        this.websiteGeneratorService = websiteGeneratorService;
    }

    public SmeWebsiteGenerateResponse generate(SmeWebsiteGenerateRequest req) {
        SmeWebsiteGenerateResponse out = new SmeWebsiteGenerateResponse();
        out.setSuggestedSubdomain(SmeModernSiteRenderer.suggestedSubdomain(req));
        out.setManifest(buildManifest(req));

        boolean forceTemplate =
                Boolean.TRUE.equals(req.getUseFallback()) || Boolean.FALSE.equals(req.getUseAi());

        if (!forceTemplate) {
            WebsiteGenerateRequest wg = new WebsiteGenerateRequest();
            wg.setPrompt(buildAiPrompt(req));
            wg.setUseFallback(false);
            Map<String, Object> aiBody = websiteGeneratorService.generateWebsite(wg);

            if (!aiBody.containsKey("error")) {
                boolean legacyFallback = Boolean.TRUE.equals(aiBody.get("fallback"));
                String text = GeminiWebsiteBundleParser.extractTextFromGeminiResponse(aiBody);
                Map<String, String> bundle = GeminiWebsiteBundleParser.parseBundle(text);

                if (!legacyFallback && GeminiWebsiteBundleParser.bundleLooksUsable(bundle)) {
                    out.setBundle(bundle);
                    out.setPreviewHtml(SmeModernSiteRenderer.composePreviewDocument(bundle));
                    out.setSource("ai");
                    out.setFallback(false);
                    if (aiBody.get("message") != null) {
                        out.setMessage(String.valueOf(aiBody.get("message")));
                    }
                    return out;
                }
            }
        }

        Map<String, String> templateBundle = SmeModernSiteRenderer.renderBundle(req);
        out.setBundle(templateBundle);
        out.setPreviewHtml(SmeModernSiteRenderer.composePreviewDocument(templateBundle));
        out.setSource("template");
        out.setFallback(forceTemplate);
        if (!forceTemplate) {
            out.setMessage("AI output was not usable; rendered a deterministic SME template instead.");
        }
        return out;
    }

    private static Map<String, Object> buildManifest(SmeWebsiteGenerateRequest req) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("businessName", req.getBusinessName());
        m.put("tagline", req.getTagline());
        m.put("industry", req.getIndustry());
        m.put("about", req.getAbout());
        m.put("theme", req.getTheme());

        List<Map<String, String>> products = new ArrayList<>();
        if (req.getProducts() != null) {
            for (SmeProductItem p : req.getProducts()) {
                if (p == null) {
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                row.put("name", p.getName());
                row.put("description", p.getDescription());
                products.add(row);
            }
        }
        m.put("products", products);

        Map<String, String> contact = new LinkedHashMap<>();
        SmeContactDto c = req.getContact();
        if (c != null) {
            contact.put("email", c.getEmail());
            contact.put("phone", c.getPhone());
            contact.put("address", c.getAddress());
        }
        m.put("contact", contact);

        Map<String, String> social = new LinkedHashMap<>();
        SmeSocialDto s = req.getSocial();
        if (s != null) {
            social.put("facebook", s.getFacebook());
            social.put("instagram", s.getInstagram());
            social.put("youtube", s.getYoutube());
            social.put("linkedin", s.getLinkedin());
            social.put("tiktok", s.getTiktok());
        }
        m.put("social", social);
        m.put("galleryImageUrls", req.getGalleryImageUrls() == null ? List.of() : req.getGalleryImageUrls());
        return m;
    }

    private static String buildAiPrompt(SmeWebsiteGenerateRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You design lightweight marketing sites for Sri Lankan SMEs (Coconut, Kithul, Palmyrah).
                Return ONE complete HTML5 document only, inside a single markdown code fence labeled html.
                Put all CSS in a <style> tag in <head> and all JS in one <script> before </body>.
                No separate file outputs. Use modern, clean layout; responsive; good contrast; system fonts.
                Include: sticky top bar with business name, hero, about, product/service cards, gallery grid,
                visible contact details, inquiry form with front-end validation only, footer with social links.
                Use the user's facts faithfully; short copy; no lorem ipsum; no emojis.

                Business Name: %s
                Tagline: %s
                Industry focus: %s
                About: %s
                Theme hint: %s

                """.formatted(
                safe(r.getBusinessName()),
                safe(r.getTagline()),
                safe(r.getIndustry()),
                safe(r.getAbout()),
                safe(r.getTheme())));

        sb.append("Products / services:\n");
        if (r.getProducts() == null || r.getProducts().isEmpty()) {
            sb.append("- (none supplied — invent 2 plausible offerings)\n");
        } else {
            for (SmeProductItem p : r.getProducts()) {
                if (p == null) {
                    continue;
                }
                sb.append("- ").append(safe(p.getName())).append(": ").append(safe(p.getDescription())).append('\n');
            }
        }

        SmeContactDto c = r.getContact();
        sb.append("\nContact:\n");
        if (c != null) {
            sb.append("- Email: ").append(safe(c.getEmail())).append('\n');
            sb.append("- Phone: ").append(safe(c.getPhone())).append('\n');
            sb.append("- Address: ").append(safe(c.getAddress())).append('\n');
        }

        SmeSocialDto s = r.getSocial();
        sb.append("\nSocial URLs (only output anchors for non-empty):\n");
        if (s != null) {
            sb.append("- Facebook: ").append(safe(s.getFacebook())).append('\n');
            sb.append("- Instagram: ").append(safe(s.getInstagram())).append('\n');
            sb.append("- YouTube: ").append(safe(s.getYoutube())).append('\n');
            sb.append("- LinkedIn: ").append(safe(s.getLinkedin())).append('\n');
            sb.append("- TikTok: ").append(safe(s.getTiktok())).append('\n');
        }

        sb.append("\nGallery image URLs (use as <img src>):\n");
        if (r.getGalleryImageUrls() == null || r.getGalleryImageUrls().isEmpty()) {
            sb.append("- (none — pick two tasteful Unsplash URLs related to agriculture / ingredients)\n");
        } else {
            for (String u : r.getGalleryImageUrls()) {
                sb.append("- ").append(safe(u)).append('\n');
            }
        }

        sb.append("""
                \nOutput:
                ```html
                ... full document ...
                ```
                """);
        return sb.toString();
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}
