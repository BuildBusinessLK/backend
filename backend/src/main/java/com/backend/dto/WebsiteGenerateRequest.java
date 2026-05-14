package com.backend.dto;

public class WebsiteGenerateRequest {
    private String prompt;
    private Boolean useFallback;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Boolean getUseFallback() {
        return useFallback;
    }

    public void setUseFallback(Boolean useFallback) {
        this.useFallback = useFallback;
    }
}
