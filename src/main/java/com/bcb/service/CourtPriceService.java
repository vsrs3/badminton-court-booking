package com.bcb.service;

import com.bcb.exception.BusinessException;
import com.bcb.model.CourtPrice;

import java.util.List;

public interface CourtPriceService {

    /**
     * Get all prices of a court
     */
    List<CourtPrice> getPricesByCourt(int courtId);

    /**
     * Get price by id
     */
    CourtPrice getPriceById(int priceId) throws BusinessException;

    /**
     * Create new court price
     */
    int createPrice(CourtPrice price) throws BusinessException;

    /**
     * Update existing price
     */
    void updatePrice(CourtPrice price) throws BusinessException;

    /**
     * Delete price
     */
    void deletePrice(int priceId) throws BusinessException;
}
