package com.bcb.service.impl;

import com.bcb.config.ConfigUpload;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityImage;
import com.bcb.repository.FacilityImageRepository;
import com.bcb.dto.FacilityDTO;
import com.bcb.dto.FacilityPriceRuleDetailDTO;
import com.bcb.dto.FacilityReviewDetailDTO;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityImageRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityImageService;
import com.bcb.service.FacilityService;
import com.bcb.service.UploadService;
import com.bcb.validation.FacilityValidator;
import com.bcb.utils.DBContext;
import jakarta.servlet.http.Part;

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
    public List<FacilityDTO> getFacilities(int page, int pageSize, Double userLat, Double userLng, Integer accountId, String keyword, String province, String district, boolean favoritesOnly) {
        Integer favoriteAccountId = favoritesOnly ? accountId : null;
        if (favoritesOnly && favoriteAccountId == null) {
            return new ArrayList<>();
        }

        int offset = page * pageSize;
        List<Facility> facilities = facilityRepository.findForHome(offset, pageSize, keyword, province, district, favoriteAccountId);

        Set<Integer> favoriteFacilityIds = new HashSet<>();
        if (accountId != null) {
            favoriteFacilityIds = getFavoriteFacilityIds(accountId);
        }

        List<Integer> facilityIds = facilities.stream()
                                              .map(Facility::getFacilityId)
                                              .collect(Collectors.toList());

        Map<Integer, Double> ratingMap = facilityRepository.findAverageRatings(facilityIds);
        Map<Integer, String> priceRangeMap = facilityRepository.findPriceRanges(facilityIds);
        Map<Integer, String> thumbnailMap = facilityRepository.findThumbnailPaths(facilityIds);

        List<FacilityDTO> dtos = new ArrayList<>(facilities.size());
        for (Facility facility : facilities) {
            FacilityDTO dto = enrichEntityToDTO(facility, userLat, userLng, ratingMap, priceRangeMap, thumbnailMap);
            dto.setIsFavorite(favoriteFacilityIds.contains(facility.getFacilityId()));
            dtos.add(dto);
        }

        if (userLat != null && userLng != null) {
            dtos.sort((a, b) -> {
                Double distA = a.getDistanceValue() != null ? a.getDistanceValue() : Double.MAX_VALUE;
                Double distB = b.getDistanceValue() != null ? b.getDistanceValue() : Double.MAX_VALUE;
                return distA.compareTo(distB);
            });
        }

        return dtos;
    }
    @Override
    public int getTotalCount() {
        return facilityRepository.getTotalCount();
    }

    @Override
    public int getTotalCount(String keyword, String province, String district, Integer accountId, boolean favoritesOnly) {
        Integer favoriteAccountId = favoritesOnly ? accountId : null;
        if (favoritesOnly && favoriteAccountId == null) {
            return 0;
        }
        return facilityRepository.countForHome(keyword, province, district, favoriteAccountId);
    }

    @Override
    public FacilityDTO getFacilityById(Integer facilityId, Integer accountId) {
        return facilityRepository.findById(facilityId)
                                 .map(facility -> {
                                     FacilityDTO dto = enrichEntityToDTO(facility, null, null);

                                     dto.setDescription(normalizeText(facility.getDescription()));

                                     List<String> galleryImages = getGalleryImageUrls(facilityId);
                                     dto.setGalleryImages(galleryImages);

                                     List<FacilityPriceRuleDetailDTO> priceRules = getPriceRulesForDetail(facilityId);
                                     dto.setPriceRules(priceRules);

                                     List<FacilityReviewDetailDTO> reviews = getReviewsForDetail(facilityId);
                                     dto.setReviews(reviews);
                                     dto.setReviewCount(reviews.size());

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
     * Enrich pure Entity to DTO with computed fields
     * This is where business logic happens
     */
    private FacilityDTO enrichEntityToDTO(Facility facility, Double userLat, Double userLng) {
        return enrichEntityToDTO(facility, userLat, userLng, null, null, null);
    }

    private FacilityDTO enrichEntityToDTO(Facility facility,
                                          Double userLat,
                                          Double userLng,
                                          Map<Integer, Double> ratingMap,
                                          Map<Integer, String> priceRangeMap,
                                          Map<Integer, String> thumbnailMap) {
        FacilityDTO dto = new FacilityDTO();

        dto.setId(String.valueOf(facility.getFacilityId()));
        dto.setName(facility.getName());
        dto.setLocation(facility.getFullAddress());
        dto.setProvince(facility.getProvince());
        dto.setDistrict(facility.getDistrict());

        if (userLat != null && userLng != null &&
                facility.getLatitude() != null && facility.getLongitude() != null) {

            double distance = calculateDistance(
                    userLat, userLng,
                    facility.getLatitude().doubleValue(),
                    facility.getLongitude().doubleValue()
            );

            dto.setDistance(formatDistance(distance));
            dto.setDistanceValue(distance);
        } else {
            dto.setDistance("Đang tính...");
            dto.setDistanceValue(Double.MAX_VALUE);
        }

        Double rating = ratingMap != null
                ? ratingMap.getOrDefault(facility.getFacilityId(), 0.0)
                : facilityRepository.getAverageRating(facility.getFacilityId());
        dto.setRating(rating != null ? rating : 0.0);

        dto.setOpenTime(formatTimeRange(facility.getOpenTime(), facility.getCloseTime()));

        String priceRange = priceRangeMap != null
                ? priceRangeMap.get(facility.getFacilityId())
                : getPriceRange(facility.getFacilityId());
        dto.setPriceRange(priceRange != null && !priceRange.isEmpty() ? priceRange : "Lien he");

        String imagePath = thumbnailMap != null
                ? thumbnailMap.get(facility.getFacilityId())
                : facilityRepository.findThumbnailPath(facility.getFacilityId());
        if (imagePath != null && !imagePath.isEmpty()) {
            dto.setImageUrl("uploads/" + imagePath);
        } else {
            dto.setImageUrl("uploads/facility/default-facility.jpg");
        }

        dto.setHotline("");
        dto.setWebsite("");
        dto.setBookingLink("");

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
                formatSingleTime(openTime),
                formatSingleTime(closeTime));
    }

    /**
     * Format a single LocalTime for display.
     * Converts 23:59:59 (end of day in DB) to "24:00" for UI.
     */
    private String formatSingleTime(LocalTime time) {
        if (time == null) return "";
        if (time.getHour() == 23 && time.getMinute() == 59 && time.getSecond() == 59) {
            return "24:00";
        }
        return time.toString().substring(0, 5);
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


    private List<String> getGalleryImageUrls(Integer facilityId) {
        List<String> imageUrls = new ArrayList<>();

        List<FacilityImage> galleryImages = facilityImageRepository.findGallery(facilityId);
        if (galleryImages != null && !galleryImages.isEmpty()) {
            for (FacilityImage image : galleryImages) {
                if (image.getImagePath() != null && !image.getImagePath().isBlank()) {
                    imageUrls.add("uploads/" + image.getImagePath());
                }
            }
        }

        if (!imageUrls.isEmpty()) {
            return imageUrls;
        }

        FacilityImage thumbnail = facilityImageRepository.findThumbnail(facilityId);
        if (thumbnail != null && thumbnail.getImagePath() != null && !thumbnail.getImagePath().isBlank()) {
            imageUrls.add("uploads/" + thumbnail.getImagePath());
        }

        return imageUrls;
    }

    private List<FacilityPriceRuleDetailDTO> getPriceRulesForDetail(Integer facilityId) {
        String sql = """
            SELECT
                COALESCE(NULLIF(ct.description, ''), ct.type_code) AS court_type_name,
                pr.day_type,
                pr.start_time,
                pr.end_time,
                pr.price
            FROM FacilityPriceRule pr
            INNER JOIN CourtType ct ON ct.court_type_id = pr.court_type_id
            WHERE pr.facility_id = ?
            ORDER BY COALESCE(NULLIF(ct.description, ''), ct.type_code) ASC, pr.day_type ASC, pr.start_time ASC
        """;

        List<FacilityPriceRuleDetailDTO> priceRules = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FacilityPriceRuleDetailDTO dto = new FacilityPriceRuleDetailDTO();
                    dto.setCourtTypeName(normalizeText(rs.getString("court_type_name")));
                    dto.setDayType(normalizeText(rs.getString("day_type")));

                    java.sql.Time startTime = rs.getTime("start_time");
                    java.sql.Time endTime = rs.getTime("end_time");
                    dto.setStartTime(startTime != null ? formatSingleTime(startTime.toLocalTime()) : null);
                    dto.setEndTime(endTime != null ? formatSingleTime(endTime.toLocalTime()) : null);
                    dto.setPrice(rs.getBigDecimal("price"));

                    priceRules.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching detail price rules for facility " + facilityId + ": " + e.getMessage());
        }

        return priceRules;
    }

    private List<FacilityReviewDetailDTO> getReviewsForDetail(Integer facilityId) {
        String sql = """
            SELECT
                a.full_name,
                r.rating,
                r.comment,
                r.created_at
            FROM Review r
            INNER JOIN Booking b ON b.booking_id = r.booking_id
            INNER JOIN Account a ON a.account_id = r.account_id
            WHERE b.facility_id = ?
            ORDER BY r.created_at DESC
        """;

        List<FacilityReviewDetailDTO> reviews = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FacilityReviewDetailDTO dto = new FacilityReviewDetailDTO();
                    dto.setReviewerName(normalizeText(rs.getString("full_name")));
                    dto.setRating(rs.getInt("rating"));
                    dto.setComment(normalizeText(rs.getString("comment")));

                    java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                    dto.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime().toString() : null);

                    reviews.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching detail reviews: " + e.getMessage());
        }

        return reviews;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    @Override
    public boolean addFavorite(Integer accountId, Integer facilityId) {
        return facilityRepository.addFavorite(accountId, facilityId);
    }

    @Override
    public boolean removeFavorite(Integer accountId, Integer facilityId) {
        return facilityRepository.removeFavorite(accountId, facilityId);
    }

    /**
     */
    private Set<Integer> getFavoriteFacilityIds(Integer accountId) {
        return new HashSet<>(facilityRepository.getFavoriteFacilityIds(accountId));
    }

    /**
     * Check if a facility is favorite for a user
     */
    private boolean isFavorite(Integer accountId, Integer facilityId) {
        return facilityRepository.isFavorite(accountId, facilityId);
    }
}











