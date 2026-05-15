package com.backend.controller;

import com.backend.dto.SmePublishRequest;
import com.backend.dto.SmeWebsiteGenerateRequest;
import com.backend.dto.SmeWebsiteGenerateResponse;
import com.backend.service.SmeHostedSiteService;
import com.backend.service.SmeWebsiteOrchestrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sme-website")
@CrossOrigin(origins = "*")
public class SmeWebsiteController {

    private final SmeWebsiteOrchestrationService smeWebsiteOrchestrationService;
    private final SmeHostedSiteService smeHostedSiteService;

    public SmeWebsiteController(
            SmeWebsiteOrchestrationService smeWebsiteOrchestrationService,
            SmeHostedSiteService smeHostedSiteService) {
        this.smeWebsiteOrchestrationService = smeWebsiteOrchestrationService;
        this.smeHostedSiteService = smeHostedSiteService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody SmeWebsiteGenerateRequest request) {
        if (request == null || request.getBusinessName() == null || request.getBusinessName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "businessName is required"));
        }
        SmeWebsiteGenerateResponse body = smeWebsiteOrchestrationService.generate(request);
        return ResponseEntity.ok(body);
    }

    /**
     * Saves generated HTML + manifest for public serving at /business/{slug} on the React app.
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody SmePublishRequest request) {
        try {
            return ResponseEntity.ok(smeHostedSiteService.publish(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (JsonProcessingException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid manifest"));
        }
    }
}
