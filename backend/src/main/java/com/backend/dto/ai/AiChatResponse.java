package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiChatResponse {

    private String type;

    @JsonAlias("answer")
    private String message;

    private BusinessRecommendationResponse recommendation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAnswer(String answer) {
        if (this.message == null) {
            this.message = answer;
        }
    }

    public String getAnswer() {
        return message;
    }

    public BusinessRecommendationResponse getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(BusinessRecommendationResponse recommendation) {
        this.recommendation = recommendation;
    }
}
