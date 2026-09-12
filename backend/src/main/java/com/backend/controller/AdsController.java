package com.backend.controller;

import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.AdsGenerationResponse;
import com.backend.dto.AdVisualRequest;
import com.backend.dto.AdVisualResponse;
import com.backend.service.AdsGenerationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ads")
@CrossOrigin(origins = "*")
public class AdsController {

    private static final Logger log = LoggerFactory.getLogger(AdsController.class);
    private final AdsGenerationService adsGenerationService;

    public AdsController(AdsGenerationService adsGenerationService) {
        this.adsGenerationService = adsGenerationService;
    }

    /**
     * Generate creative ads based on user's idea
     */
    @PostMapping("/generate")
    public ResponseEntity<AdsGenerationResponse> generateAds(@Valid @RequestBody AdsGenerationRequest request) {
        log.info("Generating ads for idea: {}", request.getIdea());
        AdsGenerationResponse response = adsGenerationService.generateAds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/visuals")
    public ResponseEntity<AdVisualResponse> generateVisuals(@Valid @RequestBody AdVisualRequest request) {
        return ResponseEntity.ok(adsGenerationService.generateVisuals(request));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Ads generation service is running");
    }
}
