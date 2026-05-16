package com.backend.controller;

import com.backend.entity.KnowledgeSource;
import com.backend.repository.KnowledgeSourceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge-sources")
@CrossOrigin(origins = "*")
public class KnowledgeSourceController {

    private final KnowledgeSourceRepository knowledgeSourceRepository;

    public KnowledgeSourceController(KnowledgeSourceRepository knowledgeSourceRepository) {
        this.knowledgeSourceRepository = knowledgeSourceRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<KnowledgeSource> list() {
        return knowledgeSourceRepository.findAll();
    }
}
