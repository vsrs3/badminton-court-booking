package com.bcb.service.impl;

import com.bcb.model.Facility;
import com.bcb.dto.FacilityDTO;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityService;
import com.bcb.utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityServiceImpl() {
        this.facilityRepository = new FacilityRepositoryImpl();
    }

    public FacilityServiceImpl(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public List<FacilityDTO> getFacilities(int page, int pageSize, Double userLat, Double userLng, Integer accountId) {
        int offset = page * pageSize;

        List<Facility> facilities = facilityRepository.findAllWithPagination(offset, pageSize);

        // Get user's favorite facility IDs if logged in
        Set<Integer> favoriteFacilityIds = new HashSet<>();
        if (accountId != null) {
            favoriteFacilityIds = getFavoriteFacilityIds(accountId);
        }

        // Convert to DTOs and calculate distance
        List<FacilityDTO> dtos = new ArrayList<>();
        for (Facility facility : facilities) {
            FacilityDTO dto = convertToDTO(facility, userLat, userLng);

            // Set favorite status
            dto.setIsFavorite(favoriteFacilityIds.contains(facility.getFacilityId()));

            // Calculate actual distance in km (for sorting)
            if (userLat != null && userLng != null &&
                    facility.getLatitude() != null && facility.getLongitude() != null) {

                double distanceKm = calculateDistance(
                        userLat, userLng,
                        facility.getLatitude().doubleValue(),
                        facility.getLongitude().doubleValue()
                );

                // Store as internal field for sorting
                dto.setDistanceValue(distanceKm);

            } else {
                dto.setDistanceValue(Double.MAX_VALUE); // No location = send to end
            }

            dtos.add(dto);
        }

        // Sort by distance (nearest first)
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
    public FacilityDTO getFacilityById(Integer facilityId, Integer accountId) {
        return facilityRepository.findById(facilityId)
                                 .map(facility -> {
                                     FacilityDTO dto = convertToDTO(facility, null, null);

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
     * Convert Facility entity to FacilityDTO
     */
    private FacilityDTO convertToDTO(Facility facility, Double userLat, Double userLng) {
        FacilityDTO dto = new FacilityDTO();

        dto.setId(String.valueOf(facility.getFacilityId()));
        dto.setName(facility.getName());
        dto.setLocation(facility.getFullAddress());
        dto.setProvince(facility.getProvince());
        dto.setDistrict(facility.getDistrict());

        // Calculate distance if user location provided
        if (userLat != null && userLng != null &&
                facility.getLatitude() != null && facility.getLongitude() != null) {

            double distance = calculateDistance(
                    userLat, userLng,
                    facility.getLatitude().doubleValue(),
                    facility.getLongitude().doubleValue()
            );

            dto.setDistance(formatDistance(distance));
        } else {
            dto.setDistance("Đang tính...");
        }

        // Rating
        dto.setRating(facility.getRating() != null ? facility.getRating() : 0.0);

        // Open/Close time
        dto.setOpenTime(formatTimeRange(facility.getOpenTime(), facility.getCloseTime()));

        // Price range - TODO: calculate from FacilityPriceRule
        dto.setPriceRange(getPriceRange(facility.getFacilityId()));

        // Image URL
        String imagePath = facility.getThumbnailPath();
        if (imagePath != null && !imagePath.isEmpty()) {
            // If using relative path from database
            dto.setImageUrl("/badminton_court_booking/" + imagePath);
        } else {
            // Fallback placeholder
            dto.setImageUrl("https://placehold.co/800x450/064E3B/A3E635?text=" +
                    facility.getName().substring(0, 1));
        }

        // Contact info - TODO: add phone field to Facility table or hardcode for now

        // Coordinates for map
        if (facility.getLatitude() != null && facility.getLongitude() != null) {
            dto.setLat(facility.getLatitude().doubleValue());
            dto.setLng(facility.getLongitude().doubleValue());
        }
        return dto;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Referenced from https://www.movable-type.co.uk/scripts/latlong.html
     * Returns distance in kilometers
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

    /** TODO
     * Get price range for a facility
     * TODO: Implement actual price calculation from FacilityPriceRule
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