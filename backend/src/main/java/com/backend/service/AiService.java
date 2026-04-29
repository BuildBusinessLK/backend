package com.backend.service;

import com.backend.dto.AiRequest;
import com.backend.dto.AiResponse;
import com.backend.dto.AiApiResponse;
import com.backend.entity.Message;
import com.backend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final MessageRepository messageRepository;

    @Value("${ai.service.url:http://localhost:8000/ask}")
    private String aiServiceUrl;

    public AiService(RestTemplate restTemplate, MessageRepository messageRepository) {
        this.restTemplate = restTemplate;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public AiApiResponse askAi(AiRequest request) {
        String question = request.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            return new AiApiResponse(false, null, "Question cannot be empty", null, 400);
        }

        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        try {
            List<Map<String, String>> chatHistory = getRecentChatHistory(conversationId);
            saveChatMessage(conversationId, "user", question);

            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("question", question);
            aiRequest.put("conversation_id", conversationId);
            aiRequest.put("chat_history", chatHistory);

            AiResponse response = restTemplate.postForObject(aiServiceUrl, aiRequest, AiResponse.class);
            if (response != null && response.getAnswer() != null) {
                saveChatMessage(conversationId, "assistant", response.getAnswer());

                return new AiApiResponse(true, response.getAnswer(), "Query successful", conversationId, 200);
            } else {
                return new AiApiResponse(false, null, "No response from AI service", conversationId, 500);
            }
        } catch (Exception e) {
            return new AiApiResponse(false, null, "Error connecting to AI service: " + e.getMessage(), conversationId, 503);
        }
    }

    private void saveChatMessage(String conversationId, String role, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messageRepository.save(message);
    }

    private List<Map<String, String>> getRecentChatHistory(String conversationId) {
        List<Message> messages = new ArrayList<>(messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversationId));
        Collections.reverse(messages);

        return messages.stream()
                .map(message -> {
                    Map<String, String> chatMessage = new HashMap<>();
                    chatMessage.put("role", message.getRole());
                    chatMessage.put("content", message.getContent());
                    return chatMessage;
                })
                .toList();
    }
}
