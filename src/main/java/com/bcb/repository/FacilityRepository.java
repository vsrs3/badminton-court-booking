package com.bcb.repository;

import com.bcb.model.Facility;
import java.util.List;
import java.util.Optional;

public interface FacilityRepository {

    /**
     * Find all active facilities with infinite scroll
     *
     * @param offset Starting position
     * @param limit Number of records to fetch
     * @return List of facilities
     */
    List<Facility> findAllWithPagination(int offset, int limit);

    /**
     * Find facility by ID
     *
     * @param facilityId Facility ID
     * @return Optional containing facility if found
     */
    Optional<Facility> findById(Integer facilityId);

    /**
     * Get total count of active facilities
     *
     * @return Total count
     */
    int getTotalCount();

    /**
     * Find thumbnail image path for a facility
     *
     * @param facilityId Facility ID
     * @return Image path or null if not found
     */
    String findThumbnailPath(Integer facilityId);

    /**
     * Get average rating for a facility
     *
     * @param facilityId Facility ID
     * @return Average rating (0.0 if no reviews)
     */
    Double getAverageRating(Integer facilityId);
}