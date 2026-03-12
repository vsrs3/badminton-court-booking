package com.bcb.service.impl;

import com.bcb.dto.staff.StaffRentalInventoryItemDTO;
import com.bcb.repository.impl.StaffRentalRepositoryImpl;
import com.bcb.repository.staff.StaffRentalRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class StaffRentalServiceImpl implements com.bcb.service.staff.StaffRentalService {

    private final StaffRentalRepository repo = new StaffRentalRepositoryImpl() {
        @Override
        public void insertRentalLogAndDecreaseStock(Connection conn, int bookingId, int facilityId, int staffId) throws Exception {

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
        }
    };

    @Override
    public String getInventoryJson(int facilityId, String keyword, int page, int pageSize) throws Exception {
        List<StaffRentalInventoryItemDTO> items = repo.findRentalItems(facilityId, keyword, page, pageSize);
        int total = repo.countRentalItems(facilityId, keyword);
        int totalPages = (int) Math.ceil(total * 1.0 / pageSize);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"data\":{");
        sb.append("\"page\":").append(page).append(",");
        sb.append("\"pageSize\":").append(pageSize).append(",");
        sb.append("\"total\":").append(total).append(",");
        sb.append("\"totalPages\":").append(totalPages).append(",");
        sb.append("\"items\":[");

        for (int i = 0; i < items.size(); i++) {
            StaffRentalInventoryItemDTO it = items.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"facilityInventoryId\":").append(it.getFacilityInventoryId()).append(",");
            sb.append("\"inventoryId\":").append(it.getInventoryId()).append(",");
            sb.append("\"name\":\"").append(escape(it.getName())).append("\",");
            sb.append("\"brand\":\"").append(escape(it.getBrand())).append("\",");
            sb.append("\"description\":\"").append(escape(it.getDescription())).append("\",");
            sb.append("\"rentalPrice\":").append(it.getRentalPrice()).append(",");
            sb.append("\"availableQuantity\":").append(it.getAvailableQuantity());
            sb.append("}");
        }

        sb.append("]}}");
        return sb.toString();
    }

    @Override
    public String saveRental(String body, int facilityId, int staffId) throws Exception {
        int inventoryId = extractInt(body, "inventoryId");
        int quantity = extractInt(body, "quantity");
        BigDecimal unitPrice = extractDecimal(body, "unitPrice");
        int[] bookingSlotIds = extractIntArray(body, "bookingSlotIds");

        if (inventoryId <= 0 || quantity <= 0 || bookingSlotIds.length == 0 || unitPrice == null) {
            return "{\"success\":false,\"message\":\"Dữ liệu thuê không hợp lệ\"}";
        }

        for (int bookingSlotId : bookingSlotIds) {
            if (!repo.existsRacketRental(bookingSlotId, inventoryId)) {
                repo.insertRacketRental(bookingSlotId, inventoryId, quantity, unitPrice, staffId);
            } else {
                repo.updateRacketRental(bookingSlotId, inventoryId, quantity);
            }
        }

        return "{\"success\":true,\"message\":\"Thuê đồ thành công\"}";
    }

    @Override
    public String updateRental(String body, int facilityId, int staffId) throws Exception {
        return saveRental(body, facilityId, staffId);
    }

    @Override
    public String deleteRental(String body, int facilityId, int staffId) throws Exception {
        int inventoryId = extractInt(body, "inventoryId");
        int[] bookingSlotIds = extractIntArray(body, "bookingSlotIds");

        if (inventoryId <= 0 || bookingSlotIds.length == 0) {
            return "{\"success\":false,\"message\":\"Thiếu dữ liệu xóa thuê đồ\"}";
        }

        for (int bookingSlotId : bookingSlotIds) {
            repo.deleteRacketRental(bookingSlotId, inventoryId);
        }

        return "{\"success\":true,\"message\":\"Đã bỏ thuê đồ\"}";
    }

    @Override
    public String getRentalSummaryJson(int bookingId, int facilityId) throws Exception {
        BigDecimal total = repo.getBookingRentalTotal(bookingId);

        return "{\"success\":true,\"data\":{\"bookingId\":" + bookingId +
                ",\"rentalTotal\":" + total + "}}";
    }

    private String escape(String val) {
        if (val == null) return "";
        return val.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int extractInt(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return -1;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return -1;

            StringBuilder num = new StringBuilder();
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c)) num.append(c);
                else if (num.length() > 0) break;
            }
            return num.length() > 0 ? Integer.parseInt(num.toString()) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal extractDecimal(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;

            StringBuilder num = new StringBuilder();
            boolean dot = false;
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c)) num.append(c);
                else if (c == '.' && !dot) {
                    dot = true;
                    num.append(c);
                } else if (num.length() > 0) break;
            }
            return num.length() > 0 ? new BigDecimal(num.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int[] extractIntArray(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return new int[0];
            int open = json.indexOf("[", idx);
            int close = json.indexOf("]", open);
            if (open < 0 || close < 0) return new int[0];

            String arr = json.substring(open + 1, close).trim();
            if (arr.isEmpty()) return new int[0];

            String[] parts = arr.split(",");
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i].trim());
            }
            return result;
        } catch (Exception e) {
            return new int[0];
        }
    }
}