package com.backend.service;

import com.backend.dto.ai.AdGenerationRequest;
import com.backend.dto.ai.AdGenerationResponse;
import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
import com.backend.dto.ai.BusinessRecommendationRequest;
import com.backend.dto.ai.BusinessRecommendationResponse;
import com.backend.dto.ai.WebsiteCopyRequest;
import com.backend.dto.ai.WebsiteCopyResponse;
import com.backend.dto.email.EmailGenerateRequest;
import com.backend.dto.email.EmailGenerateResponse;
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

    @Value("${ai.service.ad-generation-url:http://localhost:8000/ad-generate}")
    private String adGenerationUrl;

    @Value("${ai.service.recommendation-url:http://localhost:8000/business-advisor}")
    private String recommendationUrl;

    @Value("${ai.service.email-generate-url:http://localhost:8000/email-generate}")
    private String emailGenerateUrl;

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

    public AdGenerationResponse generateAdCopy(AdGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdGenerationRequest> entity = new HttpEntity<>(request, headers);
        try {
            AdGenerationResponse body = restTemplate.postForObject(adGenerationUrl, entity, AdGenerationResponse.class);
            if (body == null || body.getGeneratedAds() == null || body.getGeneratedAds().isBlank()) {
                AdGenerationResponse fallback = new AdGenerationResponse();
                fallback.setGeneratedAds("We could not retrieve a full AI draft right now, but your campaign brief is ready to use.");
                return fallback;
            }
            return body;
        } catch (Exception ex) {
            AdGenerationResponse fallback = new AdGenerationResponse();
            fallback.setGeneratedAds("We could not retrieve a full AI draft right now, but your campaign brief is ready to use.");
            return fallback;
        }
    }

    public BusinessRecommendationResponse requestBusinessRecommendation(BusinessRecommendationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BusinessRecommendationRequest> entity = new HttpEntity<>(request, headers);
        try {
            BusinessRecommendationResponse body = restTemplate.postForObject(recommendationUrl, entity, BusinessRecommendationResponse.class);
            if (body == null) {
                throw new IllegalStateException("AI recommendation service returned an empty response.");
            }
            return body;
        } catch (HttpStatusCodeException e) {
            log.warn("AI recommendation service at {} responded with HTTP {}: {}", recommendationUrl, e.getStatusCode(), e.getResponseBodyAsString());
            BusinessRecommendationResponse errResponse = new BusinessRecommendationResponse();
            errResponse.setMessage("Unable to generate recommendation at this moment (HTTP " + e.getStatusCode().value() + "). Please try again shortly.");
            return errResponse;
        } catch (Exception ex) {
            log.warn("AI recommendation service request failed: {}", ex.getMessage());
            BusinessRecommendationResponse errResponse = new BusinessRecommendationResponse();
            errResponse.setMessage("The AI recommendation service is currently unreachable. If the service is waking up, please wait a moment and try again.");
            return errResponse;
        }
    }

    public EmailGenerateResponse generateEmail(EmailGenerateRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailGenerateRequest> entity = new HttpEntity<>(request, headers);
        try {
            EmailGenerateResponse body = restTemplate.postForObject(emailGenerateUrl, entity, EmailGenerateResponse.class);
            if (body == null || body.getSubject() == null || body.getBody() == null) {
                throw new IllegalStateException("AI service returned empty email response");
            }
            return body;
        } catch (Exception ex) {
            log.warn("AI email generation failed at {}: {}", emailGenerateUrl, ex.getMessage());
            String comp = (request.getCompanyName() != null && !request.getCompanyName().isBlank()) ? request.getCompanyName() : "Lanka Value Agribusiness";
            String sec = (request.getSector() != null && !request.getSector().isBlank()) ? request.getSector() : "Coconut";
            String prod = (request.getProductName() != null && !request.getProductName().isBlank()) ? request.getProductName() : ("Premium " + sec + " Products");
            String user = (request.getUserName() != null && !request.getUserName().isBlank()) ? request.getUserName() : "Commercial Sales Director";
            String phone = (request.getContactPhone() != null && !request.getContactPhone().isBlank()) ? request.getContactPhone() : "+94 77 123 4567";
            String email = (request.getContactEmail() != null && !request.getContactEmail().isBlank()) ? request.getContactEmail() : "inquiry@buildbusinesslk.com";

            String subject = "Commercial Supply Inquiry: Export-Grade " + prod + " – " + comp;
            String body = "Dear Commercial Partner,\n\n"
                    + "I hope this email finds you well.\n\n"
                    + "I am reaching out on behalf of " + comp + ", a verified producer in the Sri Lankan " + sec + " sector. "
                    + "We are pleased to introduce our current commercial batch of " + prod + ", processed in strict adherence to hygienic standards.\n\n"
                    + "Commercial Specification Highlights:\n"
                    + "• 100% authentic Sri Lankan farmgate origin with full batch traceability\n"
                    + "• Consistent grading and purity suitable for bulk export and retail packaging\n"
                    + "• Low initial Minimum Order Quantity (MOQ) for commercial evaluation\n\n"
                    + "We would welcome the opportunity to provide technical specifications and sample packs for your evaluation.\n\n"
                    + "Could we arrange a brief call or may I send over our wholesale pricing catalog this week?\n\n"
                    + "Kind regards,\n\n"
                    + user + "\n"
                    + comp + "\n"
                    + "Tel: " + phone + "\n"
                    + "Email: " + email;

            return new EmailGenerateResponse(
                    subject,
                    body,
                    "Request wholesale technical specification and sample pack",
                    "Targeting EDB Registered Exporters & Wholesale Distributors"
            );
        }
    }
}

