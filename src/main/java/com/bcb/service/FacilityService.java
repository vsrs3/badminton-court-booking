package com.bcb.service;

import com.bcb.model.FacilityDTO;
import java.util.List;

public interface FacilityService {

    /**
     * Get facilities with pagination for infinite scroll
     *
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @param userLat User's latitude (optional, for distance calculation)
     * @param userLng User's longitude (optional, for distance calculation)
     * @param accountId User account ID (optional, for favorites)
     * @return List of FacilityDTO
     */
    List<FacilityDTO> getFacilities(int page, int pageSize, Double userLat, Double userLng, Integer accountId);

    /**
     * Get total number of active facilities
     *
     * @return Total count
     */
    int getTotalCount();

    /**
     * Get facility by ID
     *
     * @param facilityId Facility ID
     * @param accountId User account ID (optional, for favorite status)
     * @return FacilityDTO or null if not found
     */
    FacilityDTO getFacilityById(Integer facilityId, Integer accountId);
}