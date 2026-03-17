package com.bcb.service;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import jakarta.servlet.http.Part;

import java.util.Collection;
import com.bcb.dto.FacilityDTO;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Facility business operations.
 * Defines high-level facility operations for admin.
 *
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

    void updateFacilityWithImages(
            Facility facility,
            Part thumbnailPart,
            Collection<Part> newGalleryParts,
            String deletedImageIds
    ) throws BusinessException;

    Map<Integer, String> buildDisplayAddressMap(List<Facility> facilities);

    int createFacilityWithImages(Facility facility, Part thumbnailPart, Collection<Part> galleryParts) throws BusinessException;

    /**
     * Get facilities with pagination for home/infinite scroll.
     */
    List<FacilityDTO> getFacilities(int page, int pageSize, Double userLat, Double userLng,
                                    Double maxDistance,
                                    Integer accountId, String keyword, String province, String district,
                                    boolean favoritesOnly);

    int getTotalCount();

    int getTotalCount(String keyword, String province, String district, Integer accountId, boolean favoritesOnly,
                      Double userLat, Double userLng, Double maxDistance);

    FacilityDTO getFacilityById(Integer facilityId, Integer accountId);

    boolean addFavorite(Integer accountId, Integer facilityId);

    boolean removeFavorite(Integer accountId, Integer facilityId);
}
