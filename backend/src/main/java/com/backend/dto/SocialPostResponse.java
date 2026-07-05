package com.backend.dto;

public class SocialPostResponse {

    private String platform;
    private String hook;
    private String caption;
    private String hashtags;

    public SocialPostResponse() {
    }

    public SocialPostResponse(String platform, String hook, String caption, String hashtags) {
        this.platform = platform;
        this.hook = hook;
        this.caption = caption;
        this.hashtags = hashtags;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }
}
