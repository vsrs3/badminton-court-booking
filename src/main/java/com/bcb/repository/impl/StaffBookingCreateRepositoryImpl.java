package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingCreateSlotDTO;
import com.bcb.dto.staff.StaffCustomerAccountDTO;
import com.bcb.repository.staff.StaffBookingCreateRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class StaffBookingCreateRepositoryImpl implements StaffBookingCreateRepository {

    @Override
    public StaffCustomerAccountDTO findActiveCustomerByPhone(String phone) throws Exception {
        String sql = "SELECT TOP 1 account_id, full_name, phone, email " +
                "FROM Account WHERE role = 'CUSTOMER' AND is_active = 1 AND phone = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                StaffCustomerAccountDTO out = new StaffCustomerAccountDTO();
                out.setAccountId(rs.getInt("account_id"));
                out.setFullName(rs.getString("full_name"));
                out.setPhone(rs.getString("phone"));
                out.setEmail(rs.getString("email"));
                return out;
            }
        }
    }

    @Override
    public Integer insertGuest(Connection conn, String guestName, String guestPhone, String guestEmail) throws Exception {
        String sql = "INSERT INTO Guest (guest_name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, guestName.trim());
            ps.setString(2, guestPhone.trim());
            if (guestEmail == null || guestEmail.trim().isEmpty()) {
                ps.setNull(3, java.sql.Types.NVARCHAR);
            } else {
                ps.setString(3, guestEmail.trim());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return null;
    }

    @Override
    public int insertBookingForAccount(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                                       int accountId, int staffId) throws Exception {
        String sql = "INSERT INTO Booking (facility_id, booking_date, account_id, staff_id, booking_status) " +
                "VALUES (?, ?, ?, ?, 'CONFIRMED')";
        return insertBooking(conn, sql, facilityId, bookingDate, accountId, staffId);
    }

    @Override
    public int insertBookingForGuest(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                                     int guestId, int staffId) throws Exception {
        String sql = "INSERT INTO Booking (facility_id, booking_date, guest_id, staff_id, booking_status) " +
                "VALUES (?, ?, ?, ?, 'CONFIRMED')";
        return insertBooking(conn, sql, facilityId, bookingDate, guestId, staffId);
    }

    private int insertBooking(Connection conn, String sql, int facilityId, java.time.LocalDate bookingDate,
                              int accountOrGuestId, int staffId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, accountOrGuestId);
            ps.setInt(4, staffId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to create booking");
    }

    @Override
    public Map<String, BigDecimal> loadPrices(Connection conn, int facilityId, String dayType) throws Exception {
        String sql = "SELECT court_type_id, start_time, end_time, price FROM FacilityPriceRule " +
                "WHERE facility_id = ? AND day_type = ?";
        Map<String, BigDecimal> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setString(2, dayType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getInt("court_type_id") + "|" +
                            rs.getTime("start_time").toLocalTime() + "|" +
                            rs.getTime("end_time").toLocalTime();
                    map.put(key, rs.getBigDecimal("price"));
                }
            }
        }
        return map;
    }

    @Override
    public Map<Integer, Integer> loadCourtTypes(Connection conn, int facilityId) throws Exception {
        String sql = "SELECT court_id, court_type_id FROM Court WHERE facility_id = ? AND is_active = 1";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("court_id"), rs.getInt("court_type_id"));
                }
            }
        }
        return map;
    }

    @Override
    public Map<Integer, LocalTime[]> loadSlotTimes(Connection conn) throws Exception {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot ORDER BY start_time";
        Map<Integer, LocalTime[]> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("slot_id"), new LocalTime[]{
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime()
                });
            }
        }
        return map;
    }

    @Override
    public Map<Integer, Integer> loadSlotOrder(Connection conn) throws Exception {
        String sql = "SELECT slot_id, start_time FROM TimeSlot ORDER BY start_time";
        Map<Integer, Integer> slotOrder = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int idx = 0;
            while (rs.next()) {
                slotOrder.put(rs.getInt("slot_id"), idx++);
            }
        }
        return slotOrder;
    }

    @Override
    public int insertBookingSlot(Connection conn, int bookingId, java.time.LocalDate bookingDate,
                                 StaffBookingCreateSlotDTO slot, BigDecimal price) throws Exception {
        String sql = "INSERT INTO BookingSlot (booking_id, booking_date, court_id, slot_id, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slot.getCourtId());
            ps.setInt(4, slot.getSlotId());
            ps.setBigDecimal(5, price);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to insert booking slot");
    }

    @Override
    public void insertCourtSlotBooking(Connection conn, StaffBookingCreateSlotDTO slot,
                                       java.time.LocalDate bookingDate, int bookingSlotId) throws Exception {
        String sql = "INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot.getCourtId());
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slot.getSlotId());
            ps.setInt(4, bookingSlotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void insertInvoice(Connection conn, int bookingId, BigDecimal totalAmount) throws Exception {
        String sql = "INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status) " +
                "VALUES (?, ?, 0, 100, 'UNPAID')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, totalAmount);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertBookingRental(Connection conn, int bookingSlotId,
                                    int inventoryId, int quantity, BigDecimal unitPrice,
                                    String addedBy) throws Exception {
        String sql = "INSERT INTO RacketRental " +
                "(booking_slot_id, inventory_id, quantity, unit_price, added_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, GETDATE())";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingSlotId);
            ps.setInt(2, inventoryId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.setString(5, addedBy);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateInventoryRentalScheduleStatus(Connection conn, int facilityId, java.time.LocalDate bookingDate,
                                                    int courtId, int slotId, int inventoryId, String status) throws Exception {
        String sql = """
                UPDATE InventoryRentalSchedule
                SET status = ?
                WHERE facility_id = ?
                  AND booking_date = ?
                  AND court_id = ?
                  AND slot_id = ?
                  AND inventory_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, status);
            ps.setInt(2, facilityId);
            ps.setDate(3, Date.valueOf(bookingDate));
            ps.setInt(4, courtId);
            ps.setInt(5, slotId);
            ps.setInt(6, inventoryId);
            ps.executeUpdate();
        }
    }
}

