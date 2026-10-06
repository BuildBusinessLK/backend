package com.backend.dto.social;

public class InstagramPublishResponse {
    private boolean success;
    private Long postId;
    private String externalPostId;
    private String permalink;
    private String message;

    public InstagramPublishResponse() {}

    public InstagramPublishResponse(boolean success, Long postId, String externalPostId, String permalink, String message) {
        this.success = success;
        this.postId = postId;
        this.externalPostId = externalPostId;
        this.permalink = permalink;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getExternalPostId() {
        return externalPostId;
    }

    public void setExternalPostId(String externalPostId) {
        this.externalPostId = externalPostId;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
