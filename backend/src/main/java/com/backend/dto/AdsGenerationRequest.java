package com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdsGenerationRequest {

    @NotBlank(message = "Idea cannot be blank")
    private String idea;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty("target_audience")
    private String targetAudience;

    private String tone;
    private String platform;
    private String website;

    public AdsGenerationRequest() {}

    public AdsGenerationRequest(String idea, String productType, String targetAudience, String tone) {
        this.idea = idea;
        this.productType = productType;
        this.targetAudience = targetAudience;
        this.tone = tone;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}