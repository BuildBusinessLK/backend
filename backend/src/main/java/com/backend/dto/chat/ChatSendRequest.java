package com.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;

public class ChatSendRequest {

    private Long sessionId;

    @NotBlank
    private String question;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
