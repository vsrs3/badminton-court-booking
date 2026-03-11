package com.bcb.repository.booking;

import com.bcb.model.Court;

import java.util.List;

/**
 * Repository interface for Court lookup in single-booking context.
 *
 * @author AnhTN
 */
public interface CourtRepository {

    /**
     * Finds all active courts belonging to a facility.
     */
    List<Court> findActiveByFacilityId(int facilityId);
}

