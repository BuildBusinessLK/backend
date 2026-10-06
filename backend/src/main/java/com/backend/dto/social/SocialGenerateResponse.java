package com.backend.dto.social;

import java.util.ArrayList;
import java.util.List;

public class SocialGenerateResponse {
    private Long id;
    private String platform;
    private String headline;
    private String caption;
    private String callToAction;
    private List<String> hashtags = new ArrayList<>();
    private String imagePrompt;
    private String imageUrl;
    private boolean cloudImage = false;
    private String status = "DRAFT";
    private String generatedAds;
    private String message;

    public SocialGenerateResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags != null ? hashtags : new ArrayList<>();
    }

    public String getImagePrompt() {
        return imagePrompt;
    }

    public void setImagePrompt(String imagePrompt) {
        this.imagePrompt = imagePrompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isCloudImage() {
        return cloudImage;
    }

    public void setCloudImage(boolean cloudImage) {
        this.cloudImage = cloudImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGeneratedAds() {
        return generatedAds;
    }

    public void setGeneratedAds(String generatedAds) {
        this.generatedAds = generatedAds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
