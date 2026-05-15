package com.backend.controller;

import com.backend.dto.SmePublicSiteResponse;
import com.backend.service.SmeHostedSiteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/sme-sites")
@CrossOrigin(origins = "*")
public class PublicSmeSiteController {

    private final SmeHostedSiteService smeHostedSiteService;

    public PublicSmeSiteController(SmeHostedSiteService smeHostedSiteService) {
        this.smeHostedSiteService = smeHostedSiteService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable("slug") String slug) {
        Optional<SmePublicSiteResponse> body = smeHostedSiteService.getPublicBySlug(slug);
        if (body.isPresent()) {
            return ResponseEntity.ok(body.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Site not found"));
    }
}
