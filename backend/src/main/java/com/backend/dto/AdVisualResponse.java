package com.backend.dto;

import java.util.Map;

public class AdVisualResponse {
    private Map<String, String> images;
    private String message;

    public AdVisualResponse() { }
    public AdVisualResponse(Map<String, String> images, String message) { this.images = images; this.message = message; }
    public Map<String, String> getImages() { return images; }
    public void setImages(Map<String, String> images) { this.images = images; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
