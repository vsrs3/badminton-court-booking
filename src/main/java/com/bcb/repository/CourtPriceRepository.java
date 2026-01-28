package com.bcb.repository;

import com.bcb.model.CourtPrice;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for CourtPrice entity.
 * Defines CRUD and query operations for court prices.
 */
public interface CourtPriceRepository {

    /**
     * Find all prices for a court.
     * @param courtId Court ID
     * @return List of prices
     */
    List<CourtPrice> findByCourtId(int courtId);

    /**
     * Find price by ID.
     * @param priceId Price ID
     * @return Optional containing price or empty
     */
    Optional<CourtPrice> findById(int priceId);

    /**
     * Find price with court ownership check.
     * @param priceId Price ID
     * @param courtId Court ID
     * @return Optional containing price or empty
     */
    Optional<CourtPrice> findByIdAndCourt(int priceId, int courtId);

    /**
     * Insert a new price.
     * @param courtPrice Price to insert
     * @return Generated price ID
     */
    int insert(CourtPrice courtPrice);

    /**
     * Update existing price.
     * @param courtPrice Price to update
     * @return Number of rows affected
     */
    int update(CourtPrice courtPrice);

    /**
     * Delete price.
     * @param priceId Price ID
     * @return Number of rows affected
     */
    int delete(int priceId);

    /**
     * Delete all prices for a court.
     * @param courtId Court ID
     * @return Number of rows affected
     */
    int deleteByCourtId(int courtId);

    /**
     * Check if prices overlap for a court.
     * @param courtId Court ID
     * @param priceId Price ID to exclude (0 if new price)
     * @param startTime Start time
     * @param endTime End time
     * @return true if overlapping
     */
    boolean hasOverlappingTime(int courtId, int priceId, String startTime, String endTime);

    /**
     * Count prices for a court.
     * @param courtId Court ID
     * @return Total count
     */
    int countByCourtId(int courtId);
}
