package com.backend.dto.social;

public class InstagramStatusResponse {
    private boolean configured;
    private boolean connected;
    private String instagramAccountId;
    private String instagramUsername;
    private String facebookPageName;

    public InstagramStatusResponse() {}

    public InstagramStatusResponse(boolean configured, boolean connected, String instagramAccountId, String instagramUsername, String facebookPageName) {
        this.configured = configured;
        this.connected = connected;
        this.instagramAccountId = instagramAccountId;
        this.instagramUsername = instagramUsername;
        this.facebookPageName = facebookPageName;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getInstagramAccountId() {
        return instagramAccountId;
    }

    public void setInstagramAccountId(String instagramAccountId) {
        this.instagramAccountId = instagramAccountId;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }

    public String getFacebookPageName() {
        return facebookPageName;
    }

    public void setFacebookPageName(String facebookPageName) {
        this.facebookPageName = facebookPageName;
    }
}
