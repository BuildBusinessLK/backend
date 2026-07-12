package com.backend.service;

import com.backend.dto.business.*;
import com.backend.entity.*;
import com.backend.repository.*;
import com.backend.util.SmeSlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProductRepository businessProductRepository;
    private final BusinessSocialLinkRepository businessSocialLinkRepository;
    private final UserRepository userRepository;
    private final ClickEventRepository clickEventRepository;

    public BusinessService(
            BusinessRepository businessRepository,
            BusinessProfileRepository businessProfileRepository,
            BusinessProductRepository businessProductRepository,
            BusinessSocialLinkRepository businessSocialLinkRepository,
            UserRepository userRepository,
            ClickEventRepository clickEventRepository) {
        this.businessRepository = businessRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.businessProductRepository = businessProductRepository;
        this.businessSocialLinkRepository = businessSocialLinkRepository;
        this.userRepository = userRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessDetailDto> listMine(Long userId) {
        return businessRepository.findByOwner_Id(userId).stream().map(this::toDetail).toList();
    }

    @Transactional(readOnly = true)
    public BusinessDetailDto getMine(Long userId, Long businessId) {
        Business b = businessRepository.findByIdAndOwner_Id(businessId, userId).orElseThrow();
        return toDetail(b);
    }

    @Transactional
    public BusinessDetailDto create(Long userId, BusinessUpsertRequest req) {
        List<Business> existing = businessRepository.findByOwner_Id(userId);
        if (!existing.isEmpty()) {
            throw new IllegalStateException("A business already exists for this account. Update it instead.");
        }
        User owner = userRepository.findById(userId).orElseThrow();
        Business b = new Business();
        b.setOwner(owner);
        b.setBusinessName(req.getBusinessName().trim());
        b.setSector(req.getSector());
        String slug = resolveSlug(req.getWebsiteSlug(), req.getBusinessName());
        if (businessRepository.existsByWebsiteSlugIgnoreCase(slug) || SmeSlugUtil.isReserved(slug)) {
            throw new IllegalStateException("Website slug is not available.");
        }
        b.setWebsiteSlug(slug);
        b = businessRepository.save(b);

        BusinessProfile bp = new BusinessProfile();
        bp.setBusiness(b);
        applyProfile(bp, req);
        businessProfileRepository.save(bp);

        replaceProducts(b, req.getProducts());
        replaceSocial(b, req.getSocialLinks());

        return toDetail(businessRepository.findById(b.getId()).orElseThrow());
    }

    @Transactional
    public BusinessDetailDto update(Long userId, Long businessId, BusinessUpsertRequest req) {
        Business b = businessRepository.findByIdAndOwner_Id(businessId, userId).orElseThrow();
        b.setBusinessName(req.getBusinessName().trim());
        b.setSector(req.getSector());
        if (req.getWebsiteSlug() != null && !req.getWebsiteSlug().isBlank()) {
            String slug = SmeSlugUtil.normalize(req.getWebsiteSlug());
            if (slug.isEmpty()) {
                throw new IllegalStateException("Invalid website slug.");
            }
            if (SmeSlugUtil.isReserved(slug)) {
                throw new IllegalStateException("Reserved website slug.");
            }
            if (!slug.equalsIgnoreCase(b.getWebsiteSlug())
                    && businessRepository.existsByWebsiteSlugIgnoreCase(slug)) {
                throw new IllegalStateException("Website slug is already in use.");
            }
            b.setWebsiteSlug(slug);
        }
        Business saved = businessRepository.save(b);

        BusinessProfile bp = businessProfileRepository.findByBusiness_Id(saved.getId()).orElseGet(() -> {
            BusinessProfile p = new BusinessProfile();
            p.setBusiness(saved);
            return p;
        });
        applyProfile(bp, req);
        businessProfileRepository.save(bp);

        businessProductRepository.deleteByBusiness_Id(saved.getId());
        businessSocialLinkRepository.deleteByBusiness_Id(saved.getId());
        replaceProducts(saved, req.getProducts());
        replaceSocial(saved, req.getSocialLinks());

        return toDetail(saved);
    }

    private void applyProfile(BusinessProfile bp, BusinessUpsertRequest req) {
        bp.setBusinessDescription(trim(req.getBusinessDescription()));
        bp.setTargetMarket(trim(req.getTargetMarket()));
        bp.setMonthlyIncome(req.getMonthlyIncome());
        bp.setMonthlyProduction(req.getMonthlyProduction());
        bp.setMarketingGoals(trim(req.getMarketingGoals()));
        bp.setBusinessHoursOpen(trim(req.getBusinessHoursOpen()));
        bp.setBusinessHoursClose(trim(req.getBusinessHoursClose()));
        bp.setWorkingDays(trim(req.getWorkingDays()));
        bp.setGoogleMapsUrl(trim(req.getGoogleMapsUrl()));
        bp.setIntentMessage(trim(req.getIntentMessage()));
    }


    private void replaceProducts(Business b, List<BusinessProductDto> items) {
        if (items == null) {
            return;
        }
        for (BusinessProductDto d : items) {
            if (d.getName() == null || d.getName().isBlank()) {
                continue;
            }
            BusinessProduct p = new BusinessProduct();
            p.setBusiness(b);
            p.setName(d.getName().trim());
            p.setDescription(trim(d.getDescription()));
            p.setPrice(d.getPrice());
            p.setCategory(trim(d.getCategory()));
            p.setImageUrl(trim(d.getImageUrl()));
            businessProductRepository.save(p);
        }
    }

    private void replaceSocial(Business b, List<BusinessSocialLinkDto> items) {
        if (items == null) {
            return;
        }
        for (BusinessSocialLinkDto d : items) {
            if (d.getPlatform() == null || d.getUrl() == null || d.getUrl().isBlank()) {
                continue;
            }
            BusinessSocialLink s = new BusinessSocialLink();
            s.setBusiness(b);
            s.setPlatform(d.getPlatform().trim());
            s.setUrl(d.getUrl().trim());
            businessSocialLinkRepository.save(s);
        }
    }

    private String resolveSlug(String requested, String businessName) {
        if (requested != null && !requested.isBlank()) {
            return SmeSlugUtil.normalize(requested);
        }
        return SmeSlugUtil.normalize(businessName);
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private BusinessDetailDto toDetail(Business b) {
        BusinessDetailDto dto = new BusinessDetailDto();
        dto.setId(b.getId());
        dto.setBusinessName(b.getBusinessName());
        dto.setSector(b.getSector());
        dto.setWebsiteSlug(b.getWebsiteSlug());
        businessProfileRepository.findByBusiness_Id(b.getId()).ifPresent(bp -> {
            dto.setBusinessDescription(bp.getBusinessDescription());
            dto.setTargetMarket(bp.getTargetMarket());
            dto.setMonthlyIncome(bp.getMonthlyIncome());
            dto.setMonthlyProduction(bp.getMonthlyProduction());
            dto.setMarketingGoals(bp.getMarketingGoals());
            dto.setBusinessHoursOpen(bp.getBusinessHoursOpen());
            dto.setBusinessHoursClose(bp.getBusinessHoursClose());
            dto.setWorkingDays(bp.getWorkingDays());
            dto.setGoogleMapsUrl(bp.getGoogleMapsUrl());
            dto.setIntentMessage(bp.getIntentMessage());
        });

        List<BusinessProductDto> products = new ArrayList<>();
        for (BusinessProduct p : businessProductRepository.findByBusiness_IdOrderByIdAsc(b.getId())) {
            BusinessProductDto d = new BusinessProductDto();
            d.setId(p.getId());
            d.setName(p.getName());
            d.setDescription(p.getDescription());
            d.setPrice(p.getPrice());
            d.setCategory(p.getCategory());
            d.setImageUrl(p.getImageUrl());
            products.add(d);
        }
        dto.setProducts(products);
        List<BusinessSocialLinkDto> social = new ArrayList<>();
        for (BusinessSocialLink s : businessSocialLinkRepository.findByBusiness_IdOrderByIdAsc(b.getId())) {
            BusinessSocialLinkDto d = new BusinessSocialLinkDto();
            d.setId(s.getId());
            d.setPlatform(s.getPlatform());
            d.setUrl(s.getUrl());
            social.add(d);
        }
        dto.setSocialLinks(social);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DailyAnalyticsDto> getAnalytics(Long userId, Long businessId) {
        Business b = businessRepository.findByIdAndOwner_Id(businessId, userId).orElseThrow();
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<ClickEvent> events = clickEventRepository.findByBusiness_IdAndTimestampAfter(b.getId(), since);

        java.util.Map<String, DailyAnalyticsDto> map = new java.util.LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            String dateStr = java.time.LocalDate.now().minusDays(i).toString();
            map.put(dateStr, new DailyAnalyticsDto(dateStr, 0, 0));
        }

        for (ClickEvent e : events) {
            String dateStr = e.getTimestamp().toLocalDate().toString();
            DailyAnalyticsDto dto = map.get(dateStr);
            if (dto != null) {
                if ("whatsapp_click".equals(e.getEventType())) {
                    dto.setWhatsappClicks(dto.getWhatsappClicks() + 1);
                } else if ("directions_click".equals(e.getEventType())) {
                    dto.setDirectionsClicks(dto.getDirectionsClicks() + 1);
                }
            }
        }

        return new ArrayList<>(map.values());
    }
}
