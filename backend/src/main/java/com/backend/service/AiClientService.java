package com.backend.service;

import com.backend.dto.ai.AdGenerationRequest;
import com.backend.dto.ai.AdGenerationResponse;
import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
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

    @Value("${ai.service.ad-generation-url:http://localhost:8000/ad-generate}")
    private String adGenerationUrl;

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

    public AdGenerationResponse generateAdCopy(AdGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdGenerationRequest> entity = new HttpEntity<>(request, headers);
        AdGenerationResponse body = restTemplate.postForObject(adGenerationUrl, entity, AdGenerationResponse.class);
        if (body == null || body.getGeneratedAds() == null || body.getGeneratedAds().isBlank()) {
            throw new IllegalStateException("AI service returned empty ad copy");
        }
        return body;
    }
}
