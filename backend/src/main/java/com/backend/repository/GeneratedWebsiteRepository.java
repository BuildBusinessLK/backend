package com.backend.repository;

import com.backend.domain.WebsiteStatus;
import com.backend.entity.GeneratedWebsite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedWebsiteRepository extends JpaRepository<GeneratedWebsite, Long> {

    List<GeneratedWebsite> findByBusiness_IdOrderByUpdatedAtDesc(Long businessId);

    Optional<GeneratedWebsite> findFirstByBusiness_IdOrderByUpdatedAtDesc(Long businessId);

    /** Latest record with a specific status — used to find the active PUBLISHED site */
    Optional<GeneratedWebsite> findFirstByBusiness_IdAndStatusOrderByUpdatedAtDesc(Long businessId, WebsiteStatus status);
}
