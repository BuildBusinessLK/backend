package com.backend.dto.email;

import com.backend.domain.CampaignGoal;
import com.backend.domain.CampaignStatus;
import com.backend.domain.Sector;

import java.time.LocalDateTime;

public class EmailCampaignDto {
    private Long id;
    private Long userId;
    private String title;
    private CampaignGoal goal;
    private Sector targetSector;
    private String targetAudienceSummary;
    private String subject;
    private String body;
    private CampaignStatus status;
    private int totalRecipients;
    private int deliveredCount;
    private int openedCount;
    private int clickedCount;
    private int failedCount;
    private double openRate;
    private double clickRate;
    private double deliveryRate;
    private boolean isMock;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public EmailCampaignDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public CampaignGoal getGoal() { return goal; }
    public void setGoal(CampaignGoal goal) { this.goal = goal; }

    public Sector getTargetSector() { return targetSector; }
    public void setTargetSector(Sector targetSector) { this.targetSector = targetSector; }

    public String getTargetAudienceSummary() { return targetAudienceSummary; }
    public void setTargetAudienceSummary(String targetAudienceSummary) { this.targetAudienceSummary = targetAudienceSummary; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public int getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }

    public int getDeliveredCount() { return deliveredCount; }
    public void setDeliveredCount(int deliveredCount) { this.deliveredCount = deliveredCount; }

    public int getOpenedCount() { return openedCount; }
    public void setOpenedCount(int openedCount) { this.openedCount = openedCount; }

    public int getClickedCount() { return clickedCount; }
    public void setClickedCount(int clickedCount) { this.clickedCount = clickedCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public double getOpenRate() { return openRate; }
    public void setOpenRate(double openRate) { this.openRate = openRate; }

    public double getClickRate() { return clickRate; }
    public void setClickRate(double clickRate) { this.clickRate = clickRate; }

    public double getDeliveryRate() { return deliveryRate; }
    public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }

    public boolean isMock() { return isMock; }
    public void setMock(boolean mock) { isMock = mock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
