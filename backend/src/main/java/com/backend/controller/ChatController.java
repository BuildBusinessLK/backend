package com.backend.controller;

import com.backend.dto.chat.*;
import com.backend.security.CustomUserDetails;
import com.backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/messages")
    public ResponseEntity<ChatSendResponse> send(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody ChatSendRequest request) {
        return ResponseEntity.ok(chatService.send(principal.getId(), request));
    }
}
