package com.backend.dto.ai;

import java.util.List;
import java.util.Map;

public class AiChatRequest {

    private String question;
    private List<Map<String, String>> chatHistory;
    private Map<String, Object> userProfile;
    private Map<String, Object> businessProfile;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Map<String, String>> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<Map<String, String>> chatHistory) {
        this.chatHistory = chatHistory;
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
