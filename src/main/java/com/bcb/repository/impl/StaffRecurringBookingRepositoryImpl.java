package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffCustomerAccountDTO;
import com.bcb.repository.staff.StaffRecurringBookingRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaffRecurringBookingRepositoryImpl implements StaffRecurringBookingRepository {

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
    public int insertBookingRoot(Connection conn, int facilityId, Integer accountId, Integer guestId, int staffId) throws Exception {
        String sql = "INSERT INTO Booking (facility_id, account_id, guest_id, staff_id, booking_status, hold_expired_at) " +
                "VALUES (?, ?, ?, ?, 'PENDING', NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facilityId);
            if (accountId == null) ps.setNull(2, java.sql.Types.INTEGER);
            else ps.setInt(2, accountId);
            if (guestId == null) ps.setNull(3, java.sql.Types.INTEGER);
            else ps.setInt(3, guestId);
            ps.setInt(4, staffId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to create booking root");
    }

    @Override
    public int insertRecurringBooking(Connection conn, int facilityId, LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "INSERT INTO RecurringBooking (facility_id, start_date, end_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to create recurring booking");
    }

    @Override
    public void updateBookingRecurringId(Connection conn, int bookingId, int recurringId) throws Exception {
        String sql = "UPDATE Booking SET recurring_id = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recurringId);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateBookingStatus(Connection conn, int bookingId, String status) throws Exception {
        String sql = "UPDATE Booking SET booking_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertRecurringPattern(Connection conn, int recurringId, int courtId, int dayOfWeek, int slotId) throws Exception {
        String sql = "INSERT INTO RecurringPattern (recurring_id, court_id, day_of_week, slot_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recurringId);
            ps.setInt(2, courtId);
            ps.setInt(3, dayOfWeek);
            ps.setInt(4, slotId);
            ps.executeUpdate();
        }
    }

    @Override
    public int insertBookingSlot(Connection conn, int bookingId, int courtId, LocalDate bookingDate, int slotId, BigDecimal price)
            throws Exception {
        String sql = "INSERT INTO BookingSlot (booking_id, court_id, booking_date, slot_id, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, courtId);
            ps.setDate(3, Date.valueOf(bookingDate));
            ps.setInt(4, slotId);
            ps.setBigDecimal(5, price);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to insert booking slot");
    }

    @Override
    public void insertCourtSlotBooking(Connection conn, int courtId, LocalDate bookingDate, int slotId, int bookingSlotId)
            throws Exception {
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
    public void insertBookingSkip(Connection conn, int recurringId, LocalDate skipDate, String reason) throws Exception {
        String sql = "INSERT INTO BookingSkip (recurring_id, skip_date, reason) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recurringId);
            ps.setDate(2, Date.valueOf(skipDate));
            ps.setNString(3, reason);
            ps.executeUpdate();
        }
    }

    @Override
    public int insertInvoice(Connection conn, int bookingId, BigDecimal totalAmount) throws Exception {
        String sql = "INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status) " +
                "VALUES (?, ?, 0, 100, 'UNPAID')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, totalAmount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new Exception("Failed to insert invoice");
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
    public List<Integer> loadCourtsByType(Connection conn, int facilityId, int courtTypeId) throws Exception {
        String sql = "SELECT court_id FROM Court WHERE facility_id = ? AND court_type_id = ? AND is_active = 1";
        List<Integer> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, courtTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getInt("court_id"));
                }
            }
        }
        return out;
    }

    @Override
    public Map<Integer, List<Integer>> findBookedSlots(Connection conn, int facilityId, LocalDate bookingDate) throws Exception {
        String sql = "SELECT csb.court_id, csb.slot_id " +
                "FROM CourtSlotBooking csb " +
                "INNER JOIN Court c ON csb.court_id = c.court_id " +
                "WHERE c.facility_id = ? AND csb.booking_date = ?";
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int courtId = rs.getInt("court_id");
                    int slotId = rs.getInt("slot_id");
                    map.computeIfAbsent(courtId, k -> new ArrayList<>()).add(slotId);
                }
            }
        }
        return map;
    }
}
