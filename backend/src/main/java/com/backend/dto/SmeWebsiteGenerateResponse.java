package com.backend.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmeWebsiteGenerateResponse {

    private Map<String, Object> manifest = new LinkedHashMap<>();
    private Map<String, String> bundle = new LinkedHashMap<>();
    private String previewHtml;
    private String suggestedSubdomain;
    /** ai | template */
    private String source;
    private boolean fallback;
    private String message;

    public Map<String, Object> getManifest() {
        return manifest;
    }

    public void setManifest(Map<String, Object> manifest) {
        this.manifest = manifest;
    }

    public Map<String, String> getBundle() {
        return bundle;
    }

    public void setBundle(Map<String, String> bundle) {
        this.bundle = bundle;
    }

    public String getPreviewHtml() {
        return previewHtml;
    }

    public void setPreviewHtml(String previewHtml) {
        this.previewHtml = previewHtml;
    }

    public String getSuggestedSubdomain() {
        return suggestedSubdomain;
    }

    public void setSuggestedSubdomain(String suggestedSubdomain) {
        this.suggestedSubdomain = suggestedSubdomain;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
