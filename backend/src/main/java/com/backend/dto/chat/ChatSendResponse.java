package com.backend.dto.chat;

import com.backend.dto.ai.BusinessRecommendationResponse;

public class ChatSendResponse {

    private String type;
    private String message;
    private String action;
    private Long sessionId;
    private Long messageId;
    private BusinessRecommendationResponse recommendation;
    private java.util.Map<String, Object> emailCampaign;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public BusinessRecommendationResponse getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(BusinessRecommendationResponse recommendation) {
        this.recommendation = recommendation;
    }

    public java.util.Map<String, Object> getEmailCampaign() {
        return emailCampaign;
    }

    public void setEmailCampaign(java.util.Map<String, Object> emailCampaign) {
        this.emailCampaign = emailCampaign;
    }
}
