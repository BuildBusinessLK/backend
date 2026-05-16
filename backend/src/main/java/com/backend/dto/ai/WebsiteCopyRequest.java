package com.backend.dto.ai;

import java.util.Map;

public class WebsiteCopyRequest {
    private Map<String, Object> businessProfile;

    public Map<String, Object> getBusinessProfile() {
        return businessProfile;
    }

    public void setBusinessProfile(Map<String, Object> businessProfile) {
        this.businessProfile = businessProfile;
    }
}
