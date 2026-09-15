package com.backend.controller;

import com.backend.dto.email.*;
import com.backend.security.CustomUserDetails;
import com.backend.service.EmailCampaignService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailCampaignController {

    private static final Logger log = LoggerFactory.getLogger(EmailCampaignController.class);

    private final EmailCampaignService emailCampaignService;

    public EmailCampaignController(EmailCampaignService emailCampaignService) {
        this.emailCampaignService = emailCampaignService;
    }

    @GetMapping("/recipient-groups")
    public ResponseEntity<List<RecipientGroupDto>> getRecipientGroups(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal == null ? null : principal.getId();
        return ResponseEntity.ok(emailCampaignService.getRecipientGroups(userId));
    }

    @GetMapping("/recipients")
    public ResponseEntity<Page<EmailRecipientDto>> getRecipients(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return ResponseEntity.ok(emailCampaignService.getRecipients(groupId, search, pageable));
    }

    @PostMapping("/generate")
    public ResponseEntity<EmailGenerateResponse> generateEmail(
            @RequestBody EmailGenerateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal == null ? null : principal.getId();
        return ResponseEntity.ok(emailCampaignService.generateEmail(request, userId));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<EmailCampaignDto>> getCampaigns(
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(emailCampaignService.getCampaigns(principal.getId()));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<EmailCampaignDto> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailCampaignService.createCampaign(request, principal.getId()));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<EmailCampaignDto> getCampaignById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(emailCampaignService.getCampaignById(id, principal.getId()));
    }

    @GetMapping("/campaigns/{id}/recipients")
    public ResponseEntity<List<CampaignRecipientDto>> getCampaignRecipients(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(emailCampaignService.getCampaignRecipients(id, principal.getId()));
    }

    @PostMapping("/campaigns/{id}/simulate")
    public ResponseEntity<CampaignSimulationResponse> simulateCampaign(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(emailCampaignService.simulateCampaign(id, principal.getId()));
    }

    @PostMapping("/campaigns/{id}/test-send")
    public ResponseEntity<Map<String, Object>> testSend(
            @PathVariable Long id,
            @RequestBody(required = false) TestSendRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = request == null ? null : request.getTestEmail();
        return ResponseEntity.ok(emailCampaignService.sendTestEmail(id, email, principal.getId()));
    }

    @PostMapping("/recipients/import-edb")
    public ResponseEntity<Map<String, Object>> importEdb() {
        int count = emailCampaignService.importEdbDirectory();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully imported " + count + " official EDB registered exporters into MySQL database.",
                "count", count
        ));
    }
}
