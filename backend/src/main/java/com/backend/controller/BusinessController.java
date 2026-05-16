package com.backend.controller;

import com.backend.dto.business.BusinessDetailDto;
import com.backend.dto.business.BusinessUpsertRequest;
import com.backend.security.CustomUserDetails;
import com.backend.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin(origins = "*")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public List<BusinessDetailDto> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return businessService.listMine(principal.getId());
    }

    @GetMapping("/{id}")
    public BusinessDetailDto get(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return businessService.getMine(principal.getId(), id);
    }

    @PostMapping
    public ResponseEntity<BusinessDetailDto> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody BusinessUpsertRequest req) {
        return ResponseEntity.ok(businessService.create(principal.getId(), req));
    }

    @PatchMapping("/{id}")
    public BusinessDetailDto update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody BusinessUpsertRequest req) {
        return businessService.update(principal.getId(), id, req);
    }
}
