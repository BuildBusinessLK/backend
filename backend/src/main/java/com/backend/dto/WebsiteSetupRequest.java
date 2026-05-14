package com.backend.dto;

public class WebsiteSetupRequest {
    private String businessName;
    private String industry;
    private String businessDescription;
    private String targetAudience;
    private String websiteGoal;
    private String currentWebsite;
    private Features features;
    private String additionalNotes;

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

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public static class Features {
        private Boolean contactForm;
        private Boolean productShowcase;
        private Boolean blog;
        private Boolean ecommerce;
        private Boolean newsletter;

        public Boolean getContactForm() {
            return contactForm;
        }

        public void setContactForm(Boolean contactForm) {
            this.contactForm = contactForm;
        }

        public Boolean getProductShowcase() {
            return productShowcase;
        }

        public void setProductShowcase(Boolean productShowcase) {
            this.productShowcase = productShowcase;
        }

        public Boolean getBlog() {
            return blog;
        }

        public void setBlog(Boolean blog) {
            this.blog = blog;
        }

        public Boolean getEcommerce() {
            return ecommerce;
        }

        public void setEcommerce(Boolean ecommerce) {
            this.ecommerce = ecommerce;
        }

        public Boolean getNewsletter() {
            return newsletter;
        }

        public void setNewsletter(Boolean newsletter) {
            this.newsletter = newsletter;
        }
    }
}
