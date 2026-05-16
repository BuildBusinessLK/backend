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
}
