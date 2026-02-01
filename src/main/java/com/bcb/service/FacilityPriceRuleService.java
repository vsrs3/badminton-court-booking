package com.bcb.service;

import com.bcb.dto.BulkPriceUpdateRequestDTO;
import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.exception.BusinessException;

public interface FacilityPriceRuleService {

    /**
     * Get price configuration view for a facility.
     */
    FacilityPriceViewDTO getPriceView(int facilityId, Integer courtTypeId, String dayType) throws BusinessException;

    /**
     * Update price for a single time slot.
     */
    void updateSinglePrice(int facilityId, int courtTypeId, String dayType, int slotId, java.math.BigDecimal price) throws BusinessException;

    /**
     * Bulk update prices.
     */
    void bulkUpdatePrices(BulkPriceUpdateRequestDTO request) throws BusinessException;
}
