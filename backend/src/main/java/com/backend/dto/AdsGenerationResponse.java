package com.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdsGenerationResponse {
    
    private String prompt;
    
    @JsonProperty("generated_ads")
    private String generatedAds;
    
    @JsonProperty("share_links")
    private ShareLinks shareLinks;
    
    private String status;
    
    private String message;

    public AdsGenerationResponse() {}

    public AdsGenerationResponse(String prompt, String generatedAds, ShareLinks shareLinks, String status, String message) {
        this.prompt = prompt;
        this.generatedAds = generatedAds;
        this.shareLinks = shareLinks;
        this.status = status;
        this.message = message;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getGeneratedAds() {
        return generatedAds;
    }

    public void setGeneratedAds(String generatedAds) {
        this.generatedAds = generatedAds;
    }

    public ShareLinks getShareLinks() {
        return shareLinks;
    }

    public void setShareLinks(ShareLinks shareLinks) {
        this.shareLinks = shareLinks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Inner class for share links
    public static class ShareLinks {
        private String facebook;
        private String instagram;
        private String tiktok;
        private String whatsapp;

        public ShareLinks() {}

        public ShareLinks(String facebook, String instagram, String tiktok, String whatsapp) {
            this.facebook = facebook;
            this.instagram = instagram;
            this.tiktok = tiktok;
            this.whatsapp = whatsapp;
        }

        public String getFacebook() {
            return facebook;
        }

        public void setFacebook(String facebook) {
            this.facebook = facebook;
        }

        public String getInstagram() {
            return instagram;
        }

        public void setInstagram(String instagram) {
            this.instagram = instagram;
        }

        public String getTiktok() {
            return tiktok;
        }

        public void setTiktok(String tiktok) {
            this.tiktok = tiktok;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public void setWhatsapp(String whatsapp) {
            this.whatsapp = whatsapp;
        }
    }
}
