package com.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "website_setups")
public class WebsiteSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String industry;

    @Column(name = "business_description", nullable = false, columnDefinition = "TEXT")
    private String businessDescription;

    @Column(name = "target_audience", nullable = false, columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "website_goal", nullable = false, columnDefinition = "TEXT")
    private String websiteGoal;

    @Column(name = "current_website", columnDefinition = "TEXT")
    private String currentWebsite;

    @Column(name = "contact_form", nullable = false)
    private boolean contactForm;

    @Column(name = "product_showcase", nullable = false)
    private boolean productShowcase;

    @Column(nullable = false)
    private boolean blog;

    @Column(nullable = false)
    private boolean ecommerce;

    @Column(nullable = false)
    private boolean newsletter;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getWebsiteGoal() {
        return websiteGoal;
    }

    public void setWebsiteGoal(String websiteGoal) {
        this.websiteGoal = websiteGoal;
    }

    public String getCurrentWebsite() {
        return currentWebsite;
    }

    public void setCurrentWebsite(String currentWebsite) {
        this.currentWebsite = currentWebsite;
    }

    public boolean isContactForm() {
        return contactForm;
    }

    public void setContactForm(boolean contactForm) {
        this.contactForm = contactForm;
    }

    public boolean isProductShowcase() {
        return productShowcase;
    }

    public void setProductShowcase(boolean productShowcase) {
        this.productShowcase = productShowcase;
    }

    public boolean isBlog() {
        return blog;
    }

    public void setBlog(boolean blog) {
        this.blog = blog;
    }

    public boolean isEcommerce() {
        return ecommerce;
    }

    public void setEcommerce(boolean ecommerce) {
        this.ecommerce = ecommerce;
    }

    public boolean isNewsletter() {
        return newsletter;
    }

    public void setNewsletter(boolean newsletter) {
        this.newsletter = newsletter;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
