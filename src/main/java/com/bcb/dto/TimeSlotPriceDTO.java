package com.bcb.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO for displaying price information in the UI.
 * The price field contains price per hour (already multiplied by 2 from DB).
 */
public class TimeSlotPriceDTO {
    private Integer priceId; // For update operations
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price; // Price per hour (for display)
    private String startTimeFormatted;
    private String endTimeFormatted;

    public Integer getPriceId() {
        return priceId;
    }

    public void setPriceId(Integer priceId) {
        this.priceId = priceId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public void setStartTimeFormatted(String startTimeFormatted) {
        this.startTimeFormatted = startTimeFormatted;
    }

    public String getEndTimeFormatted() {
        return endTimeFormatted;
    }

    public void setEndTimeFormatted(String endTimeFormatted) {
        this.endTimeFormatted = endTimeFormatted;
    }
}

