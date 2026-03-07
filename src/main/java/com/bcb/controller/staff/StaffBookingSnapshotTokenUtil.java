package com.bcb.controller.staff;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Snapshot token (etag) utility for optimistic lock in booking edit/cancel APIs.
 */
public final class StaffBookingSnapshotTokenUtil {

    private StaffBookingSnapshotTokenUtil() {
    }

    public static Snapshot loadSnapshot(Connection conn, int bookingId, int facilityId) throws SQLException {
        Snapshot snapshot = new Snapshot();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT booking_id, booking_status, booking_date, facility_id FROM Booking WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (rs.getInt("facility_id") != facilityId) {
                    return null;
                }
                snapshot.bookingId = rs.getInt("booking_id");
                snapshot.bookingStatus = rs.getString("booking_status");
                Date bookingDate = rs.getDate("booking_date");
                snapshot.bookingDate = bookingDate != null ? bookingDate.toString() : "";
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT booking_slot_id, court_id, slot_id, slot_status, price, is_released " +
                        "FROM BookingSlot WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotSnapshot slot = new SlotSnapshot();
                    slot.bookingSlotId = rs.getInt("booking_slot_id");
                    slot.courtId = rs.getInt("court_id");
                    slot.slotId = rs.getInt("slot_id");
                    slot.slotStatus = safe(rs.getString("slot_status"));
                    slot.price = normalizedMoney(rs.getBigDecimal("price"));
                    slot.released = rs.getBoolean("is_released");
                    snapshot.slots.add(slot);
                }
            }
        }
        snapshot.slots.sort(Comparator.comparingInt(a -> a.bookingSlotId));

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT invoice_id, total_amount, paid_amount, payment_status, refund_due, refund_status " +
                        "FROM Invoice WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    snapshot.invoice = new InvoiceSnapshot();
                    snapshot.invoice.invoiceId = rs.getInt("invoice_id");
                    snapshot.invoice.totalAmount = normalizedMoney(rs.getBigDecimal("total_amount"));
                    snapshot.invoice.paidAmount = normalizedMoney(rs.getBigDecimal("paid_amount"));
                    snapshot.invoice.paymentStatus = safe(rs.getString("payment_status"));
                    snapshot.invoice.refundDue = normalizedMoney(rs.getBigDecimal("refund_due"));
                    snapshot.invoice.refundStatus = safe(rs.getString("refund_status"));
                }
            }
        }

        return snapshot;
    }

    public static String computeEtag(Snapshot s) {
        StringBuilder canonical = new StringBuilder(640);
        canonical.append("B|")
                .append(s.bookingId).append('|')
                .append(safe(s.bookingStatus)).append('|')
                .append(safe(s.bookingDate)).append(';');

        for (SlotSnapshot slot : s.slots) {
            canonical.append("S|")
                    .append(slot.bookingSlotId).append('|')
                    .append(slot.courtId).append('|')
                    .append(slot.slotId).append('|')
                    .append(safe(slot.slotStatus)).append('|')
                    .append(safe(slot.price)).append('|')
                    .append(slot.released ? '1' : '0')
                    .append(';');
        }

        if (s.invoice != null) {
            canonical.append("I|")
                    .append(s.invoice.invoiceId).append('|')
                    .append(safe(s.invoice.totalAmount)).append('|')
                    .append(safe(s.invoice.paidAmount)).append('|')
                    .append(safe(s.invoice.paymentStatus)).append('|')
                    .append(safe(s.invoice.refundDue)).append('|')
                    .append(safe(s.invoice.refundStatus)).append(';');
        } else {
            canonical.append("I|null;");
        }

        return sha256Hex(canonical.toString());
    }

    public static String extractString(String json, String key) {
        if (json == null) return null;
        String search = '"' + key + '"';
        int idx = json.indexOf(search);
        if (idx < 0) return null;

        idx = json.indexOf(':', idx + search.length());
        if (idx < 0) return null;

        idx++;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length()) return null;

        if (json.charAt(idx) == 'n') return null;

        if (json.charAt(idx) == '"') {
            int end = idx + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                    return json.substring(idx + 1, end);
                }
                end++;
            }
            return null;
        }

        int end = idx;
        while (end < json.length() && ",} ]".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(idx, end);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalizedMoney(BigDecimal amount) {
        if (amount == null) return "0";
        BigDecimal normalized = amount.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        return normalized.toPlainString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public static class Snapshot {
        public int bookingId;
        public String bookingStatus;
        public String bookingDate;
        public List<SlotSnapshot> slots = new ArrayList<>();
        public InvoiceSnapshot invoice;
    }

    public static class SlotSnapshot {
        public int bookingSlotId;
        public int courtId;
        public int slotId;
        public String slotStatus;
        public String price;
        public boolean released;
    }

    public static class InvoiceSnapshot {
        public int invoiceId;
        public String totalAmount;
        public String paidAmount;
        public String paymentStatus;
        public String refundDue;
        public String refundStatus;
    }
}
