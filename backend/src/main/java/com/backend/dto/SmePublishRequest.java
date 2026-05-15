package com.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class SmePublishRequest {

    /**
     * Desired public path segment: /business/{slug}. Normalized server-side.
     */
    private String slug;
    private String previewHtml;
    /** forest | ocean | amber */
    private String templateKey;
    private Map<String, Object> manifest = new HashMap<>();
    private String businessName;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPreviewHtml() {
        return previewHtml;
    }

    public void setPreviewHtml(String previewHtml) {
        this.previewHtml = previewHtml;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public Map<String, Object> getManifest() {
        return manifest;
    }

    public void setManifest(Map<String, Object> manifest) {
        this.manifest = manifest != null ? manifest : new HashMap<>();
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}
