package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffRentalInventoryStockDTO;
import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;
import com.bcb.repository.staff.StaffRentalStatusRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusRepositoryImpl implements StaffRentalStatusRepository {

    private static final String STATUS_RENTING = "RENTING";
    private static final String STATUS_RETURNED = "RETURNED";

    @Override
    public List<StaffRentalStatusCourtDTO> findCourtsByFacility(int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return findCourtsByFacility(conn, facilityId);
        }
    }

    public List<StaffRentalStatusCourtDTO> findCourtsByFacility(Connection conn, int facilityId) throws Exception {
        String sql = """
                SELECT c.court_id, c.court_name
                FROM Court c
                WHERE c.facility_id = ?
                  AND c.is_active = 1
                ORDER BY c.court_name ASC
                """;

        List<StaffRentalStatusCourtDTO> courts = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalStatusCourtDTO dto = new StaffRentalStatusCourtDTO();
                    dto.setCourtId(rs.getInt("court_id"));
                    dto.setCourtName(rs.getString("court_name"));
                    courts.add(dto);
                }
            }
        }
        return courts;
    }

    @Override
    public List<StaffRentalStatusRawRowDTO> findRentalStatusRows(int facilityId, LocalDate bookingDate) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return findRentalStatusRows(conn, facilityId, bookingDate);
        }
    }

    public List<StaffRentalStatusRawRowDTO> findRentalStatusRows(Connection conn, int facilityId, LocalDate bookingDate)
            throws Exception {
        String sql = """
                SELECT
                    irs.id AS schedule_id,
                    irs.booking_date,
                    irs.court_id,
                    c.court_name,
                    irs.slot_id,
                    ts.start_time,
                    ts.end_time,
                    irs.inventory_id,
                    i.name AS inventory_name,
                    irs.quantity,
                    irs.status,
                    COALESCE(b.booking_id, 0) AS booking_id,
                    b.account_id,
                    b.guest_id,
                    COALESCE(a.full_name, g.guest_name, N'Khách thuê') AS customer_name
                FROM InventoryRentalSchedule irs
                JOIN Court c
                    ON c.court_id = irs.court_id
                JOIN TimeSlot ts
                    ON ts.slot_id = irs.slot_id
                JOIN Inventory i
                    ON i.inventory_id = irs.inventory_id
                LEFT JOIN CourtSlotBooking csb
                    ON csb.court_id = irs.court_id
                   AND csb.booking_date = irs.booking_date
                   AND csb.slot_id = irs.slot_id
                LEFT JOIN BookingSlot bs
                    ON bs.booking_slot_id = csb.booking_slot_id
                LEFT JOIN Booking b
                    ON b.booking_id = bs.booking_id
                   AND b.facility_id = irs.facility_id
                LEFT JOIN Account a
                    ON a.account_id = b.account_id
                LEFT JOIN Guest g
                    ON g.guest_id = b.guest_id
                WHERE irs.facility_id = ?
                  AND irs.booking_date = ?
                ORDER BY c.court_name ASC,
                         ts.start_time ASC,
                         customer_name ASC,
                         i.name ASC,
                         irs.id ASC
                """;

        List<StaffRentalStatusRawRowDTO> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalStatusRawRowDTO row = new StaffRentalStatusRawRowDTO();
                    row.setScheduleId(rs.getInt("schedule_id"));
                    row.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    row.setCourtId(rs.getInt("court_id"));
                    row.setCourtName(rs.getString("court_name"));
                    row.setSlotId(rs.getInt("slot_id"));
                    row.setStartTime(rs.getTime("start_time").toLocalTime());
                    row.setEndTime(rs.getTime("end_time").toLocalTime());
                    row.setInventoryId(rs.getInt("inventory_id"));
                    row.setInventoryName(rs.getString("inventory_name"));
                    row.setQuantity(rs.getInt("quantity"));
                    row.setStatus(rs.getString("status"));
                    row.setBookingId(rs.getInt("booking_id"));
                    row.setAccountId((Integer) rs.getObject("account_id"));
                    row.setGuestId((Integer) rs.getObject("guest_id"));
                    row.setCustomerName(rs.getString("customer_name"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public List<StaffRentalInventoryStockDTO> findInventoryStocks(int facilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return findInventoryStocks(conn, facilityId);
        }
    }

    public List<StaffRentalInventoryStockDTO> findInventoryStocks(Connection conn, int facilityId) throws Exception {
        String sql = """
                SELECT
                    fi.facility_inventory_id,
                    fi.inventory_id,
                    i.name AS inventory_name,
                    fi.total_quantity,
                    fi.available_quantity
                FROM FacilityInventory fi
                JOIN Inventory i
                    ON i.inventory_id = fi.inventory_id
                WHERE fi.facility_id = ?
                  AND i.is_active = 1
                ORDER BY i.name ASC
                """;

        List<StaffRentalInventoryStockDTO> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalInventoryStockDTO dto = new StaffRentalInventoryStockDTO();
                    dto.setFacilityInventoryId(rs.getInt("facility_inventory_id"));
                    dto.setInventoryId(rs.getInt("inventory_id"));
                    dto.setInventoryName(rs.getString("inventory_name"));
                    dto.setTotalQuantity(rs.getInt("total_quantity"));
                    dto.setAvailableQuantity(rs.getInt("available_quantity"));
                    items.add(dto);
                }
            }
        }
        return items;
    }

    @Override
    public int updateScheduleStatus(int facilityId, int scheduleId, String nextStatus) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                StaffRentalStatusRawRowDTO row = loadScheduleRowForUpdate(conn, facilityId, scheduleId);
                if (row == null) {
                    conn.rollback();
                    return 0;
                }

                String currentStatus = row.getStatus();
                int delta = calculateInventoryDelta(currentStatus, nextStatus, row.getQuantity());
                if (delta != 0) {
                    adjustFacilityInventory(conn, facilityId, row.getInventoryId(), delta);
                }

                int latestAvailable = loadCurrentAvailableQuantity(conn, facilityId, row.getInventoryId());
                int updated = updateScheduleRow(conn, facilityId, scheduleId, nextStatus, latestAvailable);
                if (updated > 0) {
                    syncRacketRentalLogStatus(conn, facilityId, row, STATUS_RETURNED.equals(nextStatus));
                }

                conn.commit();
                return updated;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private StaffRentalStatusRawRowDTO loadScheduleRowForUpdate(Connection conn, int facilityId, int scheduleId) throws Exception {
        String sql = """
                SELECT
                    irs.id AS schedule_id,
                    irs.booking_date,
                    irs.court_id,
                    irs.slot_id,
                    irs.inventory_id,
                    irs.quantity,
                    irs.status
                FROM InventoryRentalSchedule irs WITH (UPDLOCK, ROWLOCK)
                WHERE irs.facility_id = ?
                  AND irs.id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                StaffRentalStatusRawRowDTO row = new StaffRentalStatusRawRowDTO();
                row.setScheduleId(rs.getInt("schedule_id"));
                row.setBookingDate(rs.getDate("booking_date").toLocalDate());
                row.setCourtId(rs.getInt("court_id"));
                row.setSlotId(rs.getInt("slot_id"));
                row.setInventoryId(rs.getInt("inventory_id"));
                row.setQuantity(rs.getInt("quantity"));
                row.setStatus(rs.getString("status"));
                return row;
            }
        }
    }

    private int calculateInventoryDelta(String currentStatus, String nextStatus, int quantity) {
        boolean wasRenting = STATUS_RENTING.equalsIgnoreCase(currentStatus);
        boolean willRenting = STATUS_RENTING.equalsIgnoreCase(nextStatus);

        if (!wasRenting && willRenting) {
            return -quantity;
        }
        if (wasRenting && !willRenting) {
            return quantity;
        }
        return 0;
    }

    private void adjustFacilityInventory(Connection conn, int facilityId, int inventoryId, int delta) throws Exception {
        String selectSql = """
                SELECT total_quantity, available_quantity
                FROM FacilityInventory WITH (UPDLOCK, ROWLOCK)
                WHERE facility_id = ?
                  AND inventory_id = ?
                """;

        int totalQuantity;
        int availableQuantity;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, inventoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Khong tim thay ton kho cua do thue.");
                }
                totalQuantity = rs.getInt("total_quantity");
                availableQuantity = rs.getInt("available_quantity");
            }
        }

        int nextAvailable = availableQuantity + delta;
        if (nextAvailable < 0) {
            throw new IllegalArgumentException("So luong kha dung khong du de chuyen sang trang thai dang thue.");
        }
        if (nextAvailable > totalQuantity) {
            throw new IllegalArgumentException("So luong kha dung khong the vuot qua tong so luong trong kho.");
        }

        String updateSql = """
                UPDATE FacilityInventory
                SET available_quantity = ?
                WHERE facility_id = ?
                  AND inventory_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, nextAvailable);
            ps.setInt(2, facilityId);
            ps.setInt(3, inventoryId);
            ps.executeUpdate();
        }
    }

    private int loadCurrentAvailableQuantity(Connection conn, int facilityId, int inventoryId) throws Exception {
        String sql = """
                SELECT available_quantity
                FROM FacilityInventory
                WHERE facility_id = ?
                  AND inventory_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, inventoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_quantity");
                }
            }
        }
        throw new IllegalArgumentException("Khong tim thay ton kho cua do thue.");
    }

    private int updateScheduleRow(Connection conn, int facilityId, int scheduleId, String nextStatus, int latestAvailable) throws Exception {
        String sql = """
                UPDATE InventoryRentalSchedule
                SET status = ?,
                    availableItem = ?
                WHERE facility_id = ?
                  AND id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nextStatus);
            ps.setInt(2, latestAvailable);
            ps.setInt(3, facilityId);
            ps.setInt(4, scheduleId);
            return ps.executeUpdate();
        }
    }

    private void syncRacketRentalLogStatus(Connection conn, int facilityId, StaffRentalStatusRawRowDTO row, boolean returned)
            throws Exception {
        String sql = """
                UPDATE rrl
                SET returned_at = ?
                FROM RacketRentalLog rrl
                JOIN BookingSlot bs
                    ON bs.booking_slot_id = rrl.booking_slot_id
                JOIN Booking b
                    ON b.booking_id = bs.booking_id
                JOIN FacilityInventory fi
                    ON fi.facility_inventory_id = rrl.facility_inventory_id
                WHERE b.facility_id = ?
                  AND b.booking_date = ?
                  AND bs.court_id = ?
                  AND bs.slot_id = ?
                  AND fi.inventory_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (returned) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setInt(2, facilityId);
            ps.setDate(3, Date.valueOf(row.getBookingDate()));
            ps.setInt(4, row.getCourtId());
            ps.setInt(5, row.getSlotId());
            ps.setInt(6, row.getInventoryId());
            ps.executeUpdate();
        }
    }
}
