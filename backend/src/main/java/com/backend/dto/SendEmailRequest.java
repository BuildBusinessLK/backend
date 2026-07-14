package com.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class SendEmailRequest {
    
    @NotEmpty(message = "Select at least one recipient group")
    private List<String> groupIds;
    
    @NotBlank(message = "Subject cannot be blank")
    private String subject;
    
    @NotBlank(message = "Body cannot be blank")
    private String body;
    
    public SendEmailRequest() {
    }
    
    public SendEmailRequest(List<String> groupIds, String subject, String body) {
        this.groupIds = groupIds;
        this.subject = subject;
        this.body = body;
    }
    
    public List<String> getGroupIds() {
        return groupIds;
    }
    
    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
}
