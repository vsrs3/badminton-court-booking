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
 * <p>
 * Note: This is a single-owner system. All facilities belong to the admin.
 * No accountId/ownerId filtering is required.
 * Implementation of FacilityRepository
 * Returns PURE entities (no computed fields)
 */
public class FacilityRepositoryImpl implements FacilityRepository {
    private static final String HOME_SEARCH_COLLATION = "Latin1_General_100_CI_AI";

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
            pstmt.setBoolean(11, facility.getIsActive());

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
            pstmt.setBoolean(11, facility.getIsActive());
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

        f.setLatitude(lat);
        f.setLongitude(lng);
        f.setDescription(rs.getString("description"));
        Time openTime = rs.getTime("open_time");
        f.setOpenTime(openTime != null ? openTime.toLocalTime() : null);

        Time closeTime = rs.getTime("close_time");
        f.setCloseTime(closeTime != null ? closeTime.toLocalTime() : null);
        f.setIsActive(rs.getBoolean("is_active"));
        return f;
    }

//    vuongdq
    @Override
    public List<Facility> findAllWithPagination(int offset, int limit) {
        return findForHome(offset, limit, null, null, null, null);
    }

    @Override
    public List<Facility> findForHome(int offset, int limit, String keyword, String province, String district, Integer favoriteAccountId) {
        List<Facility> facilities = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
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
            FROM Facility f
            WHERE f.is_active = 1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (")
                    .append("f.name COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.address COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.province COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.district COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ?)");

            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if (province != null && !province.trim().isEmpty()) {
            sql.append(" AND f.province COLLATE ").append(HOME_SEARCH_COLLATION).append(" = ?");
            params.add(province.trim());
        }

        if (favoriteAccountId != null) {
            sql.append(" AND EXISTS (")
                    .append("SELECT 1 FROM CustomerFavoriteFacility cff ")
                    .append("WHERE cff.facility_id = f.facility_id AND cff.account_id = ?)");
            params.add(favoriteAccountId);
        }
        if (district != null && !district.trim().isEmpty()) {
            sql.append(" AND f.district COLLATE ").append(HOME_SEARCH_COLLATION).append(" = ?");
            params.add(district.trim());
        }

        sql.append(" ORDER BY f.facility_id ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                if (param instanceof Integer) {
                    ps.setInt(idx++, (Integer) param);
                } else {
                    ps.setString(idx++, param.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facilities.add(mapResultSetToFacility(rs));
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
        return countForHome(null, null, null, null);
    }

    @Override
    public int countForHome(String keyword, String province, String district, Integer favoriteAccountId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Facility f WHERE f.is_active = 1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (")
                    .append("f.name COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.address COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.province COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ? OR ")
                    .append("f.district COLLATE ").append(HOME_SEARCH_COLLATION).append(" LIKE ?)");

            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if (province != null && !province.trim().isEmpty()) {
            sql.append(" AND f.province COLLATE ").append(HOME_SEARCH_COLLATION).append(" = ?");
            params.add(province.trim());
        }

        if (favoriteAccountId != null) {
            sql.append(" AND EXISTS (")
                    .append("SELECT 1 FROM CustomerFavoriteFacility cff ")
                    .append("WHERE cff.facility_id = f.facility_id AND cff.account_id = ?)");
            params.add(favoriteAccountId);
        }
        if (district != null && !district.trim().isEmpty()) {
            sql.append(" AND f.district COLLATE ").append(HOME_SEARCH_COLLATION).append(" = ?");
            params.add(district.trim());
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                if (param instanceof Integer) {
                    ps.setInt(idx++, (Integer) param);
                } else {
                    ps.setString(idx++, param.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
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



    @Override
    public boolean addFavorite(int accountId, int facilityId) {
        String sql = "INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, facilityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Duplicate favorite (UNIQUE constraint) => treat as already favorited
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return true;
            }
            throw new RuntimeException("Error adding favorite: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeFavorite(int accountId, int facilityId) {
        String sql = "DELETE FROM CustomerFavoriteFacility WHERE account_id = ? AND facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, facilityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing favorite: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isFavorite(int accountId, int facilityId) {
        String sql = "SELECT 1 FROM CustomerFavoriteFacility WHERE account_id = ? AND facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, facilityId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking favorite: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Integer> getFavoriteFacilityIds(int accountId) {
        String sql = "SELECT facility_id FROM CustomerFavoriteFacility WHERE account_id = ?";
        List<Integer> ids = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("facility_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching favorites: " + e.getMessage(), e);
        }

        return ids;
    }
}






