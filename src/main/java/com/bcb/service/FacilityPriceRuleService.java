package com.bcb.service;

import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.PriceRuleRequestDTO;
import com.bcb.exception.BusinessException;

public interface FacilityPriceRuleService {

    /**
     * Get price configuration view for a facility.
     * Prices returned are per hour (multiplied by 2 for display).
     */
    FacilityPriceViewDTO getPriceView(int facilityId, Integer courtTypeId, String dayType) throws BusinessException;

    /**
     * Create a new price rule.
     * Input price should be per hour, will be converted to per 30 minutes.
     */
    void createPriceRule(PriceRuleRequestDTO request) throws BusinessException;

    /**
     * Update an existing price rule.
     * Input price should be per hour, will be converted to per 30 minutes.
     */
    void updatePriceRule(PriceRuleRequestDTO request) throws BusinessException;

    /**
     * Delete a price rule.
     */
    void deletePriceRule(int priceId) throws BusinessException;
}

