package com.backend.dto.social;

public class SocialGenerateRequest {
    private String prompt;
    private String idea;
    private String platform = "instagram";
    private String tone = "professional";

    public SocialGenerateRequest() {}

    public String getPrompt() {
        return prompt != null && !prompt.isBlank() ? prompt : idea;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getPlatform() {
        return platform != null && !platform.isBlank() ? platform : "instagram";
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTone() {
        return tone != null && !tone.isBlank() ? tone : "professional";
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
