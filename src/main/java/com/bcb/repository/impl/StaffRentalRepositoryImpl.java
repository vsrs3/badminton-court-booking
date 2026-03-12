package com.bcb.repository.impl;
import com.bcb.dto.staff.StaffRentalInventoryItemDTO;
import com.bcb.repository.staff.StaffRentalRepository;
import com.bcb.utils.DBContext;
import com.bcb.repository.impl.StaffRentalRepositoryImpl;
import com.bcb.repository.staff.StaffRentalRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StaffRentalRepositoryImpl implements StaffRentalRepository {

    @Override
    public List<StaffRentalInventoryItemDTO> findRentalItems(int facilityId, String keyword, int page, int pageSize) throws Exception {
        List<StaffRentalInventoryItemDTO> list = new ArrayList<>();

        String sql = """
                SELECT
                    fi.facility_inventory_id,
                    i.inventory_id,
                    i.name,
                    i.brand,
                    i.description,
                    i.rental_price,
                    fi.available_quantity
                FROM FacilityInventory fi
                JOIN Inventory i ON i.inventory_id = fi.inventory_id
                WHERE fi.facility_id = ?
                  AND i.is_active = 1
                  AND (
                        ? IS NULL
                        OR i.name LIKE ?
                        OR i.brand LIKE ?
                        OR i.description LIKE ?
                  )
                ORDER BY i.name ASC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        int offset = (page - 1) * pageSize;
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : "%" + keyword.trim() + "%";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
            ps.setInt(6, offset);
            ps.setInt(7, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalInventoryItemDTO dto = new StaffRentalInventoryItemDTO();
                    dto.setFacilityInventoryId(rs.getInt("facility_inventory_id"));
                    dto.setInventoryId(rs.getInt("inventory_id"));
                    dto.setName(rs.getString("name"));
                    dto.setBrand(rs.getString("brand"));
                    dto.setDescription(rs.getString("description"));
                    dto.setRentalPrice(rs.getBigDecimal("rental_price"));
                    dto.setAvailableQuantity(rs.getInt("available_quantity"));
                    list.add(dto);
                }
            }
        }

        return list;
    }

    @Override
    public int countRentalItems(int facilityId, String keyword) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM FacilityInventory fi
                JOIN Inventory i ON i.inventory_id = fi.inventory_id
                WHERE fi.facility_id = ?
                  AND i.is_active = 1
                  AND (
                        ? IS NULL
                        OR i.name LIKE ?
                        OR i.brand LIKE ?
                        OR i.description LIKE ?
                  )
                """;

        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : "%" + keyword.trim() + "%";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facilityId);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public boolean existsRacketRental(int bookingSlotId, int inventoryId) throws Exception {
        String sql = """
                SELECT 1
                FROM RacketRental
                WHERE booking_slot_id = ? AND inventory_id = ?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingSlotId);
            ps.setInt(2, inventoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void insertRacketRental(int bookingSlotId, int inventoryId, int quantity, BigDecimal unitPrice, int addedBy) throws Exception {
        String sql = """
                INSERT INTO RacketRental
                (booking_slot_id, inventory_id, quantity, unit_price, added_by, created_at)
                VALUES (?, ?, ?, ?, ?, GETDATE())
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingSlotId);
            ps.setInt(2, inventoryId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.setInt(5, addedBy);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateRacketRental(int bookingSlotId, int inventoryId, int quantity) throws Exception {
        String sql = """
                UPDATE RacketRental
                SET quantity = ?
                WHERE booking_slot_id = ? AND inventory_id = ?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, bookingSlotId);
            ps.setInt(3, inventoryId);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteRacketRental(int bookingSlotId, int inventoryId) throws Exception {
        String sql = """
                DELETE FROM RacketRental
                WHERE booking_slot_id = ? AND inventory_id = ?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingSlotId);
            ps.setInt(2, inventoryId);
            ps.executeUpdate();
        }
    }

    @Override
    public BigDecimal getBookingRentalTotal(int bookingId) throws Exception {
        String sql = """
                SELECT ISNULL(SUM(rr.quantity * rr.unit_price), 0)
                FROM RacketRental rr
                JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
                WHERE bs.booking_id = ?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> getBookingRentalRows(int bookingId) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
                SELECT
                    bs.booking_slot_id,
                    bs.court_id,
                    c.court_name,
                    ts.start_time,
                    ts.end_time,
                    rr.inventory_id,
                    i.name AS inventory_name,
                    rr.quantity,
                    rr.unit_price,
                    (rr.quantity * rr.unit_price) AS line_total
                FROM RacketRental rr
                JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
                JOIN Court c ON c.court_id = bs.court_id
                JOIN TimeSlot ts ON ts.slot_id = bs.slot_id
                JOIN Inventory i ON i.inventory_id = rr.inventory_id
                WHERE bs.booking_id = ?
                ORDER BY c.court_name, ts.start_time, i.name
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("bookingSlotId", rs.getInt("booking_slot_id"));
                    row.put("courtId", rs.getInt("court_id"));
                    row.put("courtName", rs.getString("court_name"));
                    row.put("startTime", rs.getString("start_time"));
                    row.put("endTime", rs.getString("end_time"));
                    row.put("inventoryId", rs.getInt("inventory_id"));
                    row.put("inventoryName", rs.getString("inventory_name"));
                    row.put("quantity", rs.getInt("quantity"));
                    row.put("unitPrice", rs.getBigDecimal("unit_price"));
                    row.put("lineTotal", rs.getBigDecimal("line_total"));
                    rows.add(row);
                }
            }
        }

        return rows;
    }

    @Override
    public void insertRentalLogAndDecreaseStock(int bookingId, int facilityId, int staffId) throws Exception {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String insertLogSql = """
                    INSERT INTO RacketRentalLog (
                        booking_slot_id,
                        facility_inventory_id,
                        quantity,
                        staff_id,
                        rented_at,
                        returned_at
                    )
                    SELECT
                        rr.booking_slot_id,
                        fi.facility_inventory_id,
                        rr.quantity,
                        ?,
                        GETDATE(),
                        NULL
                    FROM RacketRental rr
                    JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
                    JOIN Court c ON c.court_id = bs.court_id
                    JOIN FacilityInventory fi
                         ON fi.facility_id = c.facility_id
                        AND fi.inventory_id = rr.inventory_id
                    WHERE bs.booking_id = ?
                      AND c.facility_id = ?
                      AND NOT EXISTS (
                          SELECT 1
                          FROM RacketRentalLog rrl
                          WHERE rrl.booking_slot_id = rr.booking_slot_id
                            AND rrl.facility_inventory_id = fi.facility_inventory_id
                      )
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertLogSql)) {
                ps.setInt(1, staffId);
                ps.setInt(2, bookingId);
                ps.setInt(3, facilityId);
                ps.executeUpdate();
            }

            String updateStockSql = """
                    UPDATE fi
                    SET fi.available_quantity = fi.available_quantity - x.total_qty
                    FROM FacilityInventory fi
                    JOIN (
                        SELECT
                            c.facility_id,
                            rr.inventory_id,
                            SUM(rr.quantity) AS total_qty
                        FROM RacketRental rr
                        JOIN BookingSlot bs ON bs.booking_slot_id = rr.booking_slot_id
                        JOIN Court c ON c.court_id = bs.court_id
                        WHERE bs.booking_id = ?
                          AND c.facility_id = ?
                        GROUP BY c.facility_id, rr.inventory_id
                    ) x
                    ON fi.facility_id = x.facility_id
                    AND fi.inventory_id = x.inventory_id
                    """;

            try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, facilityId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }


}
