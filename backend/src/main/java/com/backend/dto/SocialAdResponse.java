package com.backend.dto;

public class SocialAdResponse {

    private String headline;
    private String description;
    private String cta;

    public SocialAdResponse() {
    }

    public SocialAdResponse(String headline, String description, String cta) {
        this.headline = headline;
        this.description = description;
        this.cta = cta;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }
}
