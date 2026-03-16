package com.bcb.dto;

import java.math.BigDecimal;

/**
 * One row of the smart price config table.
 * Each row represents a time range with prices for all 4 combinations
 * (NORMAL/VIP × WEEKDAY/WEEKEND).
 * Prices are per-hour (frontend input). Backend converts to per-30-min before saving.
 */
public class SmartPriceConfigRowDTO {

    private String startTime;            // "HH:mm"
    private String endTime;              // "HH:mm"
    private BigDecimal normalWeekdayPrice;
    private BigDecimal normalWeekendPrice;
    private BigDecimal vipWeekdayPrice;
    private BigDecimal vipWeekendPrice;

    public SmartPriceConfigRowDTO() {}

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public BigDecimal getNormalWeekdayPrice() { return normalWeekdayPrice; }
    public void setNormalWeekdayPrice(BigDecimal normalWeekdayPrice) { this.normalWeekdayPrice = normalWeekdayPrice; }

    public BigDecimal getNormalWeekendPrice() { return normalWeekendPrice; }
    public void setNormalWeekendPrice(BigDecimal normalWeekendPrice) { this.normalWeekendPrice = normalWeekendPrice; }

    public BigDecimal getVipWeekdayPrice() { return vipWeekdayPrice; }
    public void setVipWeekdayPrice(BigDecimal vipWeekdayPrice) { this.vipWeekdayPrice = vipWeekdayPrice; }

    public BigDecimal getVipWeekendPrice() { return vipWeekendPrice; }
    public void setVipWeekendPrice(BigDecimal vipWeekendPrice) { this.vipWeekendPrice = vipWeekendPrice; }
}
