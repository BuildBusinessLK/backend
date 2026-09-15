package com.backend.dto.email;

public class EmailGenerateResponse {
    private String subject;
    private String body;
    private String suggestedCallToAction;
    private String targetAudienceNotes;

    public EmailGenerateResponse() {}

    public EmailGenerateResponse(String subject, String body, String suggestedCallToAction, String targetAudienceNotes) {
        this.subject = subject;
        this.body = body;
        this.suggestedCallToAction = suggestedCallToAction;
        this.targetAudienceNotes = targetAudienceNotes;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getSuggestedCallToAction() { return suggestedCallToAction; }
    public void setSuggestedCallToAction(String suggestedCallToAction) { this.suggestedCallToAction = suggestedCallToAction; }

    public String getTargetAudienceNotes() { return targetAudienceNotes; }
    public void setTargetAudienceNotes(String targetAudienceNotes) { this.targetAudienceNotes = targetAudienceNotes; }
}
