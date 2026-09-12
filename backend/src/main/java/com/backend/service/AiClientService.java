package com.backend.service;

import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
import com.backend.dto.ai.WebsiteCopyRequest;
import com.backend.dto.ai.WebsiteCopyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000/chat}")
    private String aiServiceUrl;

    @Value("${ai.service.website-copy-url:http://localhost:8000/website-copy}")
    private String websiteCopyUrl;

    public AiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiChatResponse chat(AiChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiChatRequest> entity = new HttpEntity<>(request, headers);
        try {
            AiChatResponse body = restTemplate.postForObject(aiServiceUrl, entity, AiChatResponse.class);
            if (body == null || body.getMessage() == null || body.getMessage().isBlank()) {
                throw new IllegalStateException("AI service returned empty message");
            }
            return body;
        } catch (HttpStatusCodeException e) {
            log.warn("AI service at {} responded with HTTP {}: {}", aiServiceUrl, e.getStatusCode(), e.getStatusText());
            AiChatResponse fallback = new AiChatResponse();
            fallback.setMessage("I am having trouble connecting to the AI knowledge service right now (HTTP "
                    + e.getStatusCode().value()
                    + "). If the AI service is hosted on a free plan, it may take 30–60 seconds to wake up from sleep. Please try again shortly.");
            return fallback;
        } catch (ResourceAccessException e) {
            log.warn("AI service at {} could not be reached: {}", aiServiceUrl, e.getMessage());
            AiChatResponse fallback = new AiChatResponse();
            fallback.setMessage("The AI knowledge service is currently unreachable. If the service is waking up, please wait a moment and try again.");
            return fallback;
        }
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
