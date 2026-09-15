package com.backend.service;

import com.backend.domain.ChatSender;
import com.backend.dto.ai.AiChatRequest;
import com.backend.dto.ai.AiChatResponse;
import com.backend.dto.ai.BusinessRecommendationRequest;
import com.backend.dto.ai.BusinessRecommendationResponse;
import com.backend.dto.chat.ChatMessageDto;
import com.backend.dto.chat.ChatSendRequest;
import com.backend.dto.chat.ChatSendResponse;
import com.backend.dto.chat.ChatSessionDto;
import com.backend.entity.*;
import com.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProductRepository businessProductRepository;
    private final BusinessSocialLinkRepository businessSocialLinkRepository;
    private final BusinessDocumentRepository businessDocumentRepository;
    private final AiClientService aiClientService;
    private final AssistantActionDetector assistantActionDetector;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessProductRepository businessProductRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            BusinessDocumentRepository businessDocumentRepository,
            AiClientService aiClientService,
            AssistantActionDetector assistantActionDetector) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessProductRepository = businessProductRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.businessDocumentRepository = businessDocumentRepository;
        this.aiClientService = aiClientService;
        this.assistantActionDetector = assistantActionDetector;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDto> listSessions(Long userId) {
        return chatSessionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSessionDto)
                .toList();
    }

    @Transactional
    public ChatSessionDto createSession(Long userId, String title) {
        User user = userRepository.findById(userId).orElseThrow();
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setTitle(title != null && !title.isBlank() ? title.trim() : "New chat");
        session = chatSessionRepository.save(session);
        return toSessionDto(session);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> listMessages(Long userId, Long sessionId) {
        ChatSession session = chatSessionRepository.findByIdAndUser_Id(sessionId, userId).orElseThrow();
        return chatMessageRepository.findTop50BySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toMessageDto)
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionRepository.findByIdAndUser_Id(sessionId, userId).orElseThrow();
        chatMessageRepository.deleteBySession_Id(session.getId());
        chatSessionRepository.delete(session);
    }

    @Transactional
    public ChatSendResponse send(Long userId, ChatSendRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        ChatSession session;
        if (request.getSessionId() == null) {
            session = new ChatSession();
            session.setUser(user);
            String title = summarizeTitle(request.getQuestion());
            session.setTitle(title);
            session = chatSessionRepository.save(session);
        } else {
            session = chatSessionRepository.findByIdAndUser_Id(request.getSessionId(), userId).orElseThrow();
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender(ChatSender.USER);
        userMsg.setMessage(request.getQuestion().trim());
        chatMessageRepository.save(userMsg);

        List<Map<String, String>> history = buildChatHistory(session.getId());

        AiChatRequest aiRequest = new AiChatRequest();
        aiRequest.setQuestion(request.getQuestion().trim());
        aiRequest.setChatHistory(history);
        aiRequest.setUserProfile(buildUserProfileMap(userId));
        aiRequest.setBusinessProfile(buildBusinessProfileMap(userId));

        AiChatResponse aiResponse = aiClientService.chat(aiRequest);
        String text = aiResponse.getMessage().trim();

        String action = assistantActionDetector.detectActionForAssistantMessage(text);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSession(session);
        aiMsg.setSender(ChatSender.AI);
        aiMsg.setMessage(text);
        aiMsg.setAction(action);
        aiMsg = chatMessageRepository.save(aiMsg);

        ChatSendResponse out = new ChatSendResponse();
        out.setMessage(text);
        out.setAction(action);
        out.setType(aiResponse.getType());
        out.setRecommendation(aiResponse.getRecommendation());
        out.setEmailCampaign(aiResponse.getEmailCampaign());
        out.setSessionId(session.getId());
        out.setMessageId(aiMsg.getId());
        return out;
    }

    @Transactional
    public BusinessRecommendationResponse recommendBusiness(Long userId, Long sessionId, Map<String, Object> userProfile, Map<String, Object> businessProfile) {
        User user = userRepository.findById(userId).orElseThrow();
        ChatSession session = sessionId == null ? null : chatSessionRepository.findByIdAndUser_Id(sessionId, userId).orElse(null);
        if (session == null) {
            session = new ChatSession();
            session.setUser(user);
            session.setTitle("Product fit check");
            session = chatSessionRepository.save(session);
        }

        BusinessRecommendationRequest aiRequest = new BusinessRecommendationRequest();
        aiRequest.setSessionId(session.getId());
        aiRequest.setUserProfile(userProfile);
        aiRequest.setBusinessProfile(businessProfile);

        BusinessRecommendationResponse response = aiClientService.requestBusinessRecommendation(aiRequest);
        String guidance = response.getGuidance() == null ? "" : response.getGuidance().trim();
        String recommendation = response.getRecommendedBusiness() == null ? "" : response.getRecommendedBusiness().trim();

        String userPrompt = "Which product suits my business profile best?";
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender(ChatSender.USER);
        userMsg.setMessage(userPrompt);
        chatMessageRepository.save(userMsg);

        StringBuilder aiText = new StringBuilder();
        if (!recommendation.isBlank()) {
            aiText.append("**Recommended Product:** ").append(recommendation);
            if (response.getRecommendations() != null && !response.getRecommendations().isEmpty()) {
                aiText.append(" (").append(response.getRecommendations().get(0).getConfidence()).append("% Match)");
            }
            if (!guidance.isBlank()) {
                aiText.append("\n\n**Guidance:**\n").append(guidance);
            }
        } else {
            aiText.append(response.getMessage() != null ? response.getMessage() : "Unable to generate recommendation at this moment.");
        }

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSession(session);
        aiMsg.setSender(ChatSender.AI);
        aiMsg.setMessage(aiText.toString());
        chatMessageRepository.save(aiMsg);

        response.setSessionId(session.getId());
        return response;
    }

    private String summarizeTitle(String question) {
        if (question == null) {
            return "New chat";
        }
        String clean = question.trim().replaceAll("^[?.,!\\s]+", "");
        if (clean.isEmpty()) return "New chat";

        // Remove conversational preambles
        String lower = clean.toLowerCase();
        String[] prefixes = {
            "can you tell me about ", "tell me about ", "can you help me with ",
            "how do i ", "how can i ", "how to ", "what is ", "what are ",
            "i want to know about ", "i need help with ", "i want to create "
        };
        for (String p : prefixes) {
            if (lower.startsWith(p)) {
                clean = clean.substring(p.length()).trim();
                break;
            }
        }
        if (clean.isEmpty()) clean = question.trim();

        // Capitalize first character
        clean = Character.toUpperCase(clean.charAt(0)) + clean.substring(1);

        if (clean.length() <= 45) {
            return clean;
        }
        return clean.substring(0, 42).trim() + "…";
    }

    private List<Map<String, String>> buildChatHistory(Long sessionId) {
        List<ChatMessage> rows = chatMessageRepository.findTop50BySession_IdOrderByCreatedAtAsc(sessionId);
        List<Map<String, String>> out = new ArrayList<>();
        for (ChatMessage m : rows) {
            Map<String, String> row = new LinkedHashMap<>();
            if (m.getSender() == ChatSender.USER) {
                row.put("role", "user");
            } else {
                row.put("role", "assistant");
            }
            row.put("content", m.getMessage());
            out.add(row);
        }
        return out;
    }

    private Map<String, Object> buildUserProfileMap(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("status", user.getStatus().name());
        if (profile != null) {
            map.put("fullName", profile.getFullName());
            map.put("phone", profile.getPhone());
            map.put("district", profile.getDistrict());
            map.put("experienceLevel", profile.getExperienceLevel());
            map.put("preferredLanguage", profile.getPreferredLanguage());
        }
        return map;
    }

    private Map<String, Object> buildBusinessProfileMap(Long userId) {
        List<Business> businesses = businessRepository.findByOwner_Id(userId);
        if (businesses.isEmpty()) {
            return Map.of("hasBusiness", false);
        }
        Business b = businesses.get(0);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("hasBusiness", true);
        root.put("businessName", b.getBusinessName());
        root.put("sector", b.getSector().name());
        root.put("websiteSlug", b.getWebsiteSlug());

        businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(bp -> {
            root.put("businessDescription", bp.getBusinessDescription());
            root.put("targetMarket", bp.getTargetMarket());
            root.put("monthlyIncome", bp.getMonthlyIncome());
            root.put("monthlyProduction", bp.getMonthlyProduction());
            root.put("marketingGoals", bp.getMarketingGoals());
        });

        List<Map<String, Object>> products = businessProductRepository.findByBusiness_IdOrderByIdAsc(b.getId()).stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", p.getName());
                    m.put("description", p.getDescription());
                    m.put("price", p.getPrice());
                    m.put("category", p.getCategory());
                    m.put("imageUrl", p.getImageUrl());
                    return m;
                })
                .toList();
        root.put("products", products);

        List<Map<String, Object>> social = businessSocialLinkRepository.findByBusiness_IdOrderByIdAsc(b.getId()).stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("platform", s.getPlatform());
                    m.put("url", s.getUrl());
                    return m;
                })
                .toList();
        root.put("socialLinks", social);

        List<Map<String, Object>> docs = businessDocumentRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fileName", d.getFileName());
                    m.put("category", d.getCategory() != null ? d.getCategory().name() : "GENERAL");
                    m.put("description", d.getDescription());
                    if (d.getExtractedText() != null && !d.getExtractedText().isBlank()) {
                        m.put("contentSummary", d.getExtractedText());
                    }
                    return m;
                })
                .toList();
        root.put("documents", docs);

        return root;
    }

    private ChatSessionDto toSessionDto(ChatSession s) {
        ChatSessionDto dto = new ChatSessionDto();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    private ChatMessageDto toMessageDto(ChatMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        dto.setSender(m.getSender().name());
        dto.setMessage(m.getMessage());
        dto.setAction(m.getAction());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    public char[] createSession() {
        return new char[0];
    }

    public List<String> getQuickReplies(String id) {
        return List.of();
    }

    public List<com.backend.dto.ChatMessage> getConversation(String id) {
        return List.of();
    }

    public String getBriefSummary(String id) {
        return id;
    }
}
