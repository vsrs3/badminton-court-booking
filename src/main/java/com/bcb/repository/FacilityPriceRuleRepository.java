package com.bcb.repository;

import com.bcb.dto.TimeSlotPriceDTO;
import java.math.BigDecimal;
import java.util.List;

public interface FacilityPriceRuleRepository {

    /**
     * Fetch all time slots with their prices for a specific facility, court type, and day type.
     */
    List<TimeSlotPriceDTO> findTimeSlotPrices(int facilityId, int courtTypeId, String dayType);

    /**
     * Update price for a single slot. Inserts if not exists, updates if exists.
     */
    void upsertPrice(int facilityId, int courtTypeId, String dayType, int slotId, BigDecimal price);

    /**
     * Bulk update prices for multiple slots.
     */
    void bulkUpsertPrices(int facilityId, int courtTypeId, String dayType, List<Integer> slotIds, BigDecimal price);
}
