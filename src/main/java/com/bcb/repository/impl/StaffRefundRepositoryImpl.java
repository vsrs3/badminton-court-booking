package com.bcb.repository.impl;
import com.bcb.dto.staff.StaffRefundListItemDTO;
import com.bcb.repository.staff.StaffRefundRepository;
import java.sql.Connection;import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffRefundRepositoryImpl implements StaffRefundRepository {

    @Override
    public int countPendingRefunds(Connection conn, int facilityId, String search) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
           .append("FROM Invoice i JOIN Booking b ON i.booking_id = b.booking_id ")
           .append("LEFT JOIN Account a ON b.account_id = a.account_id ")
           .append("LEFT JOIN Guest g ON b.guest_id = g.guest_id ")
           .append("WHERE b.facility_id = ? AND i.refund_status = 'PENDING_MANUAL' AND i.refund_due > 0 ");

        List<Object> params = new ArrayList<>();
        params.add(facilityId);

        String searchTerm = normalizeSearch(search);
        if (searchTerm != null) {
            if (isNumeric(searchTerm)) {
                Integer bookingId = parseBookingId(searchTerm);
                if (bookingId != null) {
                    sql.append("AND (b.booking_id = ? OR COALESCE(a.phone, g.phone) LIKE ?) ");
                    params.add(bookingId);
                    params.add("%" + searchTerm + "%");
                } else {
                    sql.append("AND COALESCE(a.phone, g.phone) LIKE ? ");
                    params.add("%" + searchTerm + "%");
                }
            } else {
                sql.append("AND COALESCE(a.phone, g.phone) LIKE ? ");
                params.add("%" + searchTerm + "%");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public List<StaffRefundListItemDTO> findPendingRefunds(Connection conn, int facilityId, int offset, int size, String search)            throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, b.booking_date, b.created_at, ")
           .append("COALESCE(a.full_name, g.guest_name) AS customer_name, ")
           .append("COALESCE(a.phone, g.phone) AS phone, ")
           .append("i.total_amount, i.paid_amount, i.refund_due, i.refund_status, i.refund_note ")
           .append("FROM Invoice i JOIN Booking b ON i.booking_id = b.booking_id ")
           .append("LEFT JOIN Account a ON b.account_id = a.account_id ")
           .append("LEFT JOIN Guest g ON b.guest_id = g.guest_id ")
           .append("OUTER APPLY (")
           .append("    SELECT MAX(cl.change_time) AS refund_request_time ")
           .append("    FROM BookingChangeLog cl ")
           .append("    WHERE cl.booking_id = b.booking_id ")
           .append("      AND cl.refund_due > 0 ")
           .append("      AND cl.change_action IN ('EDIT_SAVE','RELEASE_SLOT','CANCEL_BOOKING')")
           .append(") rr ")
           .append("WHERE b.facility_id = ? AND i.refund_status = 'PENDING_MANUAL' AND i.refund_due > 0 ");

        List<Object> params = new ArrayList<>();
        params.add(facilityId);

        String searchTerm = normalizeSearch(search);
        if (searchTerm != null) {
            if (isNumeric(searchTerm)) {
                Integer bookingId = parseBookingId(searchTerm);
                if (bookingId != null) {
                    sql.append("AND (b.booking_id = ? OR COALESCE(a.phone, g.phone) LIKE ?) ");
                    params.add(bookingId);
                    params.add("%" + searchTerm + "%");
                } else {
                    sql.append("AND COALESCE(a.phone, g.phone) LIKE ? ");
                    params.add("%" + searchTerm + "%");
                }
            } else {
                sql.append("AND COALESCE(a.phone, g.phone) LIKE ? ");
                params.add("%" + searchTerm + "%");
            }
        }

        sql.append("ORDER BY COALESCE(rr.refund_request_time, b.created_at) DESC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        params.add(offset);
        params.add(size);

        List<StaffRefundListItemDTO> results = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRefundListItemDTO item = new StaffRefundListItemDTO();
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setCustomerName(rs.getString("customer_name"));
                    item.setPhone(rs.getString("phone"));
                    item.setBookingDate(rs.getString("booking_date"));
                    item.setCreatedAt(rs.getString("created_at"));
                    item.setTotalAmount(rs.getBigDecimal("total_amount"));
                    item.setPaidAmount(rs.getBigDecimal("paid_amount"));
                    item.setRefundDue(rs.getBigDecimal("refund_due"));
                    item.setRefundStatus(rs.getString("refund_status"));
                    item.setRefundNote(rs.getString("refund_note"));
                    results.add(item);
                }
            }
        }
        return results;
    }

    @Override
    public String findRefundNote(Connection conn, int bookingId, int facilityId) throws Exception {        String sql = "SELECT i.refund_note " +
            "FROM Invoice i JOIN Booking b ON i.booking_id = b.booking_id " +
            "WHERE i.booking_id = ? AND b.facility_id = ? AND i.refund_status = 'PENDING_MANUAL'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    @Override
    public int markRefunded(Connection conn, int bookingId, int facilityId, String refundNote) throws Exception {
        String sql = "UPDATE i SET i.refund_status = 'REFUNDED', i.refund_note = ? " +
                "FROM Invoice i JOIN Booking b ON i.booking_id = b.booking_id " +
                "WHERE i.booking_id = ? AND b.facility_id = ? " +
                "AND i.refund_status = 'PENDING_MANUAL' AND i.refund_due > 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refundNote);
            ps.setInt(2, bookingId);
            ps.setInt(3, facilityId);
            return ps.executeUpdate();
        }
    }

    private static String normalizeSearch(String search) {
        if (search == null) return null;
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    private static Integer parseBookingId(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed < Integer.MIN_VALUE || parsed > Integer.MAX_VALUE) return null;
            return (int) parsed;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
