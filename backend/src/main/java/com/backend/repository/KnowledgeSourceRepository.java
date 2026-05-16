package com.backend.repository;

import com.backend.entity.KnowledgeSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSource, Long> {
}
