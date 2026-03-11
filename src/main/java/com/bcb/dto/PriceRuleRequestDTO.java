package com.bcb.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO for creating or updating a price rule.
 * The pricePerHour will be converted to price per 30 minutes before storing in DB.
 */
public class PriceRuleRequestDTO {
    private Integer priceId; // null for create, not null for update
    private Integer facilityId;
    private Integer courtTypeId;
    private String dayType; // WEEKDAY or WEEKEND
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal pricePerHour; // Input from UI (price for 1 hour)

    public PriceRuleRequestDTO() {}

    public Integer getPriceId() {
        return priceId;
    }

    public void setPriceId(Integer priceId) {
        this.priceId = priceId;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public Integer getCourtTypeId() {
        return courtTypeId;
    }

    public void setCourtTypeId(Integer courtTypeId) {
        this.courtTypeId = courtTypeId;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
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

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
}

