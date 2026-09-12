package com.backend.service;

import org.springframework.stereotype.Service;

@Service
public class AdVisualService {
    private final AiHordeService aiHordeService;

    public AdVisualService(AiHordeService aiHordeService) {
        this.aiHordeService = aiHordeService;
    }

    /**
     * Generates an image through AI Horde and returns the data-URL expected by the frontend.
     */
    public String generate(String prompt, String size) throws Exception {
        return aiHordeService.generateImage(prompt, size);
    }
}