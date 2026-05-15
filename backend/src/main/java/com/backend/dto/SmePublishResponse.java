package com.backend.dto;

public class SmePublishResponse {

    private String slug;
    private String publicPath;
    private boolean updatedExisting;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public boolean isUpdatedExisting() {
        return updatedExisting;
    }

    public void setUpdatedExisting(boolean updatedExisting) {
        this.updatedExisting = updatedExisting;
    }
}
