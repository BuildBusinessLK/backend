package com.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeasibilityDto {
    private int capitalFit;
    private int yieldFit;
    private int staffingFit;

    public FeasibilityDto() {}

    public FeasibilityDto(int capitalFit, int yieldFit, int staffingFit) {
        this.capitalFit = capitalFit;
        this.yieldFit = yieldFit;
        this.staffingFit = staffingFit;
    }

    public int getCapitalFit() {
        return capitalFit;
    }

    public void setCapitalFit(int capitalFit) {
        this.capitalFit = capitalFit;
    }

    public int getYieldFit() {
        return yieldFit;
    }

    public void setYieldFit(int yieldFit) {
        this.yieldFit = yieldFit;
    }

    public int getStaffingFit() {
        return staffingFit;
    }

    public void setStaffingFit(int staffingFit) {
        this.staffingFit = staffingFit;
    }
}
