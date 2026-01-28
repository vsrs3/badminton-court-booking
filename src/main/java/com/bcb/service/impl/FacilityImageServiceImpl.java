package com.bcb.service.impl;

import com.bcb.exception.BusinessException;
import com.bcb.model.FacilityImage;
import com.bcb.repository.FacilityImageRepository;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityImageRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityImageService;

import java.util.List;

/**
 * Implementation of FacilityImageService.
 * Handles business logic for facility image operations.
 */
public class FacilityImageServiceImpl implements FacilityImageService {

    private final FacilityImageRepository imageRepository;
    private final FacilityRepository facilityRepository;

    public FacilityImageServiceImpl() {
        this.imageRepository = new FacilityImageRepositoryImpl();
        this.facilityRepository = new FacilityRepositoryImpl();
    }


    @Override
    public List<FacilityImage> getImagesByFacility(int facilityId) throws BusinessException {
        if (facilityRepository.findById(facilityId).isEmpty()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                    "Facility not found with ID: " + facilityId);
        }
        return imageRepository.findByFacility(facilityId);
    }

    @Override
    public FacilityImage getThumbnail(int facilityId) throws BusinessException {
        if (facilityRepository.findById(facilityId).isEmpty()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                    "Facility not found with ID: " + facilityId);
        }
        return imageRepository.findThumbnail(facilityId).orElse(null);
    }

    @Override
    public List<FacilityImage> getGallery(int facilityId) throws BusinessException {
        if (facilityRepository.findById(facilityId).isEmpty()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                    "Facility not found with ID: " + facilityId);
        }
        return imageRepository.findGallery(facilityId);
    }

    @Override
    public FacilityImage getImageById(int imageId) throws BusinessException {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("IMAGE_NOT_FOUND",
                        "Image not found with ID: " + imageId));
    }

    @Override
    public int addImage(FacilityImage newImage) throws BusinessException {
        // Check facility exists
        if (facilityRepository.findById(newImage.getFacilityId()).isEmpty()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                    "Facility not found with ID: " + newImage.getFacilityId());
        }

        // Validate image path
        if (newImage.getImagePath() == null || newImage.getImagePath().trim().isEmpty()) {
            throw new BusinessException("INVALID_IMAGE_PATH", "Image path cannot be empty");
        }

        try {

            return imageRepository.insert(newImage);
        } catch (Exception e) {
            throw new BusinessException("IMAGE_ADD_ERROR",
                    "Failed to add image: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(int imageId) throws BusinessException {
        // Check image exists
        if (imageRepository.findById(imageId).isEmpty()) {
            throw new BusinessException("IMAGE_NOT_FOUND",
                    "Image not found with ID: " + imageId);
        }

        try {
            imageRepository.delete(imageId);
        } catch (Exception e) {
            throw new BusinessException("IMAGE_DELETE_ERROR",
                    "Failed to delete image: " + e.getMessage(), e);
        }
    }



    @Override
    public void update(FacilityImage currentThumbnail) {

    }
}
