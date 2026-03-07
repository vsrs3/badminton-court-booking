package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Booking;
import com.bcb.repository.booking.BookingRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BookingRepository}.
 *
 * @author AnhTN
 */
public class BookingRepositoryImpl implements BookingRepository {

    /** {@inheritDoc} */
    @Override
    public int insertBooking(Connection conn, Booking booking) {
        String sql = "INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, hold_expired_at) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getFacilityId());
            ps.setDate(2, Date.valueOf(booking.getBookingDate()));
            ps.setInt(3, booking.getAccountId());
            ps.setString(4, booking.getBookingStatus());
            ps.setTimestamp(5, Timestamp.valueOf(booking.getHoldExpiredAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("Failed to insert booking: No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert booking", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateStatus(Connection conn, int bookingId, String newStatus) {
        String sql = "UPDATE Booking SET booking_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update booking status", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Booking> findExpiredPendingBookings() {
        String sql = "SELECT booking_id FROM Booking "
                   + "WHERE booking_status = 'PENDING' AND hold_expired_at <= GETDATE()";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Booking b = new Booking();
                b.setBookingId(rs.getInt("booking_id"));
                list.add(b);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find expired pending bookings", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public String[] findBookingOwnershipInfo(int bookingId, int accountId) {
        String sql = "SELECT b.booking_status, f.name AS facility_name "
                + "FROM Booking b JOIN Facility f ON b.facility_id = f.facility_id "
                + "WHERE b.booking_id = ? AND b.account_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("booking_status"), rs.getString("facility_name")};
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find booking ownership info", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void extendHold(int bookingId, java.time.LocalDateTime newHoldExpireAt) {
        String sql = "UPDATE Booking SET hold_expired_at = ? WHERE booking_id = ? AND booking_status = 'PENDING'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newHoldExpireAt));
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to extend hold for booking #" + bookingId, e);
        }
    }
}
