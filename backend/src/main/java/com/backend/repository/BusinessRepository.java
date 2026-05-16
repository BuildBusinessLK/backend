package com.backend.repository;

import com.backend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByOwner_Id(Long ownerId);

    Optional<Business> findByWebsiteSlugIgnoreCase(String slug);

    Optional<Business> findByIdAndOwner_Id(Long id, Long ownerId);

    boolean existsByWebsiteSlugIgnoreCase(String slug);
}
