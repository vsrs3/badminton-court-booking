package com.bcb.repository.booking;

import com.bcb.model.Facility;

import java.util.Optional;

/**
 * Repository interface for Facility lookup in single-booking context.
 *
 * @author AnhTN
 */
public interface FacilityRepository {

    /**
     * Finds an active facility by ID.
     */
    Optional<Facility> findActiveById(int facilityId);
}

