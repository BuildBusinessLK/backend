package com.backend.dto.email;

import java.util.Map;

public class EmailGenerateRequest {
    private String goal = "GENERAL_ANNOUNCEMENT";
    private String sector;
    private String productName;
    private String targetAudience;
    private String keyOffer;
    private String tone = "professional";
    private String companyName;
    private String userName;
    private String contactPhone;
    private String contactEmail;
    private String idea;
    private Map<String, Object> businessProfile;
    private Map<String, Object> userProfile;

    public EmailGenerateRequest() {}

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getKeyOffer() { return keyOffer; }
    public void setKeyOffer(String keyOffer) { this.keyOffer = keyOffer; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getIdea() { return idea; }
    public void setIdea(String idea) { this.idea = idea; }

    public Map<String, Object> getBusinessProfile() { return businessProfile; }
    public void setBusinessProfile(Map<String, Object> businessProfile) { this.businessProfile = businessProfile; }

    public Map<String, Object> getUserProfile() { return userProfile; }
    public void setUserProfile(Map<String, Object> userProfile) { this.userProfile = userProfile; }
}
