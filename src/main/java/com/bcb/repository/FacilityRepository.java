package com.bcb.repository;

import com.bcb.model.Facility;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Facility data access
 * Data Access Object interface for Facility entity.
 * Defines CRUD and query operations for facilities.
 *
 * Note: This is a single-owner system. All facilities belong to the admin.
 * No accountId/ownerId filtering is required.
 */
public interface FacilityRepository {

    /**
     * Find all facilities with pagination.
     * @param limit Number of records to return
     * @param offset Starting position
     * @return List of facilities
     */
    List<Facility> findAll(int limit, int offset);

    /**
     * Count total facilities.
     * @return Total count
     */
    int count();


    /**
     * Search facilities by keyword across name and location fields.
     * @param keyword Keyword to search
     * @param limit Number of records to return
     * @param offset Starting position
     * @return List of matching facilities
     */
    List<Facility> findByKeyword(String keyword, int limit, int offset);

    /**
     * Count facilities by keyword search across name and location fields.
     * @param keyword Keyword to search
     * @return Total count
     * Find all active facilities with pagination
     * Returns PURE entities (no computed fields)
     */
    int countByKeyword(String keyword);
    List<Facility> findAllWithPagination(int offset, int limit);

    /**
     * Find facility by ID.
     * @param facilityId Facility ID
     * @return Optional containing facility or empty
     * Find facility by ID
     * Returns PURE entity
     */
    Optional<Facility> findById(int facilityId);
    Optional<Facility> findById(Integer facilityId);

    /**
     * Insert a new facility.
     * @param facility Facility to insert
     * @return Generated facility ID
     * Get total count of active facilities
     */
    int insert(Facility facility);
    int getTotalCount();

    /**
     * Update existing facility.
     * @param facility Facility to update
     * @return Number of rows affected
     * ✅ NEW: Find thumbnail image path for a facility
     * (Separated from entity - queried when needed)
     */
    int update(Facility facility);
    String findThumbnailPath(Integer facilityId);

    /**
     * Soft delete facility (set is_active = 0).
     * @param facilityId Facility ID
     * @return Number of rows affected
     */
    int softDelete(int facilityId);

    Double getAverageRating(Integer facilityId);
}
