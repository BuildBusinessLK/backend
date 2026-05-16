package com.backend.repository;

import com.backend.entity.BusinessSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessSocialLinkRepository extends JpaRepository<BusinessSocialLink, Long> {

    List<BusinessSocialLink> findByBusiness_IdOrderByIdAsc(Long businessId);

    void deleteByBusiness_Id(Long businessId);
}
