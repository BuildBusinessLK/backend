package com.backend.service;

import com.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final GeneratedWebsiteRepository generatedWebsiteRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KnowledgeSourceRepository knowledgeSourceRepository;

    public AdminService(
            UserRepository userRepository,
            BusinessRepository businessRepository,
            GeneratedWebsiteRepository generatedWebsiteRepository,
            ChatMessageRepository chatMessageRepository,
            KnowledgeSourceRepository knowledgeSourceRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.generatedWebsiteRepository = generatedWebsiteRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.knowledgeSourceRepository = knowledgeSourceRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("totalUsers", userRepository.count());
        m.put("totalBusinesses", businessRepository.count());
        m.put("totalGeneratedWebsites", generatedWebsiteRepository.count());
        m.put("totalChatMessages", chatMessageRepository.count());
        m.put("totalKnowledgeSources", knowledgeSourceRepository.count());
        return m;
    }
}
