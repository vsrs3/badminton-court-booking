package com.bcb.service.impl;

import com.bcb.model.Facility;
import com.bcb.dto.FacilityDTO;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityService;
import com.bcb.utils.DBContext;

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
 * Implementation of FacilityService
 * Responsible for enriching Entity → DTO with computed fields
 */
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityServiceImpl() {
        this.facilityRepository = new FacilityRepositoryImpl();
    }

    // Constructor for dependency injection (testing)
    public FacilityServiceImpl(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

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
            dto.setImageUrl("/badminton_court_booking/" + imagePath);
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