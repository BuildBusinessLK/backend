package com.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AdVisualRequest {
    @NotBlank private String idea;
    private String generatedAds;
    private String imageDataUrl;
    private String instruction;

    public String getIdea() { return idea; }
    public void setIdea(String idea) { this.idea = idea; }
    public String getGeneratedAds() { return generatedAds; }
    public void setGeneratedAds(String generatedAds) { this.generatedAds = generatedAds; }
    public String getImageDataUrl() { return imageDataUrl; }
    public void setImageDataUrl(String imageDataUrl) { this.imageDataUrl = imageDataUrl; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
