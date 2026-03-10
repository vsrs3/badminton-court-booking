package com.bcb.service;

import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.PriceRuleRequestDTO;
import com.bcb.dto.SmartPriceConfigRequestDTO;
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

    /**
     * Smart config: validates all rows, then replaces ALL price rules for the
     * facility atomically (DELETE all → INSERT new) inside one transaction.
     *
     * @param request facilityId + list of config rows (prices per hour)
     * @throws BusinessException on validation failure
     */
    void saveSmartPriceConfig(SmartPriceConfigRequestDTO request) throws BusinessException;
}

