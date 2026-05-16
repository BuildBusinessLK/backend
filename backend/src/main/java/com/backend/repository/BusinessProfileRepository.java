package com.backend.repository;

import com.backend.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByBusiness_Id(Long businessId);

    void deleteByBusiness_Id(Long businessId);
}
