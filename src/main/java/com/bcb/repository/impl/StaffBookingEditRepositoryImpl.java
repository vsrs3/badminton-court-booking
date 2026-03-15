package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingEditExistingSlotDTO;
import com.bcb.dto.staff.StaffBookingEditSessionCellDTO;
import com.bcb.dto.staff.StaffBookingEditSlotStateDTO;
import com.bcb.dto.staff.StaffBookingEditStatusCountDTO;
import com.bcb.repository.staff.StaffBookingEditRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingEditRepositoryImpl implements StaffBookingEditRepository {

    @Override
    public List<Integer> findPendingSlotIds(Connection conn, int bookingId) throws Exception {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT booking_slot_id FROM BookingSlot WHERE booking_id = ? AND slot_status = 'PENDING'")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("booking_slot_id"));
            }
        }
        return ids;
    }

    @Override
    public List<StaffBookingEditSessionCellDTO> findSessionCellsByBookingId(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT bs.booking_slot_id, bs.court_id, bs.slot_id, bs.slot_status, ts.start_time, ts.end_time " +
                "FROM BookingSlot bs JOIN TimeSlot ts ON bs.slot_id = ts.slot_id WHERE bs.booking_id = ?";
        List<StaffBookingEditSessionCellDTO> cells = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffBookingEditSessionCellDTO c = new StaffBookingEditSessionCellDTO();
                    c.setBookingSlotId(rs.getInt("booking_slot_id"));
                    c.setCourtId(rs.getInt("court_id"));
                    c.setSlotId(rs.getInt("slot_id"));
                    c.setSlotStatus(rs.getString("slot_status"));
                    c.setStart(rs.getTime("start_time").toLocalTime());
                    c.setEnd(rs.getTime("end_time").toLocalTime());
                    cells.add(c);
                }
            }
        }
        return cells;
    }

    @Override
    public StaffBookingEditSessionCellDTO findSessionCellBySlotId(Connection conn, int courtId, int slotId) throws Exception {
        String sql = "SELECT ts.start_time, ts.end_time FROM TimeSlot ts WHERE ts.slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                StaffBookingEditSessionCellDTO c = new StaffBookingEditSessionCellDTO();
                c.setCourtId(courtId);
                c.setSlotId(slotId);
                c.setStart(rs.getTime("start_time").toLocalTime());
                c.setEnd(rs.getTime("end_time").toLocalTime());
                return c;
            }
        }
    }

    @Override
    public int cancelPendingSlot(Connection conn, int bookingId, int bookingSlotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE BookingSlot SET slot_status = 'CANCELLED', is_released = 1 WHERE booking_id = ? AND booking_slot_id = ? AND slot_status = 'PENDING'")) {
            ps.setInt(1, bookingId);
            ps.setInt(2, bookingSlotId);
            return ps.executeUpdate();
        }
    }

    @Override
    public void deleteCourtSlotBooking(Connection conn, int bookingSlotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CourtSlotBooking WHERE booking_slot_id = ?")) {
            ps.setInt(1, bookingSlotId);
            ps.executeUpdate();
        }
    }

    @Override
    public StaffBookingEditExistingSlotDTO findExistingSlot(Connection conn, int bookingId, int courtId, int slotId) throws Exception {
        String sql = "SELECT booking_slot_id, slot_status FROM BookingSlot WHERE booking_id = ? AND court_id = ? AND slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, courtId);
            ps.setInt(3, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                StaffBookingEditExistingSlotDTO out = new StaffBookingEditExistingSlotDTO();
                out.setBookingSlotId(rs.getInt("booking_slot_id"));
                out.setSlotStatus(rs.getString("slot_status"));
                return out;
            }
        }
    }

    @Override
    public BigDecimal lookupCurrentPrice(Connection conn, int facilityId, LocalDate bookingDate, int courtId, int slotId) throws Exception {
        String dayType = (bookingDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || bookingDate.getDayOfWeek() == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        String sql = "SELECT TOP 1 fpr.price " +
                "FROM Court c " +
                "JOIN TimeSlot ts ON ts.slot_id = ? " +
                "JOIN FacilityPriceRule fpr ON fpr.facility_id = c.facility_id AND fpr.court_type_id = c.court_type_id " +
                "WHERE c.court_id = ? AND c.facility_id = ? AND c.is_active = 1 " +
                "AND fpr.day_type = ? " +
                "AND ts.start_time >= fpr.start_time AND ts.end_time <= fpr.end_time " +
                "ORDER BY fpr.start_time";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, courtId);
            ps.setInt(3, facilityId);
            ps.setString(4, dayType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("price") : null;
            }
        }
    }

    @Override
    public void reopenCancelledSlot(Connection conn, int bookingSlotId, BigDecimal price) throws Exception {
        String sql = "UPDATE BookingSlot SET slot_status = 'PENDING', is_released = 0, price = ?, checkin_time = NULL, checkout_time = NULL WHERE booking_slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, bookingSlotId);
            ps.executeUpdate();
        }
    }

    @Override
    public int insertPendingSlot(Connection conn, int bookingId, int courtId, int slotId, BigDecimal price) throws Exception {
        String sql = "INSERT INTO BookingSlot (booking_id, court_id, slot_id, price, is_released, slot_status) VALUES (?, ?, ?, ?, 0, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, courtId);
            ps.setInt(3, slotId);
            ps.setBigDecimal(4, price);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new Exception("Không thể tạo booking slot mới");
                }
                return keys.getInt(1);
            }
        }
    }

    @Override
    public void insertCourtSlotBooking(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId) throws Exception {
        String sql = "INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courtId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slotId);
            ps.setInt(4, bookingSlotId);
            ps.executeUpdate();
        }
    }

    @Override
    public BigDecimal sumActiveAmount(Connection conn, int bookingId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(price),0) AS total_amount FROM BookingSlot WHERE booking_id = ? AND slot_status <> 'CANCELLED'")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO;
            }
        }
    }

    @Override
    public BigDecimal findPaidAmount(Connection conn, int bookingId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT paid_amount FROM Invoice WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("paid_amount");
                throw new Exception("Invoice not found");
            }
        }
    }

        @Override
    public java.time.LocalDateTime findBookingCreatedAt(Connection conn, int bookingId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT created_at FROM Booking WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getTimestamp("created_at") != null) {
                    return rs.getTimestamp("created_at").toLocalDateTime();
                }
                return null;
            }
        }
    }

