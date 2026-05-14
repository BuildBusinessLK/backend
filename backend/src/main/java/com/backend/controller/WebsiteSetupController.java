package com.backend.controller;

import com.backend.dto.WebsiteSetupRequest;
import com.backend.entity.WebsiteSetup;
import com.backend.repository.WebsiteSetupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/website/setups")
@CrossOrigin(origins = "*")
public class WebsiteSetupController {

    private final WebsiteSetupRepository websiteSetupRepository;

    public WebsiteSetupController(WebsiteSetupRepository websiteSetupRepository) {
        this.websiteSetupRepository = websiteSetupRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody WebsiteSetupRequest request) {
        String missing = missingRequiredField(request);
        if (missing != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required field: " + missing
            ));
        }

        WebsiteSetup entity = new WebsiteSetup();
        entity.setBusinessName(trim(request.getBusinessName()));
        entity.setIndustry(trim(request.getIndustry()));
        entity.setBusinessDescription(trim(request.getBusinessDescription()));
        entity.setTargetAudience(trim(request.getTargetAudience()));
        entity.setWebsiteGoal(trim(request.getWebsiteGoal()));
        entity.setCurrentWebsite(trimToNull(request.getCurrentWebsite()));
        entity.setAdditionalNotes(trimToNull(request.getAdditionalNotes()));

        WebsiteSetupRequest.Features features = request.getFeatures();
        entity.setContactForm(bool(features != null ? features.getContactForm() : null));
        entity.setProductShowcase(bool(features != null ? features.getProductShowcase() : null));
        entity.setBlog(bool(features != null ? features.getBlog() : null));
        entity.setEcommerce(bool(features != null ? features.getEcommerce() : null));
        entity.setNewsletter(bool(features != null ? features.getNewsletter() : null));

        WebsiteSetup saved = websiteSetupRepository.save(entity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Website setup saved successfully");
        response.put("setup_id", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<WebsiteSetup> setups = websiteSetupRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", setups);
        response.put("total", setups.size());
        return ResponseEntity.ok(response);
    }

    private static String missingRequiredField(WebsiteSetupRequest request) {
        if (request == null) {
            return "businessName";
        }
        if (isBlank(request.getBusinessName())) {
            return "businessName";
        }
        if (isBlank(request.getIndustry())) {
            return "industry";
        }
        if (isBlank(request.getBusinessDescription())) {
            return "businessDescription";
        }
        if (isBlank(request.getTargetAudience())) {
            return "targetAudience";
        }
        if (isBlank(request.getWebsiteGoal())) {
            return "websiteGoal";
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean bool(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
