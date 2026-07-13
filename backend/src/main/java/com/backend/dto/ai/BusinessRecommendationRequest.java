package com.backend.dto.ai;

import java.util.Map;

public class BusinessRecommendationRequest {

    private Long sessionId;
    private Map<String, Object> userProfile;
    private Map<String, Object> businessProfile;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Object> getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Map<String, Object> userProfile) {
        this.userProfile = userProfile;
    }

    public Map<String, Object> getBusinessProfile() {
        return businessProfile;
    }

    public void setBusinessProfile(Map<String, Object> businessProfile) {
        this.businessProfile = businessProfile;
    }
}
