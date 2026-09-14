package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessRecommendationResponse {

    private String modelVersion;
    private String recommendedBusiness;
    private List<RecommendationDto> recommendations = new ArrayList<>();
    private FeasibilityDto feasibility;
    private String guidance;
    private List<String> actions = new ArrayList<>();
    private String message;
    private Long sessionId;

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getRecommendedBusiness() {
        return recommendedBusiness;
    }

    public void setRecommendedBusiness(String recommendedBusiness) {
        this.recommendedBusiness = recommendedBusiness;
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public FeasibilityDto getFeasibility() {
        return feasibility;
    }

    public void setFeasibility(FeasibilityDto feasibility) {
        this.feasibility = feasibility;
    }

    public String getGuidance() {
        return guidance;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
