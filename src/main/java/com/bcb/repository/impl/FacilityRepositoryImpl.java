package com.bcb.repository.impl;

import com.bcb.model.Facility;
import com.bcb.repository.FacilityRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FacilityRepository
 * Returns PURE entities (no computed fields)
 */
public class FacilityRepositoryImpl implements FacilityRepository {

    @Override
    public List<Facility> findAllWithPagination(int offset, int limit) {
        List<Facility> facilities = new ArrayList<>();

        // ✅ CLEANED: No thumbnail, no rating in main query
        String sql = """
            SELECT 
            facility_id,
            name,
            province,
            district,
            ward,
            address,
            latitude,
            longitude,
            description,
            open_time,
            close_time,
            is_active
        FROM Facility
        WHERE is_active = 1
        ORDER BY facility_id ASC
        OFFSET ? ROWS
        FETCH NEXT ? ROWS ONLY
    """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, offset);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Facility facility = mapResultSetToFacility(rs);
                    facilities.add(facility);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching facilities: " + e.getMessage(), e);
        }

        return facilities;
    }

    @Override
    public Optional<Facility> findById(Integer facilityId) {
        // ✅ CLEANED: Pure entity query
        String sql = """
            SELECT 
                facility_id,
                name,
                province,
                district,
                ward,
                address,
                latitude,
                longitude,
                description,
                open_time,
                close_time,
                is_active
            FROM Facility
            WHERE facility_id = ? AND is_active = 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Facility facility = mapResultSetToFacility(rs);
                    return Optional.of(facility);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching facility by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Facility WHERE is_active = 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error counting facilities: " + e.getMessage(), e);
        }

        return 0;
    }

    @Override
    public String findThumbnailPath(Integer facilityId) {
        String sql = """
            SELECT TOP 1 image_path 
            FROM FacilityImage 
            WHERE facility_id = ? AND is_thumbnail = 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image_path");
                }
            }

        } catch (SQLException e) {
            // Log but don't throw - image is optional
            System.err.println("Error fetching thumbnail: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Double getAverageRating(Integer facilityId) {
        String sql = """
            SELECT AVG(CAST(r.rating AS FLOAT)) as avg_rating
            FROM Review r
            INNER JOIN Booking b ON r.booking_id = b.booking_id
            INNER JOIN Court c ON b.court_id = c.court_id
            WHERE c.facility_id = ?
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avgRating = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avgRating;
                }
            }

        } catch (SQLException e) {
            // Log but don't throw - rating is optional
            System.err.println("Error fetching rating: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Map ResultSet to PURE Facility entity
     */
    private Facility mapResultSetToFacility(ResultSet rs) throws SQLException {
        Facility facility = new Facility();

        facility.setFacilityId(rs.getInt("facility_id"));
        facility.setName(rs.getString("name"));
        facility.setProvince(rs.getString("province"));
        facility.setDistrict(rs.getString("district"));
        facility.setWard(rs.getString("ward"));
        facility.setAddress(rs.getString("address"));

        BigDecimal lat = rs.getBigDecimal("latitude");
        facility.setLatitude(lat);

        BigDecimal lng = rs.getBigDecimal("longitude");
        facility.setLongitude(lng);

        facility.setDescription(rs.getString("description"));

        Time openTime = rs.getTime("open_time");
        facility.setOpenTime(openTime != null ? openTime.toLocalTime() : null);

        Time closeTime = rs.getTime("close_time");
        facility.setCloseTime(closeTime != null ? closeTime.toLocalTime() : null);

        facility.setIsActive(rs.getBoolean("is_active"));

        return facility;
    }
}