package com.backend.controller;

import com.backend.dto.SocialAdRequest;
import com.backend.dto.SocialAdResponse;
import com.backend.dto.SocialPostResponse;
import com.backend.dto.SocialPostsRequest;
import com.backend.service.SocialMediaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class SocialController {

    private static final Logger log = LoggerFactory.getLogger(SocialController.class);
    private final SocialMediaService socialMediaService;

    public SocialController(SocialMediaService socialMediaService) {
        this.socialMediaService = socialMediaService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, SocialAdResponse>> generateAd(@Valid @RequestBody SocialAdRequest request) {
        log.info("Generating ad for platform={} tone={}", request.getPlatform(), request.getTone());
        SocialAdResponse ad = socialMediaService.generateAd(request);
        return ResponseEntity.ok(Map.of("ad", ad));
    }

    @PostMapping("/generate-posts")
    public ResponseEntity<Map<String, List<SocialPostResponse>>> generatePosts(@Valid @RequestBody SocialPostsRequest request) {
        int platformCount = request.getPlatforms() == null ? 0 : request.getPlatforms().size();
        log.info("Generating posts for {} platforms", platformCount);
        List<SocialPostResponse> posts = socialMediaService.generatePosts(request);
        return ResponseEntity.ok(Map.of("posts", posts));
    }
}
