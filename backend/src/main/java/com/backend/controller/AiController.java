package com.backend.controller;

import com.backend.dto.AiRequest;
import com.backend.dto.AiApiResponse;
import com.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/ask")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<AiApiResponse> askAI(@RequestBody AiRequest request, Authentication authentication) {
        AiApiResponse response = aiService.askAi(request, authentication);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getCode()).body(response);
        }
    }

}
