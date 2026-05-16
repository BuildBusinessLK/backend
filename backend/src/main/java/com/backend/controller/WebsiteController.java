package com.backend.controller;

import com.backend.dto.website.GeneratedWebsiteDto;
import com.backend.dto.website.WebsiteGenerateRequest;
import com.backend.security.CustomUserDetails;
import com.backend.service.WebsiteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/websites")
@CrossOrigin(origins = "*")
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @GetMapping("/business/{businessId}/latest")
    public ResponseEntity<GeneratedWebsiteDto> latest(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long businessId) {
        GeneratedWebsiteDto dto = websiteService.getLatestForBusiness(principal.getId(), businessId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/generate")
    public GeneratedWebsiteDto generate(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody WebsiteGenerateRequest req) {
        return websiteService.generate(principal.getId(), req);
    }

    @PostMapping("/{id}/publish")
    public GeneratedWebsiteDto publish(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return websiteService.publish(principal.getId(), id);
    }
}
