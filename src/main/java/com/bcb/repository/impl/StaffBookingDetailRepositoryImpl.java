package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingDetailHeaderDTO;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;
import com.bcb.repository.staff.StaffBookingDetailRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailRepositoryImpl implements StaffBookingDetailRepository {

    @Override
    public StaffBookingDetailHeaderDTO findBookingHeader(Connection conn, int bookingId) throws Exception {
        String sql = """
                SELECT b.booking_id, b.booking_date, b.booking_status, b.created_at, b.facility_id,
                       b.recurring_id, rb.start_date AS recurring_start_date, rb.end_date AS recurring_end_date,
                       COALESCE(a.full_name, g.guest_name) AS customer_name,
                       COALESCE(a.phone, g.phone) AS customer_phone,
                       CASE WHEN b.account_id IS NOT NULL THEN 'ACCOUNT' ELSE 'GUEST' END AS customer_type
                FROM Booking b
                LEFT JOIN Account a ON b.account_id = a.account_id
                LEFT JOIN Guest g   ON b.guest_id = g.guest_id
                LEFT JOIN RecurringBooking rb ON b.recurring_id = rb.recurring_id
                WHERE b.booking_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                StaffBookingDetailHeaderDTO header = new StaffBookingDetailHeaderDTO();
                header.setBookingId(rs.getInt("booking_id"));
                header.setBookingDate(rs.getString("booking_date"));
                header.setRecurring(rs.getObject("recurring_id") != null);
                header.setRecurringStartDate(rs.getString("recurring_start_date"));
                header.setRecurringEndDate(rs.getString("recurring_end_date"));
                header.setBookingStatus(rs.getString("booking_status"));
                header.setCreatedAt(tsToStr(rs.getTimestamp("created_at")));
                header.setFacilityId(rs.getInt("facility_id"));
                header.setCustomerName(rs.getString("customer_name"));
                header.setCustomerPhone(rs.getString("customer_phone"));
                header.setCustomerType(rs.getString("customer_type"));
                return header;
            }
        }
    }

    @Override
    public List<StaffBookingDetailSlotDTO> findBookingSlots(Connection conn, int bookingId) throws Exception {
        String sql = """
                SELECT bs.booking_slot_id, bs.court_id, c.court_name, bs.booking_date,
                       bs.slot_id, ts.start_time, ts.end_time,
                       bs.price, bs.slot_status, bs.is_released, bs.checkin_time, bs.checkout_time
                FROM BookingSlot bs
                JOIN Court c     ON bs.court_id = c.court_id
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY bs.booking_date, c.court_name, ts.start_time
                """;

        List<StaffBookingDetailSlotDTO> slots = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffBookingDetailSlotDTO slot = new StaffBookingDetailSlotDTO();
                    slot.setBookingSlotId(rs.getInt("booking_slot_id"));
                    slot.setCourtId(rs.getInt("court_id"));
                    slot.setCourtName(rs.getString("court_name"));
                    slot.setBookingDate(rs.getString("booking_date"));
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setStartTime(fmtTime(rs.getTime("start_time")));
                    slot.setEndTime(fmtTime(rs.getTime("end_time")));
                    slot.setPrice(rs.getBigDecimal("price"));
                    slot.setSlotStatus(rs.getString("slot_status"));
                    slot.setReleased(rs.getBoolean("is_released"));
                    slot.setCheckinTime(tsToStr(rs.getTimestamp("checkin_time")));
                    slot.setCheckoutTime(tsToStr(rs.getTimestamp("checkout_time")));
                    slots.add(slot);
                }
            }
        }
        return slots;
    }

    @Override
    public StaffBookingDetailInvoiceDTO findInvoice(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT total_amount, paid_amount, payment_status, refund_due, refund_status FROM Invoice WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StaffBookingDetailInvoiceDTO invoice = new StaffBookingDetailInvoiceDTO();
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
                invoice.setPaymentStatus(rs.getString("payment_status"));
                invoice.setRefundDue(rs.getBigDecimal("refund_due"));
                invoice.setRefundStatus(rs.getString("refund_status"));
                return invoice;
            }
        }
    }

    private String fmtTime(Time t) {
        return t.toLocalTime().toString().substring(0, 5);
    }

    private String tsToStr(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().toString().replace("T", " ").substring(0, 16);
    }
}

