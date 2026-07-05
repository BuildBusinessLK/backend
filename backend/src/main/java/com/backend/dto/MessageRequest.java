package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageRequest {
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    public MessageRequest() {
    }
    
    public MessageRequest(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
