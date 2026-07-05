package com.backend.dto.ai;

import java.util.Map;

public class AdGenerationRequest {

    private String prompt;
    private Map<String, Object> businessProfile;
    private Map<String, Object> userProfile;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
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
