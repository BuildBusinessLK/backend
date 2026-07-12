package com.backend.dto.business;

public class DailyAnalyticsDto {
    private String date;
    private long whatsappClicks;
    private long directionsClicks;

    public DailyAnalyticsDto() {}

    public DailyAnalyticsDto(String date, long whatsappClicks, long directionsClicks) {
        this.date = date;
        this.whatsappClicks = whatsappClicks;
        this.directionsClicks = directionsClicks;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getWhatsappClicks() {
        return whatsappClicks;
    }

    public void setWhatsappClicks(long whatsappClicks) {
        this.whatsappClicks = whatsappClicks;
    }

    public long getDirectionsClicks() {
        return directionsClicks;
    }

    public void setDirectionsClicks(long directionsClicks) {
        this.directionsClicks = directionsClicks;
    }
}