@Override
    public void updateInvoiceAfterRecalc(Connection conn, int bookingId, BigDecimal totalAmount, BigDecimal refundDue,
                                         String refundStatus, String refundNote, String paymentStatus) throws Exception {
        String sql = "UPDATE Invoice SET total_amount = ?, refund_due = ?, refund_status = ?, refund_note = ?, payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, totalAmount);
            ps.setBigDecimal(2, refundDue);
            ps.setString(3, refundStatus);
            if (refundNote == null) ps.setNull(4, java.sql.Types.NVARCHAR);
            else ps.setString(4, refundNote);
            ps.setString(5, paymentStatus);
            ps.setInt(6, bookingId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<StaffBookingEditStatusCountDTO> findSlotStatusCounts(Connection conn, int bookingId) throws Exception {
        String sql = "SELECT slot_status, COUNT(*) AS cnt FROM BookingSlot WHERE booking_id = ? GROUP BY slot_status";
        List<StaffBookingEditStatusCountDTO> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffBookingEditStatusCountDTO row = new StaffBookingEditStatusCountDTO();
                    row.setSlotStatus(rs.getString("slot_status"));
                    row.setCount(rs.getInt("cnt"));
                    out.add(row);
                }
            }
        }
        return out;
    }

    @Override
    public void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET booking_status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsSlotStatus(Connection conn, int bookingId, String slotStatus) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT TOP 1 1 FROM BookingSlot WHERE booking_id = ? AND slot_status = ?")) {
            ps.setInt(1, bookingId);
            ps.setString(2, slotStatus);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public StaffBookingEditSlotStateDTO findSlotState(Connection conn, int bookingId, int bookingSlotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT slot_status, is_released FROM BookingSlot WHERE booking_id = ? AND booking_slot_id = ?")) {
            ps.setInt(1, bookingId);
            ps.setInt(2, bookingSlotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                StaffBookingEditSlotStateDTO s = new StaffBookingEditSlotStateDTO();
                s.setSlotStatus(rs.getString("slot_status"));
                s.setReleased(rs.getBoolean("is_released"));
                return s;
            }
        }
    }

    @Override
    public void markSlotReleased(Connection conn, int bookingSlotId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE BookingSlot SET is_released = 1 WHERE booking_slot_id = ?")) {
            ps.setInt(1, bookingSlotId);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertAuditLog(Connection conn, int bookingId, int staffId, String changeAction, String changeType,
                               String reason, String beforeEtag, String afterEtag, String beforeJson,
                               String afterJson, BigDecimal refundDue) throws Exception {
        String sql = "INSERT INTO BookingChangeLog (booking_id, change_type, note, actor_staff_id, change_action, before_data, after_data, reason, etag_before, etag_after, refund_due) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setString(2, changeType);
            ps.setString(3, changeAction);
            ps.setInt(4, staffId);
            ps.setString(5, changeAction);
            ps.setNString(6, beforeJson);
            ps.setNString(7, afterJson);
            if (reason == null) ps.setNull(8, java.sql.Types.NVARCHAR);
            else ps.setNString(8, reason);
            ps.setString(9, beforeEtag);
            ps.setString(10, afterEtag);
            ps.setBigDecimal(11, refundDue);
            ps.executeUpdate();
        }
    }
}


