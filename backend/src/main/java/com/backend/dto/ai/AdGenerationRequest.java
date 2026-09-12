package com.backend.dto.ai;

import java.util.Map;

public class AdGenerationRequest {

    private String prompt;
    private String idea;
    private String tone;
    private String platform;
    private String website;
    private Map<String, Object> businessProfile;
    private Map<String, Object> userProfile;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Map<String, Object> getBusinessProfile() {
        return businessProfile;
    }

    public void setBusinessProfile(Map<String, Object> businessProfile) {
        this.businessProfile = businessProfile;
    }

    public Map<String, Object> getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Map<String, Object> userProfile) {
        this.userProfile = userProfile;
    }
}