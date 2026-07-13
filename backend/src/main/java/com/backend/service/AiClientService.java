package com.backend.service;

import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
import com.backend.dto.ai.BusinessRecommendationRequest;
import com.backend.dto.ai.BusinessRecommendationResponse;
import com.backend.dto.ai.WebsiteCopyRequest;
import com.backend.dto.ai.WebsiteCopyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiClientService {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000/chat}")
    private String aiServiceUrl;

    @Value("${ai.service.website-copy-url:http://localhost:8000/website-copy}")
    private String websiteCopyUrl;

    @Value("${ai.service.business-advisor-url:http://localhost:8000/business-advisor}")
    private String businessAdvisorUrl;

    public AiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiChatResponse chat(AiChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiChatRequest> entity = new HttpEntity<>(request, headers);
        AiChatResponse body = restTemplate.postForObject(aiServiceUrl, entity, AiChatResponse.class);
        if (body == null || body.getMessage() == null || body.getMessage().isBlank()) {
            throw new IllegalStateException("AI service returned empty message");
        }
        return body;
    }

    public BusinessRecommendationResponse requestBusinessRecommendation(BusinessRecommendationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BusinessRecommendationRequest> entity = new HttpEntity<>(request, headers);
        BusinessRecommendationResponse body = restTemplate.postForObject(businessAdvisorUrl, entity, BusinessRecommendationResponse.class);
        if (body == null) {
            throw new IllegalStateException("AI service returned empty business recommendation");
        }
        return body;
    }

    public WebsiteCopyResponse requestWebsiteCopy(WebsiteCopyRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebsiteCopyRequest> entity = new HttpEntity<>(request, headers);
        WebsiteCopyResponse body = restTemplate.postForObject(websiteCopyUrl, entity, WebsiteCopyResponse.class);
        if (body == null) {
            throw new IllegalStateException("AI service returned empty website copy");
        }
        return body;
    }
}
