package com.backend.controller;

import com.backend.dto.SmeWebsiteGenerateRequest;
import com.backend.dto.SmeWebsiteGenerateResponse;
import com.backend.service.SmeWebsiteOrchestrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sme-website")
@CrossOrigin(origins = "*")
public class SmeWebsiteController {

    private final SmeWebsiteOrchestrationService smeWebsiteOrchestrationService;

    public SmeWebsiteController(SmeWebsiteOrchestrationService smeWebsiteOrchestrationService) {
        this.smeWebsiteOrchestrationService = smeWebsiteOrchestrationService;
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
}
