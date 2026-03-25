package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingDetailHeaderDTO;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalItemDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalRowDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;
import com.bcb.repository.staff.StaffBookingDetailRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
                SELECT bs.booking_slot_id, bs.court_id, c.court_name,
                       COALESCE(bs.booking_date, b.booking_date) AS booking_date,
                       bs.slot_id, ts.start_time, ts.end_time,
                       bs.price, bs.slot_status, bs.is_released, bs.checkin_time, bs.checkout_time
                FROM BookingSlot bs
                JOIN Booking b   ON bs.booking_id = b.booking_id
                JOIN Court c     ON bs.court_id = c.court_id
                JOIN TimeSlot ts ON bs.slot_id = ts.slot_id
                WHERE bs.booking_id = ?
                ORDER BY COALESCE(bs.booking_date, b.booking_date), c.court_name, ts.start_time
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

    @Override
    public List<StaffBookingDetailRentalRowDTO> findBookingRentalRows(Connection conn, int bookingId) throws Exception {
        String sql = """
            SELECT
                COALESCE(bs.booking_date, b.booking_date) AS booking_date,
                bs.court_id,
                c.court_name,
                bs.slot_id,
                ts.start_time,
                ts.end_time,
                rr.inventory_id,
                i.name AS inventory_name,
                rr.unit_price,
                rr.quantity,
                (rr.quantity * rr.unit_price) AS line_total,
                irs.id AS schedule_id,
                irs.status AS schedule_status
            FROM RacketRental rr
            INNER JOIN BookingSlot bs
                ON rr.booking_slot_id = bs.booking_slot_id
            INNER JOIN Booking b
                ON bs.booking_id = b.booking_id
            INNER JOIN Court c
                ON bs.court_id = c.court_id
            INNER JOIN TimeSlot ts
                ON bs.slot_id = ts.slot_id
            INNER JOIN Inventory i
                ON rr.inventory_id = i.inventory_id
            LEFT JOIN InventoryRentalSchedule irs
                ON irs.facility_id = b.facility_id
               AND irs.booking_date = COALESCE(bs.booking_date, b.booking_date)
               AND irs.court_id = bs.court_id
               AND irs.slot_id = bs.slot_id
               AND irs.inventory_id = rr.inventory_id
            WHERE bs.booking_id = ?
            ORDER BY
                COALESCE(bs.booking_date, b.booking_date) ASC,
                c.court_name ASC,
                ts.start_time ASC,
                i.name ASC
            """;

        Map<String, StaffBookingDetailRentalRowDTO> rowMap = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bookingDate = rs.getString("booking_date");
                    int courtId = rs.getInt("court_id");
                    int slotId = rs.getInt("slot_id");
                    String rowKey = bookingDate + "_" + courtId + "_" + slotId;

                    StaffBookingDetailRentalRowDTO dto = rowMap.get(rowKey);
                    if (dto == null) {
                        dto = new StaffBookingDetailRentalRowDTO();
                        dto.setBookingDate(bookingDate);
                        dto.setCourtId(courtId);
                        dto.setCourtName(rs.getString("court_name"));
                        dto.setSlotId(slotId);
                        dto.setStartTime(fmtTime(rs.getTime("start_time")));
                        dto.setEndTime(fmtTime(rs.getTime("end_time")));
                        dto.setRentalTotal(BigDecimal.ZERO);
                        rowMap.put(rowKey, dto);
                    }

                    StaffBookingDetailRentalItemDTO item = new StaffBookingDetailRentalItemDTO();
                    item.setScheduleId((Integer) rs.getObject("schedule_id"));
                    item.setInventoryId(rs.getInt("inventory_id"));
                    item.setInventoryName(rs.getString("inventory_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    item.setStatus(rs.getString("schedule_status"));
                    dto.getRentalItems().add(item);

                    BigDecimal currentTotal = dto.getRentalTotal() != null ? dto.getRentalTotal() : BigDecimal.ZERO;
                    BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO;
                    dto.setRentalTotal(currentTotal.add(lineTotal));
                }
            }
        }

        List<StaffBookingDetailRentalRowDTO> list = new ArrayList<>(rowMap.values());
        for (StaffBookingDetailRentalRowDTO row : list) {
            StringJoiner joiner = new StringJoiner(", ");
            for (StaffBookingDetailRentalItemDTO item : row.getRentalItems()) {
                joiner.add(item.getInventoryName());
            }
            row.setRentalItemsText(joiner.toString());
        }
        return list;
    }


}

