package com.bcb.repository.email.impl;

import com.bcb.dto.email.BookingEmailHeaderDTO;
import com.bcb.dto.email.BookingEmailSlotDTO;
import com.bcb.dto.email.BookingRecipientDTO;
import com.bcb.repository.email.BookingEmailRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookingEmailRepositoryImpl implements BookingEmailRepository {

    @Override
    public BookingRecipientDTO findRecipient(int bookingId) throws Exception {
        String sql = "SELECT b.booking_id, b.staff_id, " +
                "COALESCE(a.email, g.email) AS email, " +
                "COALESCE(a.full_name, g.guest_name) AS customer_name, " +
                "CASE WHEN b.account_id IS NOT NULL THEN 'ACCOUNT' ELSE 'GUEST' END AS customer_type " +
                "FROM Booking b " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                BookingRecipientDTO dto = new BookingRecipientDTO();
                dto.setBookingId(rs.getInt("booking_id"));
                int staffId = rs.getInt("staff_id");
                dto.setStaffId(rs.wasNull() ? null : staffId);
                dto.setEmail(rs.getString("email"));
                dto.setCustomerName(rs.getString("customer_name"));
                dto.setCustomerType(rs.getString("customer_type"));
                return dto;
            }
        }
    }

    @Override
    public BookingEmailHeaderDTO findHeader(int bookingId) throws Exception {
        String sql = "SELECT b.booking_id, b.booking_date, b.booking_status, f.name AS facility_name, " +
                "COALESCE(a.full_name, g.guest_name) AS customer_name, " +
                "COALESCE(a.phone, g.phone) AS customer_phone, " +
                "CASE WHEN b.account_id IS NOT NULL THEN 'ACCOUNT' ELSE 'GUEST' END AS customer_type, " +
                "i.total_amount, i.paid_amount, i.payment_status " +
                "FROM Booking b " +
                "JOIN Facility f ON b.facility_id = f.facility_id " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "LEFT JOIN Invoice i ON b.booking_id = i.booking_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                BookingEmailHeaderDTO header = new BookingEmailHeaderDTO();
                header.setBookingId(rs.getInt("booking_id"));
                header.setBookingDate(rs.getString("booking_date"));
                header.setBookingStatus(rs.getString("booking_status"));
                header.setFacilityName(rs.getString("facility_name"));
                header.setCustomerName(rs.getString("customer_name"));
                header.setCustomerPhone(rs.getString("customer_phone"));
                header.setCustomerType(rs.getString("customer_type"));
                header.setTotalAmount(rs.getBigDecimal("total_amount"));
                header.setPaidAmount(rs.getBigDecimal("paid_amount"));
                header.setPaymentStatus(rs.getString("payment_status"));
                return header;
            }
        }
    }

    @Override
    public List<BookingEmailSlotDTO> findSlots(int bookingId) throws Exception {
        String sql = "SELECT bs.court_id, c.court_name, bs.slot_id, ts.start_time, ts.end_time, " +
                "bs.price, bs.slot_status " +
                "FROM BookingSlot bs " +
                "JOIN Court c ON bs.court_id = c.court_id " +
                "JOIN TimeSlot ts ON bs.slot_id = ts.slot_id " +
                "WHERE bs.booking_id = ? " +
                "ORDER BY c.court_name, ts.start_time";

        List<BookingEmailSlotDTO> slots = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingEmailSlotDTO slot = new BookingEmailSlotDTO();
                    slot.setCourtId(rs.getInt("court_id"));
                    slot.setCourtName(rs.getString("court_name"));
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setStartTime(fmtTime(rs.getTime("start_time")));
                    slot.setEndTime(fmtTime(rs.getTime("end_time")));
                    slot.setPrice(rs.getBigDecimal("price"));
                    slot.setSlotStatus(rs.getString("slot_status"));
                    slots.add(slot);
                }
            }
        }
        return slots;
    }

    @Override
    public Map<Integer, String> findCourtNames(Set<Integer> courtIds) throws Exception {
        Map<Integer, String> map = new HashMap<>();
        if (courtIds == null || courtIds.isEmpty()) return map;

        String inClause = buildInClause(courtIds.size());
        String sql = "SELECT court_id, court_name FROM Court WHERE court_id IN " + inClause;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Integer id : courtIds) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("court_id"), rs.getString("court_name"));
                }
            }
        }
        return map;
    }

    @Override
    public Map<Integer, LocalTime[]> findSlotTimes(Set<Integer> slotIds) throws Exception {
        Map<Integer, LocalTime[]> map = new HashMap<>();
        if (slotIds == null || slotIds.isEmpty()) return map;

        String inClause = buildInClause(slotIds.size());
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot WHERE slot_id IN " + inClause;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Integer id : slotIds) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    map.put(rs.getInt("slot_id"), new LocalTime[]{
                            start != null ? start.toLocalTime() : null,
                            end != null ? end.toLocalTime() : null
                    });
                }
            }
        }
        return map;
    }

    private String buildInClause(int size) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        sb.append(')');
        return sb.toString();
    }

    private String fmtTime(Time t) {
        if (t == null) return null;
        String v = t.toLocalTime().toString();
        return v.length() >= 5 ? v.substring(0, 5) : v;
    }
}
