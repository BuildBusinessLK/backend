package com.backend.service;

import com.backend.domain.WebsiteStatus;
import com.backend.dto.business.BusinessProductDto;
import com.backend.dto.business.BusinessSocialLinkDto;
import com.backend.dto.publicapi.PublicBusinessResponse;
import com.backend.entity.Business;
import com.backend.entity.BusinessProduct;
import com.backend.entity.BusinessSocialLink;
import com.backend.entity.GeneratedWebsite;
import com.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PublicBusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProductRepository businessProductRepository;
    private final BusinessSocialLinkRepository businessSocialLinkRepository;
    private final GeneratedWebsiteRepository generatedWebsiteRepository;
    private final ClickEventRepository clickEventRepository;

    public PublicBusinessService(
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessProductRepository businessProductRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            GeneratedWebsiteRepository generatedWebsiteRepository,
            ClickEventRepository clickEventRepository) {
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessProductRepository = businessProductRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.generatedWebsiteRepository = generatedWebsiteRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional(readOnly = true)
    public Optional<PublicBusinessResponse> getBySlug(String slug) {
        Optional<Business> ob = businessRepository.findByWebsiteSlugIgnoreCase(slug);
        if (ob.isEmpty()) {
            return Optional.empty();
        }
        Business b = ob.get();
        PublicBusinessResponse dto = new PublicBusinessResponse();
        dto.setBusinessName(b.getBusinessName());
        dto.setSector(b.getSector());
        dto.setWebsiteSlug(b.getWebsiteSlug());
        businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(bp -> {
            dto.setBusinessDescription(bp.getBusinessDescription());
            dto.setTargetMarket(bp.getTargetMarket());
            dto.setBusinessHoursOpen(bp.getBusinessHoursOpen());
            dto.setBusinessHoursClose(bp.getBusinessHoursClose());
            dto.setWorkingDays(bp.getWorkingDays());
            dto.setGoogleMapsUrl(bp.getGoogleMapsUrl());
            dto.setIntentMessage(bp.getIntentMessage());
        });
        dto.setProducts(mapProducts(businessProductRepository.findByBusiness_IdOrderByIdAsc(b.getId())));
        dto.setSocialLinks(mapSocial(businessSocialLinkRepository.findByBusiness_IdOrderByIdAsc(b.getId())));

        return generatedWebsiteRepository
                .findFirstByBusiness_IdOrderByUpdatedAtDesc(b.getId())
                .filter(gw -> gw.getStatus() == WebsiteStatus.PUBLISHED)
                .map(gw -> {
                    applyWebsite(dto, gw);
                    return dto;
                });
    }

    private void applyWebsite(PublicBusinessResponse dto, GeneratedWebsite gw) {
        dto.setHeroText(gw.getHeroText());
        dto.setAboutText(gw.getAboutText());
        dto.setMarketingText(gw.getMarketingText());
        dto.setPrimaryColor(gw.getPrimaryColor());
        dto.setSecondaryColor(gw.getSecondaryColor());
        dto.setLogoUrl(gw.getLogoUrl());
        dto.setCoverImageUrl(gw.getCoverImageUrl());
        if (gw.getContactEmail() != null) {
            dto.setContactEmail(gw.getContactEmail());
        }
        if (gw.getPhone() != null) {
            dto.setPhone(gw.getPhone());
        }
        dto.setPublishedUrl(gw.getPublishedUrl());
        dto.setWebsiteStatus(gw.getStatus());
    }

    private List<BusinessProductDto> mapProducts(List<BusinessProduct> list) {
        List<BusinessProductDto> out = new ArrayList<>();
        for (BusinessProduct p : list) {
            BusinessProductDto d = new BusinessProductDto();
            d.setId(p.getId());
            d.setName(p.getName());
            d.setDescription(p.getDescription());
            d.setPrice(p.getPrice());
            d.setCategory(p.getCategory());
            d.setImageUrl(p.getImageUrl());
            out.add(d);
        }
        return out;
    }

    private List<BusinessSocialLinkDto> mapSocial(List<BusinessSocialLink> list) {
        List<BusinessSocialLinkDto> out = new ArrayList<>();
        for (BusinessSocialLink s : list) {
            BusinessSocialLinkDto d = new BusinessSocialLinkDto();
            d.setId(s.getId());
            d.setPlatform(s.getPlatform());
            d.setUrl(s.getUrl());
            out.add(d);
        }
        return out;
    }

    @Transactional
    public void recordClick(String slug, String eventType) {
        Business b = businessRepository.findByWebsiteSlugIgnoreCase(slug)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with slug: " + slug));
        com.backend.entity.ClickEvent event = new com.backend.entity.ClickEvent();
        event.setBusiness(b);
        event.setEventType(eventType);
        event.setTimestamp(java.time.LocalDateTime.now());
        clickEventRepository.save(event);
    }
}
