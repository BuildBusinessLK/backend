package com.backend.controller;

import com.backend.dto.AiRequest;
import com.backend.dto.ChatResponse;
import com.backend.dto.ChatSessionRequest;
import com.backend.dto.chat.ChatSendRequest;
import com.backend.dto.chat.ChatSendResponse;
import com.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ask")
@CrossOrigin(origins = "*")
public class AiController {

    private final ChatService chatService;

    public AiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<String> askAI(@RequestBody AiRequest request) {
        String answer = "Processed: " + request.getQuestion();
        return ResponseEntity.ok(answer);
    }

    /**
     * Create a new chat session (returns sessionId)
     */
    @PostMapping("/session")
    public ResponseEntity<String> createSession() {
        String id = String.valueOf(chatService.createSession());
        return ResponseEntity.ok(id);
    }

    /**
     * Post a message to a chat session and receive assistant reply.
     * If `sessionId` is omitted in the request, a new session is created.
     */
    @PostMapping("/session/message")
    public ResponseEntity<ChatSendResponse> postMessage(@RequestBody ChatSessionRequest req) {
        ChatSendRequest request = new ChatSendRequest();
        request.setSessionId(req.getSessionId() == null ? null : Long.parseLong(req.getSessionId()));
        request.setQuestion(req.getMessage());
        return ResponseEntity.ok(chatService.send(1L, request));
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<ChatResponse> getSession(@PathVariable("id") String id) {
        ChatResponse res = new ChatResponse();
        res.setSessionId(id);
        res.setConversation(chatService.getConversation(id));
        res.setGeneratedAt(java.time.Instant.now());
        res.setBriefSummary(chatService.getBriefSummary(id));
        res.setQuickReplies(chatService.getQuickReplies(id));
        return ResponseEntity.ok(res);
    }

}
