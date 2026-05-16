package com.backend.entity;

import com.backend.domain.WebsiteStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_websites")
public class GeneratedWebsite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "template_id", nullable = false, length = 80)
    private String templateId;

    @Column(name = "hero_text", columnDefinition = "TEXT")
    private String heroText;

    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Column(name = "marketing_text", columnDefinition = "TEXT")
    private String marketingText;

    @Column(name = "primary_color", length = 32)
    private String primaryColor;

    @Column(name = "secondary_color", length = 32)
    private String secondaryColor;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(length = 40)
    private String phone;

    @Column(name = "published_url", length = 500)
    private String publishedUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebsiteStatus status = WebsiteStatus.DRAFT;

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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getHeroText() {
        return heroText;
    }

    public void setHeroText(String heroText) {
        this.heroText = heroText;
    }

    public String getAboutText() {
        return aboutText;
    }

    public void setAboutText(String aboutText) {
        this.aboutText = aboutText;
    }

    public String getMarketingText() {
        return marketingText;
    }

    public void setMarketingText(String marketingText) {
        this.marketingText = marketingText;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPublishedUrl() {
        return publishedUrl;
    }

    public void setPublishedUrl(String publishedUrl) {
        this.publishedUrl = publishedUrl;
    }

    public WebsiteStatus getStatus() {
        return status;
    }

    public void setStatus(WebsiteStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
