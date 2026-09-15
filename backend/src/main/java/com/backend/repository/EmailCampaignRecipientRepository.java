package com.backend.repository;

import com.backend.domain.DeliveryStatus;
import com.backend.entity.EmailCampaignRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailCampaignRecipientRepository extends JpaRepository<EmailCampaignRecipient, Long> {
    List<EmailCampaignRecipient> findByCampaign_Id(Long campaignId);
    List<EmailCampaignRecipient> findByCampaign_IdAndStatus(Long campaignId, DeliveryStatus status);
    long countByCampaign_Id(Long campaignId);
    long countByCampaign_IdAndStatus(Long campaignId, DeliveryStatus status);
    void deleteByCampaign_Id(Long campaignId);
}
