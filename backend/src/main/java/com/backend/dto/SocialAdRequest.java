package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SocialAdRequest {

    @NotBlank(message = "Idea cannot be blank")
    private String idea;

    private String tone;

    private String platform;

    public SocialAdRequest() {
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
