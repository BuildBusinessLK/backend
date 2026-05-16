package com.backend.dto.publicapi;

import com.backend.domain.Sector;
import com.backend.domain.WebsiteStatus;
import com.backend.dto.business.BusinessProductDto;
import com.backend.dto.business.BusinessSocialLinkDto;

import java.util.List;

public class PublicBusinessResponse {

    private String businessName;
    private Sector sector;
    private String websiteSlug;
    private String businessDescription;
    private String targetMarket;
    private List<BusinessProductDto> products;
    private List<BusinessSocialLinkDto> socialLinks;
    private String heroText;
    private String aboutText;
    private String marketingText;
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;
    private String coverImageUrl;
    private String contactEmail;
    private String phone;
    private String publishedUrl;
    private WebsiteStatus websiteStatus;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getWebsiteSlug() {
        return websiteSlug;
    }

    public void setWebsiteSlug(String websiteSlug) {
        this.websiteSlug = websiteSlug;
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

    public List<BusinessProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<BusinessProductDto> products) {
        this.products = products;
    }

    public List<BusinessSocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<BusinessSocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
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

    public WebsiteStatus getWebsiteStatus() {
        return websiteStatus;
    }

    public void setWebsiteStatus(WebsiteStatus websiteStatus) {
        this.websiteStatus = websiteStatus;
    }
}
