package com.bcb.repository.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Court;
import com.bcb.repository.CourtRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CourtRepository.
 * Handles all database operations for Court entity.
 */
public class CourtRepositoryImpl implements CourtRepository {

    @Override
    public List<Court> findByFacility(int facilityId) {
        String sql = "SELECT court_id, facility_id, court_name, description, is_active " +
                     "FROM Court WHERE facility_id = ? AND is_active = 1 ORDER BY court_id";

        List<Court> courts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courts.add(mapResultSetToCourt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find courts by facility", e);
        }

        return courts;
    }

    @Override
    public Optional<Court> findById(int courtId) {
        String sql = "SELECT court_id, facility_id, court_name, description, is_active " +
                     "FROM Court WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourt(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find court by ID", e);
        }

        return Optional.empty();
    }


    @Override
    public int insert(Court court) {
        String sql = "INSERT INTO Court (facility_id, court_name, description, is_active) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, court.getFacilityId());
            pstmt.setString(2, court.getCourtName());
            pstmt.setString(3, court.getDescription());
            pstmt.setBoolean(4, court.isActive());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert court", e);
        }

        throw new DataAccessException("Failed to insert court: No ID generated");
    }

    @Override
    public int update(Court court) {
        String sql = "UPDATE Court SET court_name = ?, description = ? " +
                     "WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, court.getCourtName());
            pstmt.setString(2, court.getDescription());
            pstmt.setInt(3, court.getCourtId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update court", e);
        }
    }

    @Override
    public int deactivate(int courtId) {
        String sql = "UPDATE Court SET is_active = 0 WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to deactivate court", e);
        }
    }

    @Override
    public boolean hasActiveBookings(int courtId) {
        String sql = "SELECT COUNT(*) FROM Booking " +
                     "WHERE court_id = ? AND booking_status IN ('PENDING', 'CONFIRMED') " +
                     "AND CAST(CONCAT(booking_date, ' ', start_time) AS DATETIME) >= GETDATE()";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courtId);

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


    @Override
    public int countByFacility(int facilityId) {
        String sql = "SELECT COUNT(*) FROM Court WHERE facility_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count courts by facility", e);
        }

        return 0;
    }

    /**
     * Maps ResultSet row to Court object.
     */
    private Court mapResultSetToCourt(ResultSet rs) throws SQLException {
        return new Court(
            rs.getInt("court_id"),
            rs.getInt("facility_id"),
            rs.getString("court_name"),
            rs.getString("description"),
            rs.getBoolean("is_active")
        );
    }
}
