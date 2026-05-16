package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebsiteCopyResponse {

    private String heroText;
    private String aboutText;
    private String marketingText;

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
}
