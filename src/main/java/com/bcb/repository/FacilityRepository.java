package com.bcb.repository;

import com.bcb.model.Facility;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Facility data access
 */
public interface FacilityRepository {

    /**
     * Find all active facilities with pagination
     * Returns PURE entities (no computed fields)
     */
    List<Facility> findAllWithPagination(int offset, int limit);

    /**
     * Find facility by ID
     * Returns PURE entity
     */
    Optional<Facility> findById(Integer facilityId);

    /**
     * Get total count of active facilities
     */
    int getTotalCount();

    /**
     * ✅ NEW: Find thumbnail image path for a facility
     * (Separated from entity - queried when needed)
     */
    String findThumbnailPath(Integer facilityId);

    /**
     * ✅ NEW: Get average rating for a facility
     * (Computed from reviews - queried when needed)
     */
    Double getAverageRating(Integer facilityId);
}