package com.bcb.repository.owner.impl;

import com.bcb.dto.owner.OwnerRentalDetailRowDTO;
import com.bcb.dto.owner.OwnerRentalFacilityOptionDTO;
import com.bcb.dto.owner.OwnerRentalInactiveItemDTO;
import com.bcb.dto.owner.OwnerRentalPointDTO;
import com.bcb.dto.owner.OwnerRentalTopItemDTO;
import com.bcb.repository.owner.OwnerRentalReportRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class OwnerRentalReportRepositoryImpl implements OwnerRentalReportRepository {

    private static final String RENTAL_FACT_CTE = """
            WITH RentalFact AS (
                SELECT
                    c.facility_id,
                    COALESCE(bs.booking_date, b.booking_date) AS booking_date,
                    CONVERT(VARCHAR(5), ts.start_time, 108) AS slot_time,
                    rr.inventory_id,
                    i.name AS inventory_name,
                    i.brand AS inventory_brand,
                    ISNULL(fi.total_quantity, 0) AS total_quantity,
                    CAST(COALESCE(rrl.quantity, irs.quantity, rr.quantity, 0) AS INT) AS rented_quantity,
                    rr.unit_price AS unit_price,
                    CAST(COALESCE(rrl.quantity, irs.quantity, rr.quantity, 0) * rr.unit_price AS DECIMAL(18,2)) AS line_revenue
                FROM RacketRental rr
                JOIN BookingSlot bs
                    ON bs.booking_slot_id = rr.booking_slot_id
                JOIN Booking b
                    ON b.booking_id = bs.booking_id
                JOIN Court c
                    ON c.court_id = bs.court_id
                JOIN TimeSlot ts
                    ON ts.slot_id = bs.slot_id
                JOIN Inventory i
                    ON i.inventory_id = rr.inventory_id
                LEFT JOIN FacilityInventory fi
                    ON fi.facility_id = c.facility_id
                   AND fi.inventory_id = rr.inventory_id
                LEFT JOIN InventoryRentalSchedule irs
                    ON irs.facility_id = c.facility_id
                   AND irs.booking_date = COALESCE(bs.booking_date, b.booking_date)
                   AND irs.court_id = bs.court_id
                   AND irs.slot_id = bs.slot_id
                   AND irs.inventory_id = rr.inventory_id
                LEFT JOIN RacketRentalLog rrl
                    ON rrl.booking_slot_id = rr.booking_slot_id
                   AND fi.facility_inventory_id IS NOT NULL
                   AND rrl.facility_inventory_id = fi.facility_inventory_id
                WHERE b.booking_status IN ('CONFIRMED', 'COMPLETED')
                  AND COALESCE(bs.booking_date, b.booking_date) IS NOT NULL
            )
            """;

    @Override
    public List<OwnerRentalFacilityOptionDTO> findFacilityOptions(String keyword) throws Exception {
        String sql = """
                SELECT
                    facility_id,
                    name,
                    address,
                    ward,
                    district,
                    province
                FROM Facility
                WHERE is_active = 1
                  AND (
                        ? IS NULL
                        OR name COLLATE Latin1_General_100_CI_AI LIKE ?
                        OR address COLLATE Latin1_General_100_CI_AI LIKE ?
                        OR ward COLLATE Latin1_General_100_CI_AI LIKE ?
                        OR district COLLATE Latin1_General_100_CI_AI LIKE ?
                        OR province COLLATE Latin1_General_100_CI_AI LIKE ?
                  )
                ORDER BY facility_id ASC
                """;

        List<OwnerRentalFacilityOptionDTO> facilities = new ArrayList<>();
        String normalizedKeyword = normalizeKeyword(keyword);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedKeyword);
            ps.setString(2, normalizedKeyword);
            ps.setString(3, normalizedKeyword);
            ps.setString(4, normalizedKeyword);
            ps.setString(5, normalizedKeyword);
            ps.setString(6, normalizedKeyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalFacilityOptionDTO dto = new OwnerRentalFacilityOptionDTO();
                    dto.setFacilityId(rs.getInt("facility_id"));
                    dto.setName(rs.getString("name"));
                    dto.setAddress(buildFacilityAddress(
                            rs.getString("address"),
                            rs.getString("ward"),
                            rs.getString("district"),
                            rs.getString("province")
                    ));
                    facilities.add(dto);
                }
            }
        }

        return facilities;
    }

    @Override
    public String findFacilityName(int facilityId) throws Exception {
        String sql = """
                SELECT name
                FROM Facility
                WHERE facility_id = ?
                  AND is_active = 1
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }

        return null;
    }

    @Override
    public Integer findLatestRentalYear(int facilityId) throws Exception {
        String sql = """
                SELECT MAX(YEAR(COALESCE(bs.booking_date, b.booking_date)))
                FROM RacketRental rr
                JOIN BookingSlot bs
                    ON bs.booking_slot_id = rr.booking_slot_id
                JOIN Booking b
                    ON b.booking_id = bs.booking_id
                JOIN Court c
                    ON c.court_id = bs.court_id
                WHERE c.facility_id = ?
                  AND b.booking_status IN ('CONFIRMED', 'COMPLETED')
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int year = rs.getInt(1);
                    return rs.wasNull() ? null : year;
                }
            }
        }

        return null;
    }

    @Override
    public List<OwnerRentalPointDTO> findMonthlyRevenue(int facilityId, int year) throws Exception {
        String sql = RENTAL_FACT_CTE + """
                SELECT
                    MONTH(booking_date) AS point_index,
                    SUM(line_revenue) AS revenue
                FROM RentalFact
                WHERE facility_id = ?
                  AND YEAR(booking_date) = ?
                GROUP BY MONTH(booking_date)
                ORDER BY point_index
                """;

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalPointDTO point = new OwnerRentalPointDTO();
                    point.setIndex(rs.getInt("point_index"));
                    point.setRevenue(rs.getBigDecimal("revenue"));
                    points.add(point);
                }
            }
        }
        return points;
    }

    @Override
    public List<OwnerRentalPointDTO> findDailyRevenue(int facilityId, int year, int month) throws Exception {
        String sql = RENTAL_FACT_CTE + """
                SELECT
                    DAY(booking_date) AS point_index,
                    SUM(line_revenue) AS revenue
                FROM RentalFact
                WHERE facility_id = ?
                  AND YEAR(booking_date) = ?
                  AND MONTH(booking_date) = ?
                GROUP BY DAY(booking_date)
                ORDER BY point_index
                """;

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalPointDTO point = new OwnerRentalPointDTO();
                    point.setIndex(rs.getInt("point_index"));
                    point.setRevenue(rs.getBigDecimal("revenue"));
                    points.add(point);
                }
            }
        }
        return points;
    }

    @Override
    public List<String> findSlotLabels(int facilityId) throws Exception {
        String sql = """
                SELECT CONVERT(VARCHAR(5), ts.start_time, 108) AS slot_label
                FROM Facility f
                JOIN TimeSlot ts
                    ON ts.start_time >= f.open_time
                   AND ts.end_time <= f.close_time
                WHERE f.facility_id = ?
                  AND f.is_active = 1
                ORDER BY ts.start_time ASC
                """;

        List<String> labels = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getString("slot_label"));
                }
            }
        }
        return labels;
    }

    @Override
    public List<OwnerRentalPointDTO> findHourlyRevenue(int facilityId, LocalDate bookingDate) throws Exception {
        String sql = RENTAL_FACT_CTE + """
                SELECT
                    slot_time,
                    SUM(line_revenue) AS revenue
                FROM RentalFact
                WHERE facility_id = ?
                  AND booking_date = ?
                GROUP BY slot_time
                ORDER BY slot_time
                """;

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setDate(2, Date.valueOf(bookingDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalPointDTO point = new OwnerRentalPointDTO();
                    point.setKey(rs.getString("slot_time"));
                    point.setRevenue(rs.getBigDecimal("revenue"));
                    points.add(point);
                }
            }
        }
        return points;
    }

    @Override
    public List<OwnerRentalTopItemDTO> findTopItems(int facilityId, int year, int month) throws Exception {
        String sql = RENTAL_FACT_CTE + """
                SELECT TOP 10
                    inventory_name,
                    SUM(rented_quantity) AS rented_quantity,
                    SUM(line_revenue) AS total_revenue
                FROM RentalFact
                WHERE facility_id = ?
                  AND YEAR(booking_date) = ?
                  AND MONTH(booking_date) = ?
                GROUP BY inventory_name
                ORDER BY SUM(rented_quantity) DESC, SUM(line_revenue) DESC, inventory_name ASC
                """;

        List<OwnerRentalTopItemDTO> items = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    OwnerRentalTopItemDTO item = new OwnerRentalTopItemDTO();
                    item.setRank(rank++);
                    item.setName(rs.getString("inventory_name"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public List<OwnerRentalDetailRowDTO> findDetailRows(int facilityId, int year, int month, Integer day, String slotTime)
            throws Exception {
        StringBuilder sql = new StringBuilder(RENTAL_FACT_CTE).append("""
                SELECT
                    inventory_id,
                    inventory_name,
                    MAX(total_quantity) AS total_quantity,
                    SUM(rented_quantity) AS rented_quantity,
                    MAX(unit_price) AS unit_price,
                    SUM(line_revenue) AS total_revenue
                FROM RentalFact
                WHERE facility_id = ?
                  AND YEAR(booking_date) = ?
                  AND MONTH(booking_date) = ?
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(facilityId);
        parameters.add(year);
        parameters.add(month);

        if (day != null) {
            sql.append(" AND DAY(booking_date) = ? ");
            parameters.add(day);
        }

        if (slotTime != null && !slotTime.isBlank()) {
            sql.append(" AND slot_time = ? ");
            parameters.add(slotTime);
        }

        sql.append("""
                GROUP BY inventory_id, inventory_name
                ORDER BY SUM(rented_quantity) DESC, SUM(line_revenue) DESC, inventory_name ASC
                """);

        List<OwnerRentalDetailRowDTO> rows = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalDetailRowDTO row = new OwnerRentalDetailRowDTO();
                    row.setInventoryId(rs.getInt("inventory_id"));
                    row.setInventoryName(rs.getString("inventory_name"));
                    row.setTotalQuantity(rs.getInt("total_quantity"));
                    row.setRentedQuantity(rs.getInt("rented_quantity"));
                    row.setUnitPrice(rs.getBigDecimal("unit_price"));
                    row.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public List<OwnerRentalInactiveItemDTO> findInactiveItems(int facilityId, int year, int month, int limit)
            throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            return loadInactiveItems(conn, facilityId, year, month, limit);
        }
    }

    @Override
    public int deactivateInactiveItems(int facilityId, int year, int month, int limit) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<OwnerRentalInactiveItemDTO> candidates = loadInactiveItems(conn, facilityId, year, month, limit);
                if (candidates.isEmpty()) {
                    conn.commit();
                    return 0;
                }

                StringBuilder sql = new StringBuilder("UPDATE Inventory SET is_active = 0 WHERE inventory_id IN (");
                for (int index = 0; index < candidates.size(); index++) {
                    if (index > 0) {
                        sql.append(',');
                    }
                    sql.append('?');
                }
                sql.append(')');

                int updated;
                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    for (int index = 0; index < candidates.size(); index++) {
                        ps.setInt(index + 1, candidates.get(index).getInventoryId());
                    }
                    updated = ps.executeUpdate();
                }

                conn.commit();
                return updated;
            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private List<OwnerRentalInactiveItemDTO> loadInactiveItems(
            Connection conn,
            int facilityId,
            int year,
            int month,
            int limit
    ) throws Exception {
        String sql = RENTAL_FACT_CTE + """
                SELECT
                    i.inventory_id,
                    i.name AS inventory_name,
                    i.brand,
                    fi.total_quantity
                FROM FacilityInventory fi
                JOIN Inventory i
                    ON i.inventory_id = fi.inventory_id
                WHERE fi.facility_id = ?
                  AND i.is_active = 1
                  AND NOT EXISTS (
                        SELECT 1
                        FROM RentalFact rf
                        WHERE rf.facility_id = fi.facility_id
                          AND rf.inventory_id = fi.inventory_id
                          AND YEAR(rf.booking_date) = ?
                          AND MONTH(rf.booking_date) = ?
                  )
                ORDER BY fi.total_quantity DESC, i.name ASC
                OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
                """;

        List<OwnerRentalInactiveItemDTO> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OwnerRentalInactiveItemDTO item = new OwnerRentalInactiveItemDTO();
                    item.setInventoryId(rs.getInt("inventory_id"));
                    item.setInventoryName(rs.getString("inventory_name"));
                    item.setBrand(rs.getString("brand"));
                    item.setTotalQuantity(rs.getInt("total_quantity"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return "%" + keyword.trim() + "%";
    }

    private void bindParameters(PreparedStatement ps, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            int parameterIndex = index + 1;

            if (value instanceof Integer integerValue) {
                ps.setInt(parameterIndex, integerValue);
                continue;
            }

            if (value instanceof String stringValue) {
                ps.setString(parameterIndex, stringValue);
                continue;
            }

            throw new SQLException("Unsupported parameter type: " + value);
        }
    }

    private String buildFacilityAddress(String address, String ward, String district, String province) {
        StringJoiner joiner = new StringJoiner(", ");
        addAddressPart(joiner, address);
        addAddressPart(joiner, ward);
        addAddressPart(joiner, district);
        addAddressPart(joiner, province);
        return joiner.toString();
    }

    private void addAddressPart(StringJoiner joiner, String value) {
        if (value != null && !value.isBlank()) {
            joiner.add(value.trim());
        }
    }
}
