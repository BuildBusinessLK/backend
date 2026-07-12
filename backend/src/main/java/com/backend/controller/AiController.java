package com.backend.controller;

import com.backend.dto.AiRequest;
import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
import com.backend.service.AiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ask")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiClientService aiClientService;

    public AiController(AiClientService aiClientService) {
        this.aiClientService = aiClientService;
    }

    @PostMapping
    public ResponseEntity<String> askAI(@RequestBody AiRequest request) {
        AiChatRequest aiRequest = new AiChatRequest();
        aiRequest.setQuestion(request.getQuestion());

        AiChatResponse response = aiClientService.chat(aiRequest);
        return ResponseEntity.ok(response.getMessage());
    }
}
