package com.backend.dto.business;

import com.backend.domain.Sector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class BusinessUpsertRequest {

    @NotBlank
    private String businessName;

    @NotNull
    private Sector sector;

    private String websiteSlug;

    private String businessDescription;
    private String targetMarket;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyProduction;
    private String marketingGoals;

    private List<BusinessProductDto> products;
    private List<BusinessSocialLinkDto> socialLinks;

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
}
