package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffRentalStatusCourtDTO;
import com.bcb.dto.staff.StaffRentalStatusRawRowDTO;
import com.bcb.repository.staff.StaffRentalStatusRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusRepositoryImpl implements StaffRentalStatusRepository {

    @Override
    public List<StaffRentalStatusCourtDTO> findCourtsByFacility(int facilityId) throws Exception {
        String sql = """
                SELECT c.court_id, c.court_name
                FROM Court c
                WHERE c.facility_id = ?
                  AND c.is_active = 1
                ORDER BY c.court_name ASC
                """;

        List<StaffRentalStatusCourtDTO> courts = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
    public List<StaffRentalStatusRawRowDTO> findRentalStatusRows(int facilityId) throws Exception {
        String sql = """
                SELECT
                    rrl.rental_id,
                    c.court_id,
                    c.court_name,
                    COALESCE(a.full_name, g.guest_name) AS customer_name,
                    i.name AS inventory_name,
                    rrl.quantity,
                    b.booking_date,
                    ts.slot_id,
                    ts.start_time,
                    ts.end_time,
                    rrl.returned_at
                FROM RacketRentalLog rrl
                JOIN BookingSlot bs ON bs.booking_slot_id = rrl.booking_slot_id
                JOIN Booking b ON b.booking_id = bs.booking_id
                JOIN Court c ON c.court_id = bs.court_id
                JOIN TimeSlot ts ON ts.slot_id = bs.slot_id
                JOIN FacilityInventory fi ON fi.facility_inventory_id = rrl.facility_inventory_id
                JOIN Inventory i ON i.inventory_id = fi.inventory_id
                LEFT JOIN Account a ON a.account_id = b.account_id
                LEFT JOIN Guest g ON g.guest_id = b.guest_id
                WHERE c.facility_id = ?
                ORDER BY c.court_name ASC,
                         b.booking_date ASC,
                         ts.start_time ASC,
                         customer_name ASC,
                         i.name ASC,
                         rrl.quantity ASC,
                         rrl.rental_id ASC
                """;

        List<StaffRentalStatusRawRowDTO> rows = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffRentalStatusRawRowDTO row = new StaffRentalStatusRawRowDTO();
                    row.setRentalId(rs.getInt("rental_id"));
                    row.setCourtId(rs.getInt("court_id"));
                    row.setCourtName(rs.getString("court_name"));
                    row.setCustomerName(rs.getString("customer_name"));
                    row.setInventoryName(rs.getString("inventory_name"));
                    row.setQuantity(rs.getInt("quantity"));
                    row.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    row.setSlotId(rs.getInt("slot_id"));
                    row.setStartTime(rs.getTime("start_time").toLocalTime());
                    row.setEndTime(rs.getTime("end_time").toLocalTime());
                    if (rs.getTimestamp("returned_at") != null) {
                        row.setReturnedAt(rs.getTimestamp("returned_at").toLocalDateTime());
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public int updateReturnedStatus(int facilityId, List<Integer> rentalIds, boolean returned) throws Exception {
        if (rentalIds == null || rentalIds.isEmpty()) {
            return 0;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < rentalIds.size(); i++) {
            if (i > 0) placeholders.append(',');
            placeholders.append('?');
        }

        String updateExpr = returned ? "COALESCE(rrl.returned_at, GETDATE())" : "NULL";
        String sql = """
                UPDATE rrl
                SET returned_at = %s
                FROM RacketRentalLog rrl
                JOIN BookingSlot bs ON bs.booking_slot_id = rrl.booking_slot_id
                JOIN Court c ON c.court_id = bs.court_id
                WHERE c.facility_id = ?
                  AND rrl.rental_id IN (%s)
                """.formatted(updateExpr, placeholders);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, facilityId);
            for (Integer rentalId : rentalIds) {
                ps.setInt(idx++, rentalId);
            }
            return ps.executeUpdate();
        }
    }
}
