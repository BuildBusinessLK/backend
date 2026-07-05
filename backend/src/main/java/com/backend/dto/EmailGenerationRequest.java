package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class EmailGenerationRequest {
    
    @NotBlank(message = "Idea cannot be blank")
    private String idea;
    
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
}
