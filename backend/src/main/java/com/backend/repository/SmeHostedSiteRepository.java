package com.backend.repository;

import com.backend.entity.SmeHostedSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmeHostedSiteRepository extends JpaRepository<SmeHostedSite, Long> {

    Optional<SmeHostedSite> findBySlug(String slug);
}
