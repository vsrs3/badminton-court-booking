package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.BookingSlot;
import com.bcb.repository.booking.BookingSlotRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BookingSlotRepository}.
 *
 * @author AnhTN
 */
public class BookingSlotRepositoryImpl implements BookingSlotRepository {

    /** {@inheritDoc} */
    @Override
    public int insertBookingSlot(Connection conn, BookingSlot bookingSlot) {
        String sql = "INSERT INTO BookingSlot (booking_id, court_id, slot_id, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingSlot.getBookingId());
            ps.setInt(2, bookingSlot.getCourtID());
            ps.setInt(3, bookingSlot.getSlotId());
            ps.setBigDecimal(4, bookingSlot.getPrice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("Failed to insert booking slot: No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert booking slot", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> findBookingSlotIdsByBookingId(Connection conn, int bookingId) {
        String sql = "SELECT booking_slot_id FROM BookingSlot WHERE booking_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("booking_slot_id"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find booking slot IDs by booking", e);
        }
        return ids;
    }
}
