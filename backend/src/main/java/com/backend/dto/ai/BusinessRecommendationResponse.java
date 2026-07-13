package com.backend.dto.ai;

public class BusinessRecommendationResponse {

    private String recommendedBusiness;
    private String guidance;
    private String message;

    public String getRecommendedBusiness() {
        return recommendedBusiness;
    }

    public void setRecommendedBusiness(String recommendedBusiness) {
        this.recommendedBusiness = recommendedBusiness;
    }

    public String getGuidance() {
        return guidance;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
