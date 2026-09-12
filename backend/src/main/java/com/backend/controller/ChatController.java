package com.backend.controller;

import com.backend.dto.chat.*;
import com.backend.security.CustomUserDetails;
import com.backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/sessions")
    public List<ChatSessionDto> sessions(@AuthenticationPrincipal CustomUserDetails principal) {
        return chatService.listSessions(principal.getId());
    }

    @PostMapping("/sessions")
    public ChatSessionDto newSession(
            @AuthenticationPrincipal CustomUserDetails principal, @RequestBody(required = false) Map<String, String> body) {
        String title = body != null ? body.get("title") : null;
        return chatService.createSession(principal.getId(), title);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatMessageDto> messages(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long sessionId) {
        return chatService.listMessages(principal.getId(), sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long sessionId) {
        chatService.deleteSession(principal.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatSendResponse> send(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody ChatSendRequest request) {
        return ResponseEntity.ok(chatService.send(principal.getId(), request));
    }

    @PostMapping("/recommendation")
    public ResponseEntity<?> recommendation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody Map<String, Object> body) {
        Long sessionId = null;
        Object rawSessionId = body.get("sessionId");
        if (rawSessionId != null) {
            sessionId = Long.valueOf(String.valueOf(rawSessionId));
        }
        Map<String, Object> userProfile = body.containsKey("userProfile") && body.get("userProfile") instanceof Map<?, ?>
                ? convertMap((Map<?, ?>) body.get("userProfile"))
                : Map.of();
        Map<String, Object> businessProfile = body.containsKey("businessProfile") && body.get("businessProfile") instanceof Map<?, ?>
                ? convertMap((Map<?, ?>) body.get("businessProfile"))
                : Map.of();
        return ResponseEntity.ok(chatService.recommendBusiness(principal.getId(), sessionId, userProfile, businessProfile));
    }

    private Map<String, Object> convertMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }
}
