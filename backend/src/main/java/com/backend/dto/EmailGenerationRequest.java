package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class EmailGenerationRequest {
    
    @NotBlank(message = "Idea cannot be blank")
    private String idea;
private String userName;
private String companyName;
private String industry;
private String targetAudience;
private String tone;
private String signature;
private String businessDescription;
private String marketingGoals;
    
    public EmailGenerationRequest() {
    }
    
    public EmailGenerationRequest(String idea) {
        this.idea = idea;
    }
    
    public String getIdea() {
        return idea;
    }
    
    public void setIdea(String idea) {
        this.idea = idea;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getIndustry() {
        return industry;
    }
    
    public void setIndustry(String industry) {
        this.industry = industry;
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
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getMarketingGoals() {
        return marketingGoals;
    }

    public void setMarketingGoals(String marketingGoals) {
        this.marketingGoals = marketingGoals;
    }
}
