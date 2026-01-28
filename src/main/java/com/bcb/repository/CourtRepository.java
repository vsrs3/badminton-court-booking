package com.bcb.repository;

import com.bcb.model.Court;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Court entity.
 * Defines CRUD and query operations for courts.
 */
public interface CourtRepository {

    /**
     * Find all active courts for a facility.
     * @param facilityId Facility ID
     * @return List of courts
     */
    List<Court> findByFacility(int facilityId);


    /**
     * Find court by ID.
     * @param courtId Court ID
     * @return Optional containing court or empty
     */
    Optional<Court> findById(int courtId);



    /**
     * Insert a new court.
     * @param court Court to insert
     * @return Generated court ID
     */
    int insert(Court court);

    /**
     * Update existing court.
     * @param court Court to update
     * @return Number of rows affected
     */
    int update(Court court);

    /**
     * Soft deactivate court (set is_active = 0).
     * @param courtId Court ID
     * @return Number of rows affected
     */
    int deactivate(int courtId);


    /**
     * Check if court has active bookings.
     * @param courtId Court ID
     * @return true if has active/future bookings
     */
    boolean hasActiveBookings(int courtId);

    /**
     * Count courts in a facility.
     * @param facilityId Facility ID
     * @return Total count
     */
    int countByFacility(int facilityId);
}
