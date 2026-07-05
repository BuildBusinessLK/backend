package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdGenerationResponse {

    @JsonProperty("generated_ads")
    private String generatedAds;

    public String getGeneratedAds() {
        return generatedAds;
    }

    public void setGeneratedAds(String generatedAds) {
        this.generatedAds = generatedAds;
    }

}
