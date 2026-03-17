package com.bcb.repository.booking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.repository.booking.CourtSlotBookingRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link CourtSlotBookingRepository}.
 *
 * @author AnhTN
 */
public class CourtSlotBookingRepositoryImpl implements CourtSlotBookingRepository {

    /**
     * {@inheritDoc}
     * Queries CourtSlotBooking joined with Court to filter by facility.
     */
    @Override
    public Map<Integer, List<Integer>> findBookedSlots(int facilityId, LocalDate bookingDate) {
        String sql = "SELECT csb.court_id, csb.slot_id "
                   + "FROM CourtSlotBooking csb "
                   + "INNER JOIN Court c ON csb.court_id = c.court_id "
                   + "WHERE c.facility_id = ? AND csb.booking_date = ?";
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int courtId = rs.getInt("court_id");
                    int slotId = rs.getInt("slot_id");
                    map.computeIfAbsent(courtId, k -> new ArrayList<>()).add(slotId);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find booked slots", e);
        }
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public void insertLock(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId) {
        String sql = "INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courtId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slotId);
            ps.setInt(4, bookingSlotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // PK violation means slot conflict
            throw new DataAccessException("Failed to insert court slot lock", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteByBookingSlotIds(Connection conn, List<Integer> bookingSlotIds) {

        if (bookingSlotIds == null || bookingSlotIds.isEmpty()) {
            return;
        }

        final int BATCH_SIZE = 1000;

        try {

            for (int start = 0; start < bookingSlotIds.size(); start += BATCH_SIZE) {

                int end = Math.min(start + BATCH_SIZE, bookingSlotIds.size());
                List<Integer> batch = bookingSlotIds.subList(start, end);

                StringBuilder sb = new StringBuilder(
                        "DELETE FROM CourtSlotBooking WHERE booking_slot_id IN ("
                );

                for (int i = 0; i < batch.size(); i++) {
                    sb.append(i == 0 ? "?" : ",?");
                }
                sb.append(")");

                try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {

                    for (int i = 0; i < batch.size(); i++) {
                        ps.setInt(i + 1, batch.get(i));
                    }

                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete court slot locks", e);
        }
    }
}

