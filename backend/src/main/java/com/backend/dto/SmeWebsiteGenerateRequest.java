package com.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class SmeWebsiteGenerateRequest {

    private String businessName;
    private String tagline;
    /** Coconut, Kithul, Palmyrah, or free text */
    private String industry;
    private String about;
    private List<SmeProductItem> products = new ArrayList<>();
    private SmeContactDto contact;
    private SmeSocialDto social;
    private List<String> galleryImageUrls = new ArrayList<>();
    /** forest | ocean | amber */
    private String theme = "forest";
    private Boolean useAi = Boolean.TRUE;
    private Boolean useFallback = Boolean.FALSE;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<SmeProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<SmeProductItem> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    public SmeContactDto getContact() {
        return contact;
    }

    public void setContact(SmeContactDto contact) {
        this.contact = contact;
    }

    public SmeSocialDto getSocial() {
        return social;
    }

    public void setSocial(SmeSocialDto social) {
        this.social = social;
    }

    public List<String> getGalleryImageUrls() {
        return galleryImageUrls;
    }

    public void setGalleryImageUrls(List<String> galleryImageUrls) {
        this.galleryImageUrls = galleryImageUrls != null ? galleryImageUrls : new ArrayList<>();
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Boolean getUseAi() {
        return useAi;
    }

    public void setUseAi(Boolean useAi) {
        this.useAi = useAi;
    }

    public Boolean getUseFallback() {
        return useFallback;
    }

    public void setUseFallback(Boolean useFallback) {
        this.useFallback = useFallback;
    }
}
