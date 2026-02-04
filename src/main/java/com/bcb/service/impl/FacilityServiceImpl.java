package com.bcb.service.impl;

import com.bcb.config.ConfigUpload;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityImage;
import com.bcb.repository.FacilityImageRepository;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityImageRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityImageService;
import com.bcb.service.FacilityService;
import com.bcb.service.UploadService;
import com.bcb.validation.FacilityValidator;
import jakarta.servlet.http.Part;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of FacilityService.
 * Handles business logic for facility operations.
 *
 * Note: This is a single-owner system. All facilities belong to the admin.
 */
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityImageRepository facilityImageRepository;
    private final UploadService uploadService;

    public FacilityServiceImpl() {
        this.facilityRepository = new FacilityRepositoryImpl();
        this.facilityImageRepository = new FacilityImageRepositoryImpl();
        this.uploadService = new UploadServiceImpl();
    }


    @Override
    public List<Facility> findAll(int limit, int offset) {
        return facilityRepository.findAll(limit, offset);
    }

    @Override
    public int count() {
        return facilityRepository.count();
    }

    @Override
    public List<Facility> findByKeyword(String keyword, int limit, int offset) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(limit, offset);
        }
        return facilityRepository.findByKeyword(keyword.trim(), limit, offset);
    }

    @Override
    public int countByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return count();
        }
        return facilityRepository.countByKeyword(keyword.trim());
    }

    @Override
    public Facility findById(int facilityId) throws BusinessException {
        return facilityRepository.findById(facilityId)
            .orElseThrow(() -> new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facilityId));
    }

    @Override
    public int create(Facility facility) throws ValidationException, BusinessException {
        // Validate input
        List<String> errors = FacilityValidator.validate(facility);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        try {
            return facilityRepository.insert(facility);
        } catch (Exception e) {
            throw new BusinessException("FACILITY_CREATE_ERROR",
                "Failed to create facility: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Facility facility) throws ValidationException, BusinessException {
        // Validate input
        List<String> errors = FacilityValidator.validate(facility);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        // Check facility exists
        if (!facilityRepository.findById(facility.getFacilityId()).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facility.getFacilityId());
        }

        try {
            int rowsAffected = facilityRepository.update(facility);
            if (rowsAffected == 0) {
                throw new BusinessException("FACILITY_UPDATE_ERROR",
                    "No rows affected during update");
            }
        } catch (Exception e) {
            throw new BusinessException("FACILITY_UPDATE_ERROR",
                "Failed to update facility: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateFacilityWithImages(
            Facility facility,
            Part thumbnailPart,
            Collection<Part> newGalleryParts,
            String deletedImageIds
    ) throws BusinessException {

        List<String> newUploadedFiles = new ArrayList<>(); // để rollback
        List<String> oldFilesToDelete = new ArrayList<>(); // chỉ xóa khi OK

        try {
            /* ================= 1. UPDATE FACILITY ================= */
            facilityRepository.update(facility);

            int facilityId = facility.getFacilityId();

            /* ================= 2. THUMBNAIL ================= */
            if (thumbnailPart != null && thumbnailPart.getSize() > 0) {

                // upload file mới (CHƯA xóa file cũ)
                String newThumbPath = uploadService.saveImage(
                        thumbnailPart,
                        ConfigUpload.FACILITY_IMAGE_FOLDER
                );
                newUploadedFiles.add(newThumbPath);

                FacilityImage currentThumb =
                        facilityImageRepository.findThumbnail(facilityId);

                if (currentThumb != null) {
                    // lưu path cũ để xóa sau
                    oldFilesToDelete.add(currentThumb.getImagePath());

                    // update DB
                    currentThumb.setImagePath(newThumbPath);
                    facilityImageRepository.update(currentThumb);
                } else {
                    // insert mới
                    FacilityImage thumb = new FacilityImage();
                    thumb.setFacilityId(facilityId);
                    thumb.setImagePath(newThumbPath);
                    thumb.setIsThumbnail(true);
                    facilityImageRepository.insert(thumb);
                }
            }

            /* ================= 3. DELETE GALLERY ================= */
            if (deletedImageIds != null && !deletedImageIds.isBlank()) {
                String[] ids = deletedImageIds.split(",");

                for (String idStr : ids) {
                    int imageId = Integer.parseInt(idStr.trim());

                    FacilityImage img = facilityImageRepository.findById(imageId);

                    if (img != null) {
                        oldFilesToDelete.add(img.getImagePath());
                        facilityImageRepository.deleteByFacility(imageId);
                    }
                }
            }

            /* ================= 4. ADD NEW GALLERY ================= */
            for (Part part : newGalleryParts) {
                if (!"gallery".equals(part.getName()) || part.getSize() == 0) continue;

                String path = uploadService.saveImage(
                        part,
                        ConfigUpload.FACILITY_IMAGE_FOLDER
                );
                newUploadedFiles.add(path);

                FacilityImage gallery = new FacilityImage();
                gallery.setFacilityId(facilityId);
                gallery.setImagePath(path);
                gallery.setIsThumbnail(false);

                facilityImageRepository.insert(gallery);
            }

            /* ================= 5. COMMIT FILE DELETE ================= */
            for (String oldPath : oldFilesToDelete) {
                uploadService.deleteFile(oldPath);
            }

        } catch (Exception e) {

            /* ================= ROLLBACK FILE ================= */
            for (String path : newUploadedFiles) {
                try {
                    uploadService.deleteFile(path);
                } catch (Exception ignored) {
                }
            }

            throw new BusinessException(
                    "Update facility failed. All changes rolled back.", e
            );
        }
    }

    @Override
    public void delete(int facilityId) throws BusinessException {
        // Check facility exists
        if (!facilityRepository.findById(facilityId).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facilityId);
        }

        try {
            int rowsAffected = facilityRepository.softDelete(facilityId);
            if (rowsAffected == 0) {
                throw new BusinessException("FACILITY_DELETE_ERROR",
                    "No rows affected during delete");
            }
        } catch (Exception e) {
            throw new BusinessException("FACILITY_DELETE_ERROR",
                "Failed to delete facility: " + e.getMessage(), e);
        }
    }



    @Override
    public Map<Integer, String> buildDisplayAddressMap(List<Facility> facilities) {
        Map<Integer, String> map = new HashMap<>();

        for (Facility f : facilities) {
            String fullAddress = Stream.of(
                            f.getAddress(),
                            f.getWard(),
                            f.getDistrict(),
                            f.getProvince()
                    )
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(", "));

            map.put(f.getFacilityId(), fullAddress);
        }

        return map;
    }

    @Override
    public int createFacilityWithImages(
            Facility facility,
            Part thumbnailPart,
            Collection<Part> galleryParts
    ) throws BusinessException {

        List<String> newUploadedFiles = new ArrayList<>(); // rollback nếu lỗi

        try {
            /* ================= 1. CREATE FACILITY ================= */
            int facilityId = facilityRepository.insert(facility);
            facility.setFacilityId(facilityId);

            /* ================= 2. THUMBNAIL ================= */
            if (thumbnailPart != null && thumbnailPart.getSize() > 0) {

                String thumbPath = uploadService.saveImage(
                        thumbnailPart,
                        ConfigUpload.FACILITY_IMAGE_FOLDER
                );
                newUploadedFiles.add(thumbPath);

                FacilityImage thumb = new FacilityImage();
                thumb.setFacilityId(facilityId);
                thumb.setImagePath(thumbPath);
                thumb.setIsThumbnail(true);

                facilityImageRepository.insert(thumb);
            }

            /* ================= 3. GALLERY ================= */
            for (Part part : galleryParts) {
                if (!"gallery".equals(part.getName()) || part.getSize() == 0) {
                    continue;
                }

                String path = uploadService.saveImage(
                        part,
                        ConfigUpload.FACILITY_IMAGE_FOLDER
                );
                newUploadedFiles.add(path);

                FacilityImage gallery = new FacilityImage();
                gallery.setFacilityId(facilityId);
                gallery.setImagePath(path);
                gallery.setIsThumbnail(false);

                facilityImageRepository.insert(gallery);
            }

            return facilityId;

        } catch (Exception e) {

            /* ================= ROLLBACK FILE ================= */
            for (String path : newUploadedFiles) {
                try {
                    uploadService.deleteFile(path);
                } catch (Exception ignored) {
                }
            }

            throw new BusinessException(
                    "Create facility failed. All changes rolled back.", e
            );
        }
    }
}
