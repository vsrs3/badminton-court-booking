package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffCheckinBookingDto;
import com.bcb.dto.staff.StaffCheckinSessionSlotRowDto;
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
    public StaffCheckinBookingDto findBooking(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT booking_status, booking_date, facility_id FROM Booking WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                StaffCheckinBookingDto booking = new StaffCheckinBookingDto();
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
    public List<StaffCheckinSessionSlotRowDto> findSessionSlotRows(Connection conn, int bookingId) throws Exception {
        String sql = """
                SELECT bs.booking_slot_id, bs.court_id, ts.start_time, ts.end_time
                FROM BookingSlot bs
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY bs.court_id, ts.start_time
                """;
        List<StaffCheckinSessionSlotRowDto> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffCheckinSessionSlotRowDto row = new StaffCheckinSessionSlotRowDto();
                    row.setBookingSlotId(rs.getInt("booking_slot_id"));
                    row.setCourtId(rs.getInt("court_id"));
                    row.setStartTime(rs.getTime("start_time").toLocalTime());
                    row.setEndTime(rs.getTime("end_time").toLocalTime());
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
}
