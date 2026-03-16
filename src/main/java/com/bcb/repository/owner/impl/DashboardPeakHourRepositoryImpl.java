package com.bcb.repository.owner.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bcb.dto.owner.PeakHourSlotDTO;
import com.bcb.repository.owner.DashboardPeakHourRepository;
import com.bcb.utils.DBContext;

public class DashboardPeakHourRepositoryImpl implements DashboardPeakHourRepository {

    // ── Heatmap: đếm booking theo (ngày trong tuần × slot_time) ──
    private static final String SQL_HEATMAP =
        "SELECT " +
        "    (DATEPART(WEEKDAY, b.booking_date) + 5) % 7 + 1 AS day_of_week, " +
        "    CONVERT(VARCHAR(5), ts.start_time, 108)          AS slot_time, " +
        "    COUNT(bs.booking_slot_id)                        AS booking_count, " +
        "    ROUND( " +
        "        COUNT(bs.booking_slot_id) * 100.0 " +
        "        / NULLIF((SELECT COUNT(*) FROM Court WHERE is_active = 1), 0) " +
        "    , 1)                                             AS occupancy_pct " +
        "FROM BookingSlot bs " +
        "JOIN Booking b   ON b.booking_id = bs.booking_id " +
        "JOIN TimeSlot ts ON ts.slot_id   = bs.slot_id " +
        "WHERE b.booking_status   IN ('CONFIRMED','COMPLETED') " +
        "  AND bs.slot_status NOT IN ('CANCELLED','NO_SHOW') " +
        "  AND b.booking_date  >= CAST(DATEADD(MONTH, -1, GETDATE()) AS DATE) " +
        "  AND b.booking_date  <= CAST(GETDATE() AS DATE) " +
        "  AND ts.start_time   >= '05:00:00' " +
        "  AND ts.start_time    < '22:00:00' " +
        "GROUP BY " +
        "    DATEPART(WEEKDAY, b.booking_date), " +
        "    ts.slot_id, ts.start_time " +
        "ORDER BY day_of_week, ts.start_time";

    // ── Phân loại PEAK / LOW / NORMAL / NO_DATA theo Mean ± StdDev ──
    private static final String SQL_CLASSIFIED =
        "WITH BookingCounts AS ( " +
        "    SELECT " +
        "        ts.slot_id, " +
        "        CONVERT(VARCHAR(5), ts.start_time, 108) AS slot_time, " +
        "        COUNT(bs.booking_slot_id)               AS total_bookings " +
        "    FROM TimeSlot ts " +
        "    LEFT JOIN BookingSlot bs " +
        "           ON bs.slot_id = ts.slot_id " +
        "          AND bs.slot_status NOT IN ('CANCELLED','NO_SHOW') " +
        "    LEFT JOIN Booking b " +
        "           ON b.booking_id     = bs.booking_id " +
        "          AND b.booking_status IN ('CONFIRMED','COMPLETED') " +
        "          AND b.booking_date  >= CAST(DATEADD(MONTH,-1,GETDATE()) AS DATE) " +
        "          AND b.booking_date  <= CAST(GETDATE() AS DATE) " +
        "    WHERE ts.start_time >= '05:00:00' " +
        "      AND ts.start_time  < '22:00:00' " +
        "    GROUP BY ts.slot_id, ts.start_time " +
        "), " +
        "Constants AS ( " +
        "    SELECT " +
        "        (SELECT COUNT(*) FROM Court WHERE is_active = 1) AS active_courts, " +
        "        DATEDIFF(DAY, " +
        "            CAST(DATEADD(MONTH,-1,GETDATE()) AS DATE), " +
        "            CAST(GETDATE() AS DATE) " +
        "        ) + 1 AS period_days " +
        "), " +
        "OccupancyPerSlot AS ( " +
        "    SELECT " +
        "        bc.slot_id, " +
        "        bc.slot_time, " +
        "        bc.total_bookings, " +
        "        ROUND( " +
        "            bc.total_bookings * 100.0 " +
        "            / NULLIF(c.active_courts * c.period_days, 0) " +
        "        , 2) AS occupancy_pct " +
        "    FROM BookingCounts bc " +
        "    CROSS JOIN Constants c " +
        "), " +
        // Chỉ tính Mean + StdDev trên slot có booking thực
        "Stats AS ( " +
        "    SELECT " +
        "        AVG(occupancy_pct)   AS mean_occ, " +
        "        STDEV(occupancy_pct) AS std_occ " +
        "    FROM OccupancyPerSlot " +
        "    WHERE total_bookings > 0 " +
        "), " +
        "SlotClassified AS ( " +
        "    SELECT " +
        "        o.slot_id, " +
        "        o.slot_time, " +
        "        o.total_bookings, " +
        "        o.occupancy_pct, " +
        "        CASE " +
        "            WHEN o.total_bookings = 0 " +
        "                THEN 'NO_DATA' " +
        "            WHEN o.occupancy_pct >= s.mean_occ + s.std_occ " +
        "                THEN 'PEAK' " +
        "            WHEN o.occupancy_pct <= CASE " +
        "                                        WHEN s.mean_occ - s.std_occ < 0 THEN 0 " +
        "                                        ELSE s.mean_occ - s.std_occ " +
        "                                    END " +
        "                THEN 'LOW' " +
        "            ELSE 'NORMAL' " +
        "        END AS slot_type " +
        "    FROM OccupancyPerSlot o " +
        "    CROSS JOIN Stats s " +
        ") " +
        "SELECT slot_time, total_bookings, occupancy_pct, slot_type " +
        "FROM   SlotClassified " +
        "ORDER  BY slot_time";

    // ── Tất cả slot_time trong khung 05:00–22:00 ──
    private static final String SQL_ALL_SLOTS =
        "SELECT CONVERT(VARCHAR(5), start_time, 108) AS slot_time " +
        "FROM TimeSlot " +
        "WHERE start_time >= '05:00:00' " +
        "  AND start_time  < '22:00:00' " +
        "ORDER BY start_time";

    @Override
    public List<PeakHourSlotDTO> getHeatmapData() {
        List<PeakHourSlotDTO> result = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_HEATMAP);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new PeakHourSlotDTO(
                    rs.getInt("day_of_week"),
                    rs.getString("slot_time"),
                    rs.getInt("booking_count"),
                    rs.getBigDecimal("occupancy_pct"),
                    null   // slot_type không cần cho heatmap
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi truy vấn heatmap", e);
        }
        return result;
    }

    @Override
    public List<PeakHourSlotDTO> getClassifiedSlots() {
        List<PeakHourSlotDTO> result = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CLASSIFIED);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new PeakHourSlotDTO(
                    0,   // dayOfWeek không dùng ở đây
                    rs.getString("slot_time"),
                    rs.getInt("total_bookings"),
                    rs.getBigDecimal("occupancy_pct"),
                    rs.getString("slot_type")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi truy vấn classified slots", e);
        }
        return result;
    }

    @Override
    public List<String> getAllSlotTimes() {
        List<String> result = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ALL_SLOTS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("slot_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi truy vấn TimeSlot", e);
        }
        return result;
    }
}