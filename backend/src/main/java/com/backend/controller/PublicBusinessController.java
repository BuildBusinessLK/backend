package com.backend.controller;

import com.backend.dto.publicapi.PublicBusinessResponse;
import com.backend.service.PublicBusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/business")
@CrossOrigin(origins = "*")
public class PublicBusinessController {

    private final PublicBusinessService publicBusinessService;

    public PublicBusinessController(PublicBusinessService publicBusinessService) {
        this.publicBusinessService = publicBusinessService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> bySlug(@PathVariable String slug) {
        return publicBusinessService
                .getBySlug(slug)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found")));
    }

    @PostMapping("/{slug}/click")
    public ResponseEntity<?> recordClick(@PathVariable String slug, @RequestBody Map<String, String> body) {
        String eventType = body.get("eventType");
        if (eventType == null || eventType.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "eventType is required"));
        }
        try {
            publicBusinessService.recordClick(slug, eventType);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
