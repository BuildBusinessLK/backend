package com.backend.entity;

import com.backend.domain.CampaignGoal;
import com.backend.domain.CampaignStatus;
import com.backend.domain.Sector;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_campaigns", indexes = {
    @Index(name = "idx_email_campaigns_user", columnList = "user_id"),
    @Index(name = "idx_email_campaigns_status", columnList = "status")
})
public class EmailCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_goal", length = 50)
    private CampaignGoal goal = CampaignGoal.GENERAL_ANNOUNCEMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_sector", length = 50)
    private Sector targetSector;

    @Column(name = "target_audience_summary", length = 500)
    private String targetAudienceSummary;

    @Column(nullable = false, length = 300)
    private String subject;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "total_recipients", nullable = false)
    private int totalRecipients = 0;

    @Column(name = "delivered_count", nullable = false)
    private int deliveredCount = 0;

    @Column(name = "opened_count", nullable = false)
    private int openedCount = 0;

    @Column(name = "clicked_count", nullable = false)
    private int clickedCount = 0;

    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;

    @Column(name = "is_mock", nullable = false)
    private boolean isMock = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public EmailCampaign() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CampaignGoal getGoal() {
        return goal;
    }

    public void setGoal(CampaignGoal goal) {
        this.goal = goal;
    }

    public Sector getTargetSector() {
        return targetSector;
    }

    public void setTargetSector(Sector targetSector) {
        this.targetSector = targetSector;
    }

    public String getTargetAudienceSummary() {
        return targetAudienceSummary;
    }

    public void setTargetAudienceSummary(String targetAudienceSummary) {
        this.targetAudienceSummary = targetAudienceSummary;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public int getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(int totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(int deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public int getOpenedCount() {
        return openedCount;
    }

    public void setOpenedCount(int openedCount) {
        this.openedCount = openedCount;
    }

    public int getClickedCount() {
        return clickedCount;
    }

    public void setClickedCount(int clickedCount) {
        this.clickedCount = clickedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
