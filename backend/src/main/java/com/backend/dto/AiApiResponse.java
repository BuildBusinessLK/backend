package com.backend.dto;

public class AiApiResponse {
    private boolean success;
    private String answer;
    private String message;
    private String conversationId;
    private int code;

    public AiApiResponse() {
    }

    public AiApiResponse(boolean success, String answer, String message, String conversationId, int code) {
        this.success = success;
        this.answer = answer;
        this.message = message;
        this.conversationId = conversationId;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
