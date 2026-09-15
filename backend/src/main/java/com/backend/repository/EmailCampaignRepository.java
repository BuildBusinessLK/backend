package com.backend.repository;

import com.backend.domain.CampaignStatus;
import com.backend.entity.EmailCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
    List<EmailCampaign> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<EmailCampaign> findByUserIdAndStatus(Long userId, CampaignStatus status);
}
