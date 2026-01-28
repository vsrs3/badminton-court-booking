package com.bcb.repository;

import com.bcb.model.Facility;
import java.util.List;
import java.util.Optional;

/**
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
     * Search facilities by name (case-insensitive).
     * @param name Facility name
     * @param limit Number of records to return
     * @param offset Starting position
     * @return List of matching facilities
     */
    List<Facility> findByName(String name, int limit, int offset);

    /**
     * Count facilities by name search.
     * @param name Facility name
     * @return Total count
     */
    int countByName(String name);

    /**
     * Search facilities by location.
     * @param address Address to search
     * @param province Province to search (optional)
     * @param district District to search (optional)
     * @param ward Ward to search (optional)
     * @param limit Number of records to return
     * @param offset Starting position
     * @return List of matching facilities
     */
    List<Facility> findByLocation(String address, String province, String district,
                                   String ward, int limit, int offset);

    /**
     * Count facilities by location search.
     * @param address Address to search
     * @param province Province to search (optional)
     * @param district District to search (optional)
     * @param ward Ward to search (optional)
     * @return Total count
     */
    int countByLocation(String address, String province, String district, String ward);

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
     */
    int countByKeyword(String keyword);

    /**
     * Find facility by ID.
     * @param facilityId Facility ID
     * @return Optional containing facility or empty
     */
    Optional<Facility> findById(int facilityId);

    /**
     * Insert a new facility.
     * @param facility Facility to insert
     * @return Generated facility ID
     */
    int insert(Facility facility);

    /**
     * Update existing facility.
     * @param facility Facility to update
     * @return Number of rows affected
     */
    int update(Facility facility);

    /**
     * Soft delete facility (set is_active = 0).
     * @param facilityId Facility ID
     * @return Number of rows affected
     */
    int softDelete(int facilityId);

    /**
     * Check if facility has active bookings.
     * @param facilityId Facility ID
     * @return true if has active/future bookings
     */
    boolean hasActiveBookings(int facilityId);
}
