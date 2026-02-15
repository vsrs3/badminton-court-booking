package com.bcb.repository;

import com.bcb.dto.TimeSlotPriceDTO;
import com.bcb.model.FacilityPriceRule;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface FacilityPriceRuleRepository {

    /**
     * Fetch all price rules for a specific facility, court type, and day type.
     * Prices returned are for 30 minutes (as stored in DB).
     */
    List<FacilityPriceRule> findByContext(int facilityId, int courtTypeId, String dayType);

    /**
     * Find a specific price rule by ID.
     */
    Optional<FacilityPriceRule> findById(int priceId);

    /**
     * Check if there's an overlapping time range in the same context.
     * Returns true if overlap exists.
     * When updating, excludes the record with excludePriceId.
     */
    boolean hasOverlap(int facilityId, int courtTypeId, String dayType,
                       LocalTime startTime, LocalTime endTime, Integer excludePriceId);

    /**
     * Insert a new price rule.
     * Price should be for 30 minutes.
     */
    void insert(FacilityPriceRule priceRule);

    /**
     * Update an existing price rule.
     * Price should be for 30 minutes.
     */
    void update(FacilityPriceRule priceRule);

    /**
     * Delete a price rule by ID.
     */
    void delete(int priceId);
}
