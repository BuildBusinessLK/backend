package com.backend.service;

import com.backend.dto.SmePublishRequest;
import com.backend.dto.SmePublishResponse;
import com.backend.dto.SmePublicSiteResponse;
import com.backend.entity.SmeHostedSite;
import com.backend.repository.SmeHostedSiteRepository;
import com.backend.util.SmeSlugUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class SmeHostedSiteService {

    private final SmeHostedSiteRepository repository;
    private final ObjectMapper objectMapper;

    public SmeHostedSiteService(SmeHostedSiteRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SmePublishResponse publish(SmePublishRequest request) throws JsonProcessingException {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        String slug = SmeSlugUtil.normalize(request.getSlug() != null ? request.getSlug() : "");
        if (slug.length() < 2 || slug.length() > 80) {
            throw new IllegalArgumentException("URL slug must be 2–80 characters (lowercase letters, numbers, hyphens).");
        }
        if (SmeSlugUtil.isReserved(slug)) {
            throw new IllegalArgumentException("That URL is reserved. Choose a different slug.");
        }

        String html = request.getPreviewHtml();
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("previewHtml is required — generate your site first.");
        }

        Map<String, Object> manifestMap = request.getManifest() != null ? request.getManifest() : Map.of();
        String manifestJson = objectMapper.writeValueAsString(manifestMap);

        String business = request.getBusinessName();
        if (business == null || business.isBlank()) {
            Object bn = manifestMap.get("businessName");
            business = bn != null ? bn.toString() : "";
        }

        Optional<SmeHostedSite> existing = repository.findBySlug(slug);
        SmeHostedSite site = existing.orElseGet(SmeHostedSite::new);
        boolean updated = existing.isPresent();

        site.setSlug(slug);
        site.setBusinessName(business);
        site.setTemplateKey(request.getTemplateKey());
        site.setManifestJson(manifestJson);
        site.setHtmlSnapshot(html);

        repository.save(site);

        SmePublishResponse response = new SmePublishResponse();
        response.setSlug(slug);
        response.setPublicPath("/business/" + slug);
        response.setUpdatedExisting(updated);
        return response;
    }

    public Optional<SmePublicSiteResponse> getPublicBySlug(String rawSlug) {
        String slug = SmeSlugUtil.normalize(rawSlug != null ? rawSlug : "");
        if (slug.length() < 2) {
            return Optional.empty();
        }
        return repository.findBySlug(slug).map(this::toPublicDto);
    }

    private SmePublicSiteResponse toPublicDto(SmeHostedSite site) {
        SmePublicSiteResponse dto = new SmePublicSiteResponse();
        dto.setSlug(site.getSlug());
        dto.setBusinessName(site.getBusinessName());
        dto.setTemplateKey(site.getTemplateKey());
        dto.setHtml(site.getHtmlSnapshot());
        return dto;
    }
}
