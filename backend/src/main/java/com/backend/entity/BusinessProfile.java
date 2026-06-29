package com.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_profiles")
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Column(name = "business_description", columnDefinition = "TEXT")
    private String businessDescription;

    @Column(name = "target_market", length = 255)
    private String targetMarket;

    @Column(name = "monthly_income", precision = 14, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "monthly_production", precision = 14, scale = 2)
    private BigDecimal monthlyProduction;

    @Column(name = "marketing_goals", columnDefinition = "TEXT")
    private String marketingGoals;

    @Column(name = "business_hours_open", length = 20)
    private String businessHoursOpen;

    @Column(name = "business_hours_close", length = 20)
    private String businessHoursClose;

    @Column(name = "working_days", length = 255)
    private String workingDays;

    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getTargetMarket() {
        return targetMarket;
    }

    public void setTargetMarket(String targetMarket) {
        this.targetMarket = targetMarket;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyProduction() {
        return monthlyProduction;
    }

    public void setMonthlyProduction(BigDecimal monthlyProduction) {
        this.monthlyProduction = monthlyProduction;
    }

    public String getMarketingGoals() {
        return marketingGoals;
    }

    public void setMarketingGoals(String marketingGoals) {
        this.marketingGoals = marketingGoals;
    }

    public String getBusinessHoursOpen() {
        return businessHoursOpen;
    }

    public void setBusinessHoursOpen(String businessHoursOpen) {
        this.businessHoursOpen = businessHoursOpen;
    }

    public String getBusinessHoursClose() {
        return businessHoursClose;
    }

    public void setBusinessHoursClose(String businessHoursClose) {
        this.businessHoursClose = businessHoursClose;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
