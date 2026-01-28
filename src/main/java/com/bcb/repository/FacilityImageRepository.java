package com.bcb.repository;

import com.bcb.model.FacilityImage;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for FacilityImage entity.
 * Defines CRUD and query operations for facility images.
 */
public interface FacilityImageRepository {

    /**
     * Find all images for a facility.
     * @param facilityId Facility ID
     * @return List of images
     */
    List<FacilityImage> findByFacility(int facilityId);

    /**
     * Find a thumbnail image for a facility.
     * @param facilityId Facility ID
     * @return Optional containing thumbnail image or empty
     */
    Optional<FacilityImage> findThumbnail(int facilityId);

    /**
     * Find all gallery images (non-thumbnail) for a facility.
     * @param facilityId Facility ID
     * @return List of gallery images
     */
    List<FacilityImage> findGallery(int facilityId);

    /**
     * Find image by ID.
     * @param imageId Image ID
     * @return Optional containing image or empty
     */
    Optional<FacilityImage> findById(int imageId);

    /**
     * Insert a new image.
     * @param image Image to insert
     * @return Generated image ID
     */
    int insert(FacilityImage image);

    /**
     * Update image.
     * @param image Image to update
     * @return Number of rows affected
     */
    int update(FacilityImage image);

    /**
     * Delete image.
     * @param imageId Image ID
     * @return Number of rows affected
     */
    int delete(int imageId);

    /**
     * Delete all images for a facility.
     * @param facilityId Facility ID
     * @return Number of rows affected
     */
    int deleteByFacility(int facilityId);

    /**
     * Set thumbnail flag for an image.
     * @param imageId Image ID
     * @param isThumbnail true to set as thumbnail
     * @return Number of rows affected
     */
    int setThumbnail(int imageId, boolean isThumbnail);

    /**
     * Remove thumbnail flag from all images of a facility.
     * @param facilityId Facility ID
     * @return Number of rows affected
     */
    int clearThumbnails(int facilityId);
}
