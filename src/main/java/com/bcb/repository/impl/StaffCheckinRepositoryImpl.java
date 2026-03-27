package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffCheckinBookingDTO;
import com.bcb.dto.staff.StaffCheckinSessionSlotRowDTO;
import com.bcb.repository.staff.StaffCheckinRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StaffCheckinRepositoryImpl implements StaffCheckinRepository {

    @Override
    public StaffCheckinBookingDTO findBooking(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT booking_status, booking_date, facility_id FROM Booking WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                StaffCheckinBookingDTO booking = new StaffCheckinBookingDTO();
                booking.setBookingStatus(rs.getString("booking_status"));
                Date bookingDate = rs.getDate("booking_date");
                booking.setBookingDate(bookingDate != null ? bookingDate.toLocalDate() : null);
                booking.setFacilityId(rs.getInt("facility_id"));
                return booking;
            }
        }
    }

    @Override
    public String findInvoicePaymentStatus(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT payment_status FROM Invoice WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("payment_status");
            }
        }
    }

    @Override
    public List<StaffCheckinSessionSlotRowDTO> findSessionSlotRows(Connection conn, int bookingId) throws Exception {
        String sql = """
                SELECT bs.booking_slot_id,
                       bs.court_id,
                       COALESCE(bs.booking_date, b.booking_date) AS session_date,
                       ts.start_time,
                       ts.end_time,
                       bs.slot_status
                FROM BookingSlot bs
                JOIN Booking b ON b.booking_id = bs.booking_id
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY session_date, bs.court_id, ts.start_time
                """;
        List<StaffCheckinSessionSlotRowDTO> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffCheckinSessionSlotRowDTO row = new StaffCheckinSessionSlotRowDTO();
                    row.setBookingSlotId(rs.getInt("booking_slot_id"));
                    row.setCourtId(rs.getInt("court_id"));
                    Date sessionDate = rs.getDate("session_date");
                    row.setSessionDate(sessionDate != null ? sessionDate.toLocalDate() : null);
                    row.setStartTime(rs.getTime("start_time").toLocalTime());
                    row.setEndTime(rs.getTime("end_time").toLocalTime());
                    row.setSlotStatus(rs.getString("slot_status"));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    @Override
    public List<String> findSlotStatuses(Connection conn, List<Integer> slotIds) throws Exception {
        if (slotIds == null || slotIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < slotIds.size(); i++) {
            if (i > 0) inClause.append(',');
            inClause.append('?');
        }

        String sql = "SELECT slot_status FROM BookingSlot WHERE booking_slot_id IN (" + inClause + ")";
        List<String> statuses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < slotIds.size(); i++) {
                ps.setInt(i + 1, slotIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statuses.add(rs.getString("slot_status"));
                }
            }
        }

        return statuses;
    }

    @Override
    public void updateSlotsCheckedIn(Connection conn, List<Integer> slotIds, Timestamp checkinTime) throws Exception {
        for (int slotId : slotIds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BookingSlot SET slot_status = 'CHECKED_IN', checkin_time = ? WHERE booking_slot_id = ?")) {
                ps.setTimestamp(1, checkinTime);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void updateSlotsCheckedOut(Connection conn, List<Integer> slotIds, Timestamp checkoutTime) throws Exception {
        for (int slotId : slotIds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BookingSlot SET slot_status = 'CHECK_OUT', checkout_time = ? WHERE booking_slot_id = ?")) {
                ps.setTimestamp(1, checkoutTime);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void updateSlotsNoShow(Connection conn, List<Integer> slotIds) throws Exception {
        for (int slotId : slotIds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BookingSlot SET slot_status = 'NO_SHOW' WHERE booking_slot_id = ?")) {
                ps.setInt(1, slotId);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Booking SET booking_status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }
    @Override
    public List<Integer> findConfirmedBookingIdsWithPendingSlots(Connection conn, java.time.LocalDate bookingDate) throws Exception {
        String sql = """
                SELECT DISTINCT b.booking_id
                FROM Booking b
                JOIN BookingSlot bs ON b.booking_id = bs.booking_id
                WHERE b.booking_status = 'CONFIRMED'
                  AND COALESCE(bs.booking_date, b.booking_date) = ?
                  AND bs.slot_status = 'PENDING'
                """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("booking_id"));
                }
            }
        }
        return ids;
    }

    @Override
    public List<Integer> findBookingIdsWithCheckedInSlots(Connection conn, java.time.LocalDate upToDate) throws Exception {
        String sql = """
                SELECT DISTINCT b.booking_id
                FROM Booking b
                JOIN BookingSlot bs ON b.booking_id = bs.booking_id
                WHERE COALESCE(bs.booking_date, b.booking_date) <= ?
                  AND bs.slot_status = 'CHECKED_IN'
                """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(upToDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("booking_id"));
                }
            }
        }
        return ids;
    }
}

