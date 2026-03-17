package com.bcb.repository.impl;

import com.bcb.dto.staff.InventoryRentalScheduleSaveItemDTO;
import com.bcb.dto.staff.StaffRentalInventoryItemDTO;
import com.bcb.repository.staff.StaffRentalScheduleRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffRentalScheduleRepositoryImpl implements StaffRentalScheduleRepository {

    @Override
    public List<StaffRentalInventoryItemDTO> findRentalItemsForSlot(
            int facilityId, LocalDate bookingDate, int courtId, int slotId, String keyword, int page, int pageSize)
            throws Exception {

        String sql = """
                SELECT
                    fi.facility_inventory_id,
                    i.inventory_id,
                    i.name,
                    i.brand,
                    i.description,
                    i.rental_price,
                    fi.available_quantity
                        - ISNULL(other_slot.used_quantity, 0) AS available_quantity,
                    ISNULL(current_slot.quantity, 0) AS selected_quantity
                FROM FacilityInventory fi
                JOIN Inventory i ON i.inventory_id = fi.inventory_id
                OUTER APPLY (
                    SELECT SUM(s.quantity) AS used_quantity
                    FROM InventoryRentalSchedule s
                    WHERE s.facility_id = fi.facility_id
                      AND s.booking_date = ?
                      AND s.slot_id = ?
                      AND s.inventory_id = fi.inventory_id
                      AND s.court_id <> ?
                      AND s.status IN (N'RENTING', N'RENTED')
                ) other_slot
                OUTER APPLY (
                    SELECT TOP 1 s.quantity
                    FROM InventoryRentalSchedule s
                    WHERE s.facility_id = fi.facility_id
                      AND s.booking_date = ?
                      AND s.court_id = ?
                      AND s.slot_id = ?
                      AND s.inventory_id = fi.inventory_id
                    ORDER BY s.id DESC
                ) current_slot
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

        int offset = Math.max(0, page - 1) * pageSize;
        String kw = normalizeKeyword(keyword);
        List<StaffRentalInventoryItemDTO> items = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(bookingDate));
            ps.setInt(idx++, slotId);
            ps.setInt(idx++, courtId);
            ps.setDate(idx++, Date.valueOf(bookingDate));
            ps.setInt(idx++, courtId);
            ps.setInt(idx++, slotId);
            ps.setInt(idx++, facilityId);
            ps.setString(idx++, kw);
            ps.setString(idx++, kw);
            ps.setString(idx++, kw);
            ps.setString(idx++, kw);
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalInventoryItemDTO dto = new StaffRentalInventoryItemDTO();
                    dto.setFacilityInventoryId(rs.getInt("facility_inventory_id"));
                    dto.setInventoryId(rs.getInt("inventory_id"));
                    dto.setName(rs.getString("name"));
                    dto.setBrand(rs.getString("brand"));
                    dto.setDescription(rs.getString("description"));
                    dto.setRentalPrice(rs.getBigDecimal("rental_price"));
                    dto.setAvailableQuantity(Math.max(0, rs.getInt("available_quantity")));
                    dto.setSelectedQuantity(Math.max(0, rs.getInt("selected_quantity")));
                    items.add(dto);
                }
            }
        }

        return items;
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

        String kw = normalizeKeyword(keyword);
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public List<StaffRentalInventoryItemDTO> findSelectedItemsForSlot(
            int facilityId, LocalDate bookingDate, int courtId, int slotId) throws Exception {
        String sql = """
                SELECT
                    i.inventory_id,
                    i.name,
                    i.brand,
                    i.description,
                    i.rental_price,
                    s.quantity,
                    s.availableItem
                FROM InventoryRentalSchedule s
                JOIN Inventory i ON i.inventory_id = s.inventory_id
                WHERE s.facility_id = ?
                  AND s.booking_date = ?
                  AND s.court_id = ?
                  AND s.slot_id = ?
                  AND s.status IN (N'RENTING', N'RENTED')
                ORDER BY i.name ASC
                """;

        List<StaffRentalInventoryItemDTO> items = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, courtId);
            ps.setInt(4, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalInventoryItemDTO dto = new StaffRentalInventoryItemDTO();
                    dto.setInventoryId(rs.getInt("inventory_id"));
                    dto.setName(rs.getString("name"));
                    dto.setBrand(rs.getString("brand"));
                    dto.setDescription(rs.getString("description"));
                    dto.setRentalPrice(rs.getBigDecimal("rental_price"));
                    dto.setSelectedQuantity(rs.getInt("quantity"));
                    dto.setAvailableQuantity(rs.getInt("availableItem"));
                    items.add(dto);
                }
            }
        }
        return items;
    }

    @Override
    public void replaceRentalSchedule(
            Connection conn,
            int facilityId,
            LocalDate bookingDate,
            int courtId,
            int slotId,
            List<InventoryRentalScheduleSaveItemDTO> items) throws Exception {

        deleteExistingSchedule(conn, facilityId, bookingDate, courtId, slotId);
        if (items == null || items.isEmpty()) {
            return;
        }

        String insertSql = """
                INSERT INTO InventoryRentalSchedule (
                    facility_id,
                    booking_date,
                    court_id,
                    slot_id,
                    inventory_id,
                    quantity,
                    availableItem,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, N'RENTING', GETDATE())
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (InventoryRentalScheduleSaveItemDTO item : items) {
                int totalAvailable = loadFacilityInventoryAvailable(conn, facilityId, item.getInventoryId());
                int otherUsed = loadOtherSlotUsedQuantity(conn, facilityId, bookingDate, slotId, courtId, item.getInventoryId());
                int availableItem = totalAvailable - otherUsed - item.getQuantity();
                if (availableItem < 0) {
                    throw new IllegalArgumentException("Số lượng khả dụng không đủ cho đồ thuê đã chọn.");
                }

                ps.setInt(1, facilityId);
                ps.setDate(2, Date.valueOf(bookingDate));
                ps.setInt(3, courtId);
                ps.setInt(4, slotId);
                ps.setInt(5, item.getInventoryId());
                ps.setInt(6, item.getQuantity());
                ps.setInt(7, availableItem);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteExistingSchedule(Connection conn, int facilityId, LocalDate bookingDate, int courtId, int slotId)
            throws Exception {
        String sql = """
                DELETE FROM InventoryRentalSchedule
                WHERE facility_id = ?
                  AND booking_date = ?
                  AND court_id = ?
                  AND slot_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, courtId);
            ps.setInt(4, slotId);
            ps.executeUpdate();
        }
    }

    private int loadFacilityInventoryAvailable(Connection conn, int facilityId, int inventoryId) throws Exception {
        String sql = """
                SELECT available_quantity
                FROM FacilityInventory
                WHERE facility_id = ? AND inventory_id = ?
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
        throw new IllegalArgumentException("Không tìm thấy tồn kho của đồ thuê đã chọn.");
    }

    private int loadOtherSlotUsedQuantity(
            Connection conn, int facilityId, LocalDate bookingDate, int slotId, int courtId, int inventoryId)
            throws Exception {
        String sql = """
                SELECT ISNULL(SUM(quantity), 0)
                FROM InventoryRentalSchedule
                WHERE facility_id = ?
                  AND booking_date = ?
                  AND slot_id = ?
                  AND inventory_id = ?
                  AND court_id <> ?
                  AND status IN (N'RENTING', N'RENTED')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setInt(3, slotId);
            ps.setInt(4, inventoryId);
            ps.setInt(5, courtId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String normalizeKeyword(String keyword) {
        return (keyword == null || keyword.trim().isEmpty()) ? null : "%" + keyword.trim() + "%";
    }
}
