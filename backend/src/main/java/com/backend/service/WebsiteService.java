package com.backend.service;

import com.backend.domain.WebsiteStatus;
import com.backend.dto.ai.WebsiteCopyRequest;
import com.backend.dto.ai.WebsiteCopyResponse;
import com.backend.dto.website.GeneratedWebsiteDto;
import com.backend.dto.website.WebsiteGenerateRequest;
import com.backend.entity.Business;
import com.backend.entity.GeneratedWebsite;
import com.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebsiteService {

    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProductRepository businessProductRepository;
    private final BusinessSocialLinkRepository businessSocialLinkRepository;
    private final GeneratedWebsiteRepository generatedWebsiteRepository;
    private final AiClientService aiClientService;

    @Value("${app.public-site.base-url:http://localhost:3001}")
    private String publicSiteBaseUrl;

    public WebsiteService(
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessProductRepository businessProductRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            GeneratedWebsiteRepository generatedWebsiteRepository,
            AiClientService aiClientService) {
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessProductRepository = businessProductRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.generatedWebsiteRepository = generatedWebsiteRepository;
        this.aiClientService = aiClientService;
    }

    @Transactional(readOnly = true)
    public GeneratedWebsiteDto getLatestForBusiness(Long userId, Long businessId) {
        Business b = businessRepository.findByIdAndOwner_Id(businessId, userId).orElseThrow();
        return generatedWebsiteRepository
                .findFirstByBusiness_IdOrderByUpdatedAtDesc(b.getId())
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public GeneratedWebsiteDto generate(Long userId, WebsiteGenerateRequest req) {
        Business b = businessRepository.findByIdAndOwner_Id(req.getBusinessId(), userId).orElseThrow();

        WebsiteCopyRequest copyReq = new WebsiteCopyRequest();
        copyReq.setBusinessProfile(buildBusinessSnapshot(b));

        WebsiteCopyResponse copy;
        try {
            copy = aiClientService.requestWebsiteCopy(copyReq);
        } catch (Exception e) {
            copy = new WebsiteCopyResponse();
            copy.setHeroText("Welcome to " + b.getBusinessName());
            copy.setAboutText(
                    businessProfileRepository
                            .findByBusiness_Id(b.getId())
                            .map(p -> p.getBusinessDescription() != null
                                    ? p.getBusinessDescription()
                                    : "Quality products from Sri Lanka.")
                            .orElse("Quality products from Sri Lanka."));
            copy.setMarketingText("Discover our range and get in touch today.");
        }

        GeneratedWebsite gw = new GeneratedWebsite();
        gw.setBusiness(b);
        String tid = req.getTemplateId();
        if (tid == null || tid.isBlank()) {
            tid = "modern-business-v1";
        }
        gw.setTemplateId(tid);
        gw.setHeroText(trimToNull(copy.getHeroText()));
        gw.setAboutText(trimToNull(copy.getAboutText()));
        gw.setMarketingText(trimToNull(copy.getMarketingText()));
        gw.setPrimaryColor(trimToNull(req.getPrimaryColor()));
        gw.setSecondaryColor(trimToNull(req.getSecondaryColor()));
        gw.setLogoUrl(trimToNull(req.getLogoUrl()));
        gw.setCoverImageUrl(trimToNull(req.getCoverImageUrl()));
        gw.setContactEmail(trimToNull(req.getContactEmail()));
        gw.setPhone(trimToNull(req.getPhone()));
        gw.setStatus(WebsiteStatus.DRAFT);
        gw = generatedWebsiteRepository.save(gw);
        return toDto(gw);
    }

    @Transactional
    public GeneratedWebsiteDto publish(Long userId, Long websiteId) {
        GeneratedWebsite gw = generatedWebsiteRepository.findById(websiteId).orElseThrow();
        Business b = gw.getBusiness();
        if (!b.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Forbidden");
        }
        String base = publicSiteBaseUrl.replaceAll("/$", "");
        String slug = b.getWebsiteSlug();
        gw.setPublishedUrl(base + "/business/" + slug);
        gw.setStatus(WebsiteStatus.PUBLISHED);
        gw = generatedWebsiteRepository.save(gw);
        return toDto(gw);
    }

    @Transactional
    public GeneratedWebsiteDto update(Long userId, Long websiteId, com.backend.dto.website.WebsiteUpdateRequest req) {
        GeneratedWebsite gw = generatedWebsiteRepository.findById(websiteId).orElseThrow();
        Business b = gw.getBusiness();
        if (!b.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Forbidden");
        }
        if (req.getHeroText() != null) {
            gw.setHeroText(trimToNull(req.getHeroText()));
        }
        if (req.getAboutText() != null) {
            gw.setAboutText(trimToNull(req.getAboutText()));
        }
        if (req.getMarketingText() != null) {
            gw.setMarketingText(trimToNull(req.getMarketingText()));
        }
        if (req.getPrimaryColor() != null) {
            gw.setPrimaryColor(trimToNull(req.getPrimaryColor()));
        }
        if (req.getSecondaryColor() != null) {
            gw.setSecondaryColor(trimToNull(req.getSecondaryColor()));
        }
        if (req.getLogoUrl() != null) {
            gw.setLogoUrl(trimToNull(req.getLogoUrl()));
        }
        if (req.getCoverImageUrl() != null) {
            gw.setCoverImageUrl(trimToNull(req.getCoverImageUrl()));
        }
        if (req.getContactEmail() != null) {
            gw.setContactEmail(trimToNull(req.getContactEmail()));
        }
        if (req.getPhone() != null) {
            gw.setPhone(trimToNull(req.getPhone()));
        }
        if (req.getTemplateId() != null && !req.getTemplateId().isBlank()) {
            gw.setTemplateId(req.getTemplateId().trim());
        }
        gw = generatedWebsiteRepository.save(gw);
        return toDto(gw);
    }

    private Map<String, Object> buildBusinessSnapshot(Business b) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("hasBusiness", true);
        root.put("businessName", b.getBusinessName());
        root.put("sector", b.getSector().name());
        root.put("websiteSlug", b.getWebsiteSlug());
        businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(bp -> {
            root.put("businessDescription", bp.getBusinessDescription());
            root.put("targetMarket", bp.getTargetMarket());
            root.put("monthlyIncome", bp.getMonthlyIncome());
            root.put("monthlyProduction", bp.getMonthlyProduction());
            root.put("marketingGoals", bp.getMarketingGoals());
        });
        List<Map<String, Object>> products =
                businessProductRepository.findByBusiness_IdOrderByIdAsc(b.getId()).stream()
                        .map(p -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("name", p.getName());
                            m.put("description", p.getDescription());
                            m.put("price", p.getPrice());
                            m.put("category", p.getCategory());
                            return m;
                        })
                        .toList();
        root.put("products", products);
        List<Map<String, Object>> social =
                businessSocialLinkRepository.findByBusiness_IdOrderByIdAsc(b.getId()).stream()
                        .map(s -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("platform", s.getPlatform());
                            m.put("url", s.getUrl());
                            return m;
                        })
                        .toList();
        root.put("socialLinks", social);
        return root;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private GeneratedWebsiteDto toDto(GeneratedWebsite g) {
        GeneratedWebsiteDto d = new GeneratedWebsiteDto();
        d.setId(g.getId());
        d.setBusinessId(g.getBusiness().getId());
        d.setTemplateId(g.getTemplateId());
        d.setHeroText(g.getHeroText());
        d.setAboutText(g.getAboutText());
        d.setMarketingText(g.getMarketingText());
        d.setPrimaryColor(g.getPrimaryColor());
        d.setSecondaryColor(g.getSecondaryColor());
        d.setLogoUrl(g.getLogoUrl());
        d.setCoverImageUrl(g.getCoverImageUrl());
        d.setContactEmail(g.getContactEmail());
        d.setPhone(g.getPhone());
        
        String url = g.getPublishedUrl();
        if (url != null && publicSiteBaseUrl != null && !publicSiteBaseUrl.isBlank()) {
            String base = publicSiteBaseUrl.replaceAll("/$", "");
            url = url.replaceFirst("^https?://localhost:3001", base);
        }
        d.setPublishedUrl(url);
        d.setStatus(g.getStatus());
        return d;
    }
}
