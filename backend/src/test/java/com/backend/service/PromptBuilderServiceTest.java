package com.backend.service;

import com.backend.domain.Sector;
import com.backend.dto.AdsGenerationRequest;
import com.backend.dto.business.BusinessDetailDto;
import com.backend.dto.business.BusinessProductDto;
import com.backend.dto.business.BusinessSocialLinkDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceTest {

    private final PromptBuilderService promptBuilderService = new PromptBuilderService();

    @Test
    void buildAdPromptIncludesUserProfileAndSocialLinks() {
        BusinessDetailDto business = new BusinessDetailDto();
        business.setBusinessName("Kithul House");
        business.setSector(Sector.KITHUL);
        business.setBusinessDescription("Premium kithul products");
        business.setTargetMarket("Families in Colombo");
        business.setMarketingGoals("Increase online orders");
        business.setMonthlyProduction(new BigDecimal("500"));

        BusinessProductDto product = new BusinessProductDto();
        product.setName("Kithul Treacle");
        product.setCategory("Food");
        product.setDescription("Natural sweetener");
        business.setProducts(List.of(product));

        List<BusinessSocialLinkDto> socialLinks = List.of(
                socialLink("facebook", "https://facebook.com/kithulhouse"),
                socialLink("instagram", "https://instagram.com/kithulhouse")
        );

        Map<String, Object> userProfile = Map.of(
                "fullName", "Nimal Perera",
                "preferredLanguage", "English",
                "district", "Colombo"
        );

        AdsGenerationRequest request = new AdsGenerationRequest();
        request.setIdea("Launch a festive offer");
        request.setTone("friendly");
        request.setPlatform("instagram");
        request.setWebsite("https://kithulhouse.lk");

        String prompt = promptBuilderService.buildAdPrompt(business, request, socialLinks, userProfile);

        assertThat(prompt)
                .contains("Kithul House")
                .contains("Nimal Perera")
                .contains("https://facebook.com/kithulhouse")
                .contains("https://instagram.com/kithulhouse")
                .contains("festive offer")
                .contains("friendly")
                .contains("instagram")
                .contains("https://kithulhouse.lk");
    }

    private BusinessSocialLinkDto socialLink(String platform, String url) {
        BusinessSocialLinkDto dto = new BusinessSocialLinkDto();
        dto.setPlatform(platform);
        dto.setUrl(url);
        return dto;
    }
}
