package com.backend.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {
    private String sessionId;
    private List<ChatMessage> conversation;
    private String assistantReply;
    private Instant generatedAt;
    private String briefSummary;
    private List<String> quickReplies;

    public ChatResponse() {}

    public ChatResponse(String sessionId, List<ChatMessage> conversation, String assistantReply, Instant generatedAt) {
        this.sessionId = sessionId;
        this.conversation = conversation;
        this.assistantReply = assistantReply;
        this.generatedAt = generatedAt;
    }

    public ChatResponse(String sessionId, List<ChatMessage> conversation, String assistantReply, Instant generatedAt, String briefSummary, List<String> quickReplies) {
        this.sessionId = sessionId;
        this.conversation = conversation;
        this.assistantReply = assistantReply;
        this.generatedAt = generatedAt;
        this.briefSummary = briefSummary;
        this.quickReplies = quickReplies;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<ChatMessage> getConversation() {
        return conversation;
    }

    public void setConversation(List<ChatMessage> conversation) {
        this.conversation = conversation;
    }

    public String getAssistantReply() {
        return assistantReply;
    }

    public void setAssistantReply(String assistantReply) {
        this.assistantReply = assistantReply;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getBriefSummary() {
        return briefSummary;
    }

    public void setBriefSummary(String briefSummary) {
        this.briefSummary = briefSummary;
    }

    public List<String> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
    }
}
