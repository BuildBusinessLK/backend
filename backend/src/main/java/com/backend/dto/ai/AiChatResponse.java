package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiChatResponse {

    @JsonAlias("answer")
    private String message;

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
}
