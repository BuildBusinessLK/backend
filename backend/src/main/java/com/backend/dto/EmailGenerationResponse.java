package com.backend.dto;

public class EmailGenerationResponse {
    
    private EmailContent email;
    
    public EmailGenerationResponse() {
    }
    
    public EmailGenerationResponse(EmailContent email) {
        this.email = email;
    }
    
    public EmailContent getEmail() {
        return email;
    }
    
    public void setEmail(EmailContent email) {
        this.email = email;
    }
}
