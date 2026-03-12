package com.bcb.dto;

import java.util.List;

/**
 * Request body for PUT /owner/prices/smart-config
 * Carries all price rows for a facility in one shot.
 */
public class SmartPriceConfigRequestDTO {

    private int facilityId;
    private List<SmartPriceConfigRowDTO> priceConfigs;

    public SmartPriceConfigRequestDTO() {}

    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }

    public List<SmartPriceConfigRowDTO> getPriceConfigs() { return priceConfigs; }
    public void setPriceConfigs(List<SmartPriceConfigRowDTO> priceConfigs) { this.priceConfigs = priceConfigs; }
}
