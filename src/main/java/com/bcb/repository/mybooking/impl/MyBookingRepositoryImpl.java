package com.bcb.repository.mybooking.impl;

import com.bcb.dto.mybooking.BookingSlotDetailDTO;
import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.mybooking.MyBookingRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link MyBookingRepository}.
 */
public class MyBookingRepositoryImpl implements MyBookingRepository {

    /** {@inheritDoc} */
    @Override
    public List<MyBookingListDTO> findMyBookings(int accountId, String status,
                                                  LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.booking_id, f.name AS facility_name, ")
           .append("       CONCAT(f.address, ', ', f.ward, ', ', f.district, ', ', f.province) AS full_address, ")
           .append("       b.booking_date, b.booking_status, b.recurring_id, b.created_at, b.hold_expired_at, ")
           .append("       i.total_amount, i.paid_amount, i.payment_status, ")
           .append("       fi.image_path AS thumbnail_path, ")
           .append("       STRING_AGG(CONCAT(c.court_name, '|', ")
           .append("           FORMAT(CAST(ts.start_time AS DATETIME), 'HH:mm'), '|', ")
           .append("           FORMAT(CAST(ts.end_time AS DATETIME), 'HH:mm')), ';;') ")
           .append("           WITHIN GROUP (ORDER BY c.court_name, ts.start_time) AS slot_raw ")
           .append("FROM Booking b ")
           .append("JOIN Facility f ON b.facility_id = f.facility_id ")
           .append("LEFT JOIN Invoice i ON b.booking_id = i.booking_id ")
           .append("LEFT JOIN FacilityImage fi ON f.facility_id = fi.facility_id AND fi.is_thumbnail = 1 ")
           .append("JOIN BookingSlot bs ON b.booking_id = bs.booking_id ")
           .append("JOIN Court c ON bs.court_id = c.court_id ")
           .append("JOIN TimeSlot ts ON bs.slot_id = ts.slot_id ")
           .append("WHERE b.account_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(accountId);

        // Dynamic filters
        if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            sql.append("AND b.booking_status = ? ");
            params.add(status.toUpperCase());
        }
        if (dateFrom != null) {
            sql.append("AND b.booking_date >= ? ");
            params.add(Date.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append("AND b.booking_date <= ? ");
            params.add(Date.valueOf(dateTo));
        }

        sql.append("GROUP BY b.booking_id, f.name, f.address, f.ward, f.district, f.province, ")
           .append("         b.booking_date, b.booking_status, b.recurring_id, b.created_at, b.hold_expired_at, ")
           .append("         i.total_amount, i.paid_amount, i.payment_status, fi.image_path ")
           .append("ORDER BY b.created_at DESC");

        List<MyBookingListDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MyBookingListDTO dto = new MyBookingListDTO();
                    dto.setBookingId(rs.getInt("booking_id"));
                    dto.setFacilityName(rs.getString("facility_name"));
                    dto.setFullAddress(rs.getString("full_address"));
                    dto.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    dto.setBookingStatus(rs.getString("booking_status"));

                    // slot_raw: "CourtA|08:00|08:30;;CourtA|08:30|09:00;;CourtB|09:00|09:30"
                    // merge logic delegated to service layer
                    dto.setSlotDetails(rs.getString("slot_raw"));

                    Integer recurringId = rs.getObject("recurring_id") != null
                            ? rs.getInt("recurring_id") : null;
                    dto.setBookingType(recurringId != null ? "RECURRING" : "SINGLE");

                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                    dto.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

                    BigDecimal paidAmount = rs.getBigDecimal("paid_amount");
                    dto.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);

                    dto.setPaymentStatus(rs.getString("payment_status"));

                    Timestamp createdTs = rs.getTimestamp("created_at");
                    if (createdTs != null) dto.setCreatedAt(createdTs.toLocalDateTime());

                    Timestamp holdTs = rs.getTimestamp("hold_expired_at");
                    if (holdTs != null) dto.setHoldExpiredAt(holdTs.toLocalDateTime());

                    dto.setThumbnailPath(rs.getString("thumbnail_path"));

                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find my bookings", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<MyBookingDetailDTO> findBookingDetail(int bookingId, int accountId) {
        String sql =
            "SELECT b.booking_id, b.booking_date, b.booking_status, b.recurring_id, " +
            "       b.created_at, b.hold_expired_at, " +
            "       f.name AS facility_name, " +
            "       CONCAT(f.address, ', ', f.ward, ', ', f.district, ', ', f.province) AS full_address, " +
            "       fi.image_path AS thumbnail_path, " +
            "       i.total_amount, i.paid_amount, i.payment_status, " +
            "       a_staff.full_name AS staff_name, a_staff.phone AS staff_phone " +
            "FROM Booking b " +
            "JOIN Facility f ON b.facility_id = f.facility_id " +
            "LEFT JOIN Invoice i ON b.booking_id = i.booking_id " +
            "LEFT JOIN FacilityImage fi ON f.facility_id = fi.facility_id AND fi.is_thumbnail = 1 " +
            "LEFT JOIN Staff s ON s.facility_id = f.facility_id AND s.is_active = 1 " +
            "LEFT JOIN Account a_staff ON s.account_id = a_staff.account_id " +
            "WHERE b.booking_id = ? AND b.account_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MyBookingDetailDTO dto = new MyBookingDetailDTO();
                    dto.setBookingId(rs.getInt("booking_id"));
                    dto.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    dto.setBookingStatus(rs.getString("booking_status"));

                    Integer recurringId = rs.getObject("recurring_id") != null
                            ? rs.getInt("recurring_id") : null;
                    dto.setBookingType(recurringId != null ? "RECURRING" : "SINGLE");

                    Timestamp createdTs = rs.getTimestamp("created_at");
                    if (createdTs != null) dto.setCreatedAt(createdTs.toLocalDateTime());

                    Timestamp holdTs = rs.getTimestamp("hold_expired_at");
                    if (holdTs != null) dto.setHoldExpiredAt(holdTs.toLocalDateTime());

                    dto.setFacilityName(rs.getString("facility_name"));
                    dto.setFullAddress(rs.getString("full_address"));
                    dto.setThumbnailPath(rs.getString("thumbnail_path"));

                    dto.setTotalAmount(rs.getBigDecimal("total_amount"));
                    dto.setPaidAmount(rs.getBigDecimal("paid_amount"));
                    dto.setPaymentStatus(rs.getString("payment_status"));

                    dto.setStaffName(rs.getString("staff_name"));
                    dto.setStaffPhone(rs.getString("staff_phone"));

                    return Optional.of(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find booking detail", e);
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public List<BookingSlotDetailDTO> findSlotsByBookingId(int bookingId) {
        String sql =
            "SELECT bs.booking_slot_id, c.court_name, " +
            "       FORMAT(CAST(ts.start_time AS DATETIME), 'HH:mm') AS start_time, " +
            "       FORMAT(CAST(ts.end_time AS DATETIME), 'HH:mm') AS end_time, " +
            "       bs.price, bs.slot_status " +
            "FROM BookingSlot bs " +
            "JOIN Court c ON bs.court_id = c.court_id " +
            "JOIN TimeSlot ts ON bs.slot_id = ts.slot_id " +
            "WHERE bs.booking_id = ? " +
            "ORDER BY ts.start_time, c.court_name";

        List<BookingSlotDetailDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingSlotDetailDTO dto = new BookingSlotDetailDTO();
                    dto.setBookingSlotId(rs.getInt("booking_slot_id"));
                    dto.setCourtName(rs.getString("court_name"));
                    dto.setStartTime(rs.getString("start_time"));
                    dto.setEndTime(rs.getString("end_time"));
                    dto.setPrice(rs.getBigDecimal("price"));
                    dto.setSlotStatus(rs.getString("slot_status"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find slots by booking ID", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCancellable(int bookingId, int accountId) {
        String sql =
            "SELECT COUNT(*) AS cnt " +
            "FROM Booking b " +
            "LEFT JOIN Invoice i ON b.booking_id = i.booking_id " +
            "WHERE b.booking_id = ? " +
            "  AND b.account_id = ? " +
            "  AND b.booking_status IN ('PENDING', 'CONFIRMED') " +
            "  AND (i.payment_status IS NULL OR i.payment_status = 'UNPAID')";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check cancellable", e);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelBooking(int bookingId) {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete CourtSlotBooking locks
            String deleteLocksSQL =
                "DELETE FROM CourtSlotBooking WHERE booking_slot_id IN " +
                "(SELECT booking_slot_id FROM BookingSlot WHERE booking_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(deleteLocksSQL)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // 2. Update BookingSlot statuses
            String updateSlotsSQL =
                "UPDATE BookingSlot SET slot_status = 'CANCELLED' WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSlotsSQL)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // 3. Update Booking status
            String updateBookingSQL =
                "UPDATE Booking SET booking_status = 'CANCELLED' WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSQL)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) { /* ignored */ }
            }
            throw new DataAccessException("Failed to cancel booking", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) { /* ignored */ }
            }
        }
    }
}
