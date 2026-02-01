package com.bcb.repository.impl;

import com.bcb.dto.CourtViewDTO;
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
        String sql = "SELECT court_id, facility_id, court_type_id, court_name, is_active " +
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
        String sql = "SELECT court_id, facility_id, court_type_id, court_name, is_active " +
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
        String sql = "INSERT INTO Court (facility_id, court_type_id, court_name, is_active) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, court.getFacilityId());
            pstmt.setInt(2, court.getCourtTypeId());
            pstmt.setString(3, court.getCourtName());
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
        String sql = "UPDATE Court SET court_type_id = ?, court_name = ? " +
                "WHERE court_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, court.getCourtTypeId());
            pstmt.setString(2, court.getCourtName());
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
        String sql = "SELECT COUNT(*) " +
                "FROM Booking b " +
                "INNER JOIN BookingSlot bs ON b.booking_id = bs.booking_id " +
                "INNER JOIN TimeSlot ts ON bs.slot_id = ts.slot_id " +
                "WHERE b.court_id = ? " +
                "AND b.booking_status IN ('PENDING', 'CONFIRMED') " +
                "AND CAST(b.booking_date AS DATETIME) + CAST(ts.start_time AS DATETIME) >= GETDATE()";

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

    @Override
    public List<CourtViewDTO> findByFacilityForView(int facilityId) {
        String sql = "SELECT c.court_id, c.facility_id, c.court_type_id, ct.type_code, c.court_name " +
                "FROM Court c " +
                "JOIN CourtType ct ON c.court_type_id = ct.court_type_id " +
                "WHERE c.facility_id = ? AND c.is_active = 1 " +
                "ORDER BY c.court_id";

        List<CourtViewDTO> courts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courts.add(new CourtViewDTO(
                            rs.getInt("court_id"),
                            rs.getInt("facility_id"),
                            rs.getString("court_name"),
                            rs.getInt("court_type_id"),
                            rs.getString("type_code")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find courts by facility", e);
        }

        return courts;

    }

    /**
     * Maps ResultSet row to Court object.
     */
    private Court mapResultSetToCourt(ResultSet rs) throws SQLException {
        return new Court(
                rs.getInt("court_id"),
                rs.getInt("facility_id"),
                rs.getInt("court_type_id"),
                rs.getString("court_name"),
                rs.getBoolean("is_active")
        );
    }


}
