package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
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
 * JDBC implementation of FacilityRepository.
 * Handles all database operations for Facility entity.
 *
 * Note: This is a single-owner system. All facilities belong to the admin.
 * No accountId/ownerId filtering is required.
 */
public class FacilityRepositoryImpl implements FacilityRepository {

    @Override
    public List<Facility> findAll(int limit, int offset) {
        String sql = "SELECT f.* FROM Facility f " +
                     "WHERE f.is_active = 1 " +
                     "ORDER BY f.facility_id DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Facility> facilities = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    facilities.add(mapResultSetToFacility(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all facilities", e);
        }

        return facilities;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM Facility WHERE is_active = 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count facilities", e);
        }

        return 0;
    }

    @Override
    public List<Facility> findByName(String name, int limit, int offset) {
        String sql = "SELECT f.* FROM Facility f " +
                     "WHERE f.name LIKE ? AND f.is_active = 1 " +
                     "ORDER BY f.facility_id DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Facility> facilities = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    facilities.add(mapResultSetToFacility(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find facilities by name", e);
        }

        return facilities;
    }

    @Override
    public int countByName(String name) {
        String sql = "SELECT COUNT(*) FROM Facility WHERE name LIKE ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count facilities by name", e);
        }

        return 0;
    }

    @Override
    public List<Facility> findByLocation(String address, String province, String district,
                                          String ward, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
            "SELECT f.* FROM Facility f WHERE 1=1 "
        );

        List<Facility> facilities = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (address != null && !address.trim().isEmpty()) {
            sql.append("AND f.address LIKE ? ");
            params.add("%" + address + "%");
        }
        if (province != null && !province.trim().isEmpty()) {
            sql.append("AND f.province LIKE ? ");
            params.add("%" + province + "%");
        }
        if (district != null && !district.trim().isEmpty()) {
            sql.append("AND f.district LIKE ? ");
            params.add("%" + district + "%");
        }
        if (ward != null && !ward.trim().isEmpty()) {
            sql.append("AND f.ward LIKE ? ");
            params.add("%" + ward + "%");
        }

        sql.append("ORDER BY f.facility_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    facilities.add(mapResultSetToFacility(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find facilities by location", e);
        }

        return facilities;
    }

    @Override
    public int countByLocation(String address, String province, String district, String ward) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Facility WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (address != null && !address.trim().isEmpty()) {
            sql.append("AND address LIKE ? ");
            params.add("%" + address + "%");
        }
        if (province != null && !province.trim().isEmpty()) {
            sql.append("AND province LIKE ? ");
            params.add("%" + province + "%");
        }
        if (district != null && !district.trim().isEmpty()) {
            sql.append("AND district LIKE ? ");
            params.add("%" + district + "%");
        }
        if (ward != null && !ward.trim().isEmpty()) {
            sql.append("AND ward LIKE ? ");
            params.add("%" + ward + "%");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count facilities by location", e);
        }

        return 0;
    }

    @Override
    public List<Facility> findByKeyword(String keyword, int limit, int offset) {
        String sql = "SELECT f.* FROM Facility f " +
                     "WHERE f.is_active = 1 AND (" +
                     "f.name LIKE ? OR f.address LIKE ? OR f.province LIKE ? OR " +
                     "f.district LIKE ? OR f.ward LIKE ?)" +
                     "ORDER BY f.facility_id DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Facility> facilities = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            pstmt.setString(1, likeParam);
            pstmt.setString(2, likeParam);
            pstmt.setString(3, likeParam);
            pstmt.setString(4, likeParam);
            pstmt.setString(5, likeParam);
            pstmt.setInt(6, offset);
            pstmt.setInt(7, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    facilities.add(mapResultSetToFacility(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find facilities by keyword", e);
        }

        return facilities;
    }

    @Override
    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM Facility " +
                     "WHERE is_active = 1 AND (" +
                     "name LIKE ? OR address LIKE ? OR province LIKE ? OR " +
                     "district LIKE ? OR ward LIKE ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeParam = "%" + keyword + "%";
            pstmt.setString(1, likeParam);
            pstmt.setString(2, likeParam);
            pstmt.setString(3, likeParam);
            pstmt.setString(4, likeParam);
            pstmt.setString(5, likeParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count facilities by keyword", e);
        }

        return 0;
    }

    @Override
    public Optional<Facility> findById(int facilityId) {
        String sql = "SELECT f.* FROM Facility f " +
                     "WHERE f.facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFacility(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find facility by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public int insert(Facility facility) {
        String sql = "INSERT INTO Facility (name, province, district, ward, address, latitude, longitude, description, " +
                     "open_time, close_time, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, facility.getName());
            pstmt.setString(2, facility.getProvince());
            pstmt.setString(3, facility.getDistrict());
            pstmt.setString(4, facility.getWard());
            pstmt.setString(5, facility.getAddress());
            pstmt.setObject(6, facility.getLatitude());
            pstmt.setObject(7, facility.getLongitude());
            pstmt.setString(8, facility.getDescription());
            pstmt.setTime(9, Time.valueOf(facility.getOpenTime()));
            pstmt.setTime(10, Time.valueOf(facility.getCloseTime()));
            pstmt.setBoolean(11, facility.isActive());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert facility", e);
        }

        throw new DataAccessException("Failed to insert facility: No ID generated");
    }

    @Override
    public int update(Facility facility) {
        String sql = "UPDATE Facility " +
                     "SET name = ?, province = ?, district = ?, ward = ?, " +
                     "address = ?, latitude = ?, longitude = ?, description = ?, " +
                     "open_time = ?, close_time = ?, is_active = ? " +
                     "WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, facility.getName());
            pstmt.setString(2, facility.getProvince());
            pstmt.setString(3, facility.getDistrict());
            pstmt.setString(4, facility.getWard());
            pstmt.setString(5, facility.getAddress());
            pstmt.setObject(6, facility.getLatitude());
            pstmt.setObject(7, facility.getLongitude());
            pstmt.setString(8, facility.getDescription());
            pstmt.setTime(9, Time.valueOf(facility.getOpenTime()));
            pstmt.setTime(10, Time.valueOf(facility.getCloseTime()));
            pstmt.setBoolean(11, facility.isActive());
            pstmt.setInt(12, facility.getFacilityId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update facility", e);
        }
    }

    @Override
    public int softDelete(int facilityId) {
        String sql = "UPDATE Facility SET is_active = 0 WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to soft delete facility", e);
        }
    }

    @Override
    public boolean hasActiveBookings(int facilityId) {
        String sql = "SELECT COUNT(*) " +
                     "FROM Booking b " +
                     "INNER JOIN Court c ON b.court_id = c.court_id " +
                     "WHERE c.facility_id = ? " +
                     "AND b.booking_status IN ('PENDING', 'CONFIRMED') " +
                     "AND EXISTS (" +
                     "    SELECT 1 FROM BookingSlot bs " +
                     "    INNER JOIN TimeSlot ts ON bs.slot_id = ts.slot_id " +
                     "    WHERE bs.booking_id = b.booking_id " +
                     "    AND CAST(b.booking_date AS DATETIME) + CAST(ts.start_time AS DATETIME) >= GETDATE()" +
                     ")";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check active bookings", e);
        }

        return false;
    }

    /**
     * Maps ResultSet row to Facility object.
     */
    private Facility mapResultSetToFacility(ResultSet rs) throws SQLException {
        Facility f = new Facility();
        f.setFacilityId(rs.getInt("facility_id"));
        f.setName(rs.getString("name"));
        f.setProvince(rs.getString("province"));
        f.setDistrict(rs.getString("district"));
        f.setWard(rs.getString("ward"));
        f.setAddress(rs.getString("address"));
        BigDecimal lat = rs.getBigDecimal("latitude");
        BigDecimal lng = rs.getBigDecimal("longitude");

        f.setLatitude(lat != null ? lat.doubleValue() : null);
        f.setLongitude(lng != null ? lng.doubleValue() : null);
        f.setDescription(rs.getString("description"));
        f.setOpenTime(rs.getTime("open_time").toLocalTime());
        f.setCloseTime(rs.getTime("close_time").toLocalTime());
        f.setActive(rs.getBoolean("is_active"));
        return f;
    }
}
