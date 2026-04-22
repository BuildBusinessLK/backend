package com.backend.controller;

import com.backend.dto.MessageRequest;
import com.backend.dto.MessageResponse;
import com.backend.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages/save")
@CrossOrigin(origins = "*")
public class MessageController {
    
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    /**
     * API to save a new message
     * POST /api/messages
     */
    @PostMapping
    public ResponseEntity<MessageResponse> saveMessage(@Valid @RequestBody MessageRequest request) {
        log.info("Received request to save message");
        MessageResponse response = messageService.saveMessage(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * API to fetch all messages
     * GET /api/messages
     */
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        log.info("Received request to fetch all messages");
        List<MessageResponse> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }
    
    /**
     * API to fetch a specific message by ID
     * GET /api/messages/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        log.info("Received request to fetch message with id: {}", id);
        MessageResponse message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }
}
