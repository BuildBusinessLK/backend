package com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class SocialPostsRequest {

    @NotBlank(message = "Idea cannot be blank")
    private String idea;

    @NotEmpty(message = "At least one platform is required")
    private List<String> platforms;

    public SocialPostsRequest() {
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }
}
