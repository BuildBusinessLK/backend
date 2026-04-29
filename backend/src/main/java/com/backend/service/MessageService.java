package com.backend.service;

import com.backend.dto.MessageRequest;
import com.backend.dto.MessageResponse;
import com.backend.entity.Message;
import com.backend.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final MessageRepository messageRepository;
    
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    @Transactional
    public MessageResponse saveMessage(MessageRequest request) {
        log.info("Saving new message with content: {}", request.getContent());
        
        Message message = new Message();
        message.setContent(request.getContent());
        message.setRole("user");
        message.setConversationId("manual");
        
        Message savedMessage = messageRepository.save(message);
        log.info("Message saved successfully with id: {}", savedMessage.getId());
        
        return mapToResponse(savedMessage);
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages() {
        log.info("Fetching all messages");
        
        List<Message> messages = messageRepository.findAll();
        log.info("Found {} messages", messages.size());
        
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        log.info("Fetching message with id: {}", id);
        
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        
        return mapToResponse(message);
    }
    
    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setRole(message.getRole());
        response.setConversationId(message.getConversationId());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }
}
