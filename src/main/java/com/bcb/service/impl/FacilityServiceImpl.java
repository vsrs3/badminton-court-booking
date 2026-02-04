package com.bcb.service.impl;

import com.bcb.config.ConfigUpload;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityImage;
import com.bcb.repository.FacilityImageRepository;
import com.bcb.dto.FacilityDTO;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityImageRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityImageService;
import com.bcb.service.FacilityService;
import com.bcb.service.UploadService;
import com.bcb.validation.FacilityValidator;
import jakarta.servlet.http.Part;
import com.bcb.utils.DBContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of FacilityService.
 * Handles business logic for facility operations.
 *
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
//    ============= VUONGPD =============
@Override
public List<FacilityDTO> getFacilities(int page, int pageSize, Double userLat, Double userLng, Integer accountId) {
    try {
        System.out.println("=== START getFacilities ===");

        int offset = page * pageSize;

        System.out.println("📍 getFacilities - page=" + page + ", pageSize=" + pageSize + ", offset=" + offset);

        // Get facilities from repository
        System.out.println("📦 Fetching facilities from repository...");
        List<Facility> facilities = facilityRepository.findAllWithPagination(offset, pageSize);
        System.out.println("✅ Loaded " + facilities.size() + " facilities");

        // Get total count
        int totalCount = facilityRepository.getTotalCount();
        System.out.println("📊 Total active facilities in DB: " + totalCount);

        // Step 2: Get favorites
        Set<Integer> favoriteFacilityIds = new HashSet<>();
        if (accountId != null) {
            System.out.println("👤 Fetching favorites for account: " + accountId);
            favoriteFacilityIds = getFavoriteFacilityIds(accountId);
        }

        // Step 3: Convert to DTOs
        List<FacilityDTO> dtos = new ArrayList<>();
        for (int i = 0; i < facilities.size(); i++) {
            Facility facility = facilities.get(i);
            System.out.println("🔄 Converting facility " + (i+1) + ": " + facility.getName());

            try {
                FacilityDTO dto = enrichEntityToDTO(facility, userLat, userLng);
                dto.setIsFavorite(favoriteFacilityIds.contains(facility.getFacilityId()));
                dtos.add(dto);
                System.out.println("✅ Converted: " + dto.getName());
            } catch (Exception e) {
                System.err.println("❌ ERROR converting facility: " + facility.getName());
                e.printStackTrace();
                throw e; // Re-throw để thấy full stack trace
            }
        }

        // Step 4: Sort
        if (userLat != null && userLng != null) {
            System.out.println("🔄 Sorting by distance...");
            dtos.sort((a, b) -> {
                Double distA = a.getDistanceValue() != null ? a.getDistanceValue() : Double.MAX_VALUE;
                Double distB = b.getDistanceValue() != null ? b.getDistanceValue() : Double.MAX_VALUE;
                return distA.compareTo(distB);
            });
        }

        System.out.println("=== END getFacilities - Success: " + dtos.size() + " DTOs ===");
        return dtos;

    } catch (Exception e) {
        System.err.println("❌❌❌ FATAL ERROR in getFacilities ❌❌❌");
        e.printStackTrace();
        throw new RuntimeException("Error in getFacilities: " + e.getMessage(), e);
    }
}

    @Override
    public int getTotalCount() {
        return facilityRepository.getTotalCount();
    }

    @Override
    public FacilityDTO getFacilityById(Integer facilityId, Integer accountId) {
        return facilityRepository.findById(facilityId)
                .map(facility -> {
                    FacilityDTO dto = enrichEntityToDTO(facility, null, null);

                    // Check if favorite
                    if (accountId != null) {
                        dto.setIsFavorite(isFavorite(accountId, facilityId));
                    } else {
                        dto.setIsFavorite(false);
                    }

                    return dto;
                })
                .orElse(null);
    }

    /**
     * ✅ NEW: Enrich pure Entity to DTO with computed fields
     * This is where business logic happens
     */
    private FacilityDTO enrichEntityToDTO(Facility facility, Double userLat, Double userLng) {
        FacilityDTO dto = new FacilityDTO();

        // ✅ Map basic fields from entity
        dto.setId(String.valueOf(facility.getFacilityId()));
        dto.setName(facility.getName());
        dto.setLocation(facility.getFullAddress());
        dto.setProvince(facility.getProvince());
        dto.setDistrict(facility.getDistrict());

        // ✅ Calculate distance (computed field)
        if (userLat != null && userLng != null &&
                facility.getLatitude() != null && facility.getLongitude() != null) {

            double distance = calculateDistance(
                    userLat, userLng,
                    facility.getLatitude().doubleValue(),
                    facility.getLongitude().doubleValue()
            );

            dto.setDistance(formatDistance(distance));
            dto.setDistanceValue(distance);

            System.out.println("📏 " + facility.getName() + " -> " + distance + " km");
        } else {
            dto.setDistance("Đang tính...");
            dto.setDistanceValue(Double.MAX_VALUE);
        }

        // ✅ Get rating (queried separately)
        Double rating = facilityRepository.getAverageRating(facility.getFacilityId());
        dto.setRating(rating != null ? rating : 0.0);

        // ✅ Format open/close time
        dto.setOpenTime(formatTimeRange(facility.getOpenTime(), facility.getCloseTime()));

        // ✅ Get price range
        dto.setPriceRange(getPriceRange(facility.getFacilityId()));

        // ✅ Get thumbnail image (queried separately)
        String imagePath = facilityRepository.findThumbnailPath(facility.getFacilityId());
        if (imagePath != null && !imagePath.isEmpty()) {
            dto.setImageUrl("/badminton_court_booking/uploads/" + imagePath);
        } else {
            // Fallback placeholder
            dto.setImageUrl("https://placehold.co/800x450/064E3B/A3E635?text=" +
                    facility.getName().substring(0, 1));
        }

        // Contact info (TODO: add to Facility or link to Owner)
        dto.setHotline("");
        dto.setWebsite("");
        dto.setBookingLink("");

        // Coordinates for map
        if (facility.getLatitude() != null && facility.getLongitude() != null) {
            dto.setLat(facility.getLatitude().doubleValue());
            dto.setLng(facility.getLongitude().doubleValue());
        }

        return dto;
    }

    /**
     * Calculate distance using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Format distance for display
     */
    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%dm", (int)(distanceKm * 1000));
        } else {
            return String.format("%.1fkm", distanceKm);
        }
    }

    /**
     * Format time range
     */
    private String formatTimeRange(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            return "Liên hệ";
        }

        return String.format("%s - %s",
                openTime.toString().substring(0, 5),
                closeTime.toString().substring(0, 5));
    }

    /**
     * Get price range for a facility
     */
    private String getPriceRange(Integer facilityId) {
        String sql = """
            SELECT 
                MIN(price) as min_price, 
                MAX(price) as max_price
            FROM FacilityPriceRule
            WHERE facility_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long minPrice = rs.getLong("min_price");
                    long maxPrice = rs.getLong("max_price");

                    if (minPrice > 0 && maxPrice > 0) {
                        return String.format("%,dđ - %,dđ", minPrice, maxPrice);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching price range: " + e.getMessage());
        }

        return "Liên hệ";
    }

    /**
     * Get favorite facility IDs for a user
     */
    private Set<Integer> getFavoriteFacilityIds(Integer accountId) {
        Set<Integer> favoriteIds = new HashSet<>();

        String sql = """
            SELECT facility_id 
            FROM CustomerFavoriteFacility 
            WHERE account_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favoriteIds.add(rs.getInt("facility_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching favorites: " + e.getMessage());
        }

        return favoriteIds;
    }

    /**
     * Check if a facility is favorite for a user
     */
    private boolean isFavorite(Integer accountId, Integer facilityId) {
        String sql = """
            SELECT 1 FROM CustomerFavoriteFacility 
            WHERE account_id = ? AND facility_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error checking favorite: " + e.getMessage());
        }

        return false;
    }
}

