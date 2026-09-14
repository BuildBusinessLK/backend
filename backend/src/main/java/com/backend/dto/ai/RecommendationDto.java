package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationDto {
    private int rank;
    private String product;
    private double confidence;

    public RecommendationDto() {}

    public RecommendationDto(int rank, String product, double confidence) {
        this.rank = rank;
        this.product = product;
        this.confidence = confidence;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
