package com.bcb.service;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import java.util.List;

/**
 * Service interface for Facility business operations.
 * Defines high-level facility operations for admin.
 *
 * Note: This is a single-owner system. All facilities belong to the admin.
 */
public interface FacilityService {

    /**
     * Get paginated list of all facilities.
     * @param limit Records per page
     * @param offset Starting position
     * @return List of facilities
     */
    List<Facility> findAll(int limit, int offset);

    /**
     * Get total count of all facilities.
     * @return Total count
     */
    int count();

    /**
     * Search facilities by name.
     * @param name Facility name
     * @param limit Records per page
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
     * @param limit Records per page
     * @param offset Starting position
     * @return List of matching facilities
     */
    List<Facility> findByLocation(String address, String province, String district, String ward,
                                   int limit, int offset);

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
     * @param limit Records per page
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
     * Get facility by ID.
     * @param facilityId Facility ID
     * @return Facility object
     * @throws BusinessException if not found
     */
    Facility findById(int facilityId) throws BusinessException;

    /**
     * Create new facility.
     * @param facility Facility to create
     * @return Generated facility ID
     * @throws ValidationException if validation fails
     * @throws BusinessException if creation fails
     */
    int create(Facility facility) throws ValidationException, BusinessException;

    /**
     * Update existing facility.
     * @param facility Facility to update
     * @throws ValidationException if validation fails
     * @throws BusinessException if update fails or facility not found
     */
    void update(Facility facility) throws ValidationException, BusinessException;

    /**
     * Soft delete facility (set is_active = 0).
     * @param facilityId Facility ID
     * @throws BusinessException if facility has active bookings or doesn't exist
     */
    void delete(int facilityId) throws BusinessException;
}
