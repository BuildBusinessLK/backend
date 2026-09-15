package com.backend.dto.email;

import com.backend.domain.CampaignStatus;

public class CampaignSimulationResponse {
    private Long campaignId;
    private CampaignStatus status;
    private int totalRecipients;
    private int deliveredCount;
    private int openedCount;
    private int clickedCount;
    private int failedCount;
    private double deliveryRate;
    private double openRate;
    private double clickRate;
    private String message;

    public CampaignSimulationResponse() {}

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

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

    public double getDeliveryRate() { return deliveryRate; }
    public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }

    public double getOpenRate() { return openRate; }
    public void setOpenRate(double openRate) { this.openRate = openRate; }

    public double getClickRate() { return clickRate; }
    public void setClickRate(double clickRate) { this.clickRate = clickRate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
