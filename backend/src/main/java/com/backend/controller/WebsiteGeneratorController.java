package com.backend.controller;

import com.backend.dto.WebsiteGenerateRequest;
import com.backend.service.WebsiteGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/website")
@CrossOrigin(origins = "*")
public class WebsiteGeneratorController {

    private final WebsiteGeneratorService websiteGeneratorService;

    public WebsiteGeneratorController(WebsiteGeneratorService websiteGeneratorService) {
        this.websiteGeneratorService = websiteGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody WebsiteGenerateRequest request) {
        Map<String, Object> response = websiteGeneratorService.generateWebsite(request);

        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Fallback responses should still be 200 for frontend parsing.
        return ResponseEntity.ok(response);
    }
}
