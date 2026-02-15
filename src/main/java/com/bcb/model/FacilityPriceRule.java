package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Entity representing a price rule for a facility.
 * Price is stored for 30-minute intervals (price per 30 minutes).
 */
public class FacilityPriceRule {
    private Integer priceId;
    private Integer facilityId;
    private Integer courtTypeId;
    private String dayType; // WEEKDAY or WEEKEND
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price; // Price for 30 minutes

    public FacilityPriceRule() {}

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
