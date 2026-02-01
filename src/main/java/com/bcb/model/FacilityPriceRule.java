package com.bcb.model;

import java.math.BigDecimal;

public class FacilityPriceRule {
    private int priceId;
    private int facilityId;
    private int courtTypeId;
    private String dayType;
    private int slotId;
    private BigDecimal price;

    public FacilityPriceRule() {}

    public FacilityPriceRule(int priceId, int facilityId, int courtTypeId, String dayType, int slotId, BigDecimal price) {
        this.priceId = priceId;
        this.facilityId = facilityId;
        this.courtTypeId = courtTypeId;
        this.dayType = dayType;
        this.slotId = slotId;
        this.price = price;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public int getCourtTypeId() {
        return courtTypeId;
    }

    public void setCourtTypeId(int courtTypeId) {
        this.courtTypeId = courtTypeId;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
