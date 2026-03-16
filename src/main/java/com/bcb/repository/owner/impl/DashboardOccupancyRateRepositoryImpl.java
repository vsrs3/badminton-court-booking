package com.bcb.repository.owner.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bcb.dto.owner.OwnerOccupancyRateChartDTO;
import com.bcb.repository.owner.DashboardOccupancyRateRepository;
import com.bcb.utils.DBContext;

public class DashboardOccupancyRateRepositoryImpl implements DashboardOccupancyRateRepository{

	// ── Logic tính:
    // Occupied  = số BookingSlot active (không CANCELLED/NO_SHOW) trong kỳ
    // Total     = số sân active × số TimeSlot × số ngày trong kỳ
    // Rate      = Occupied / Total * 100

    private static final String SQL =
				        "SELECT "
				        + "    ROUND(CAST(day_occ  AS FLOAT) / NULLIF(courts * slots * 1,          0) * 100, 2) AS day_pct, "
				        + "    ROUND(CAST(week_occ AS FLOAT) / NULLIF(courts * slots * week_days,  0) * 100, 2) AS week_pct, "
				        + "    ROUND(CAST(mon_occ  AS FLOAT) / NULLIF(courts * slots * month_days, 0) * 100, 2) AS month_pct, "
				        + "    ROUND(CAST(year_occ AS FLOAT) / NULLIF(courts * slots * year_days,  0) * 100, 2) AS year_pct "
				        + "FROM ( "
				        + "    SELECT "
				
				        // Tổng sân active
				        + "        (SELECT COUNT(*) FROM Court WHERE is_active = 1) AS courts, "
				
				        // Tổng slot / ngày
				        + "        (SELECT COUNT(*) FROM TimeSlot)                  AS slots, "
				
				        // ── Day
				        + "        (SELECT COUNT(*) FROM CourtSlotBooking csb "
				        + "         JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id "
				        + "         WHERE bs.slot_status NOT IN ('CANCELLED','NO_SHOW') "
				        + "           AND csb.booking_date = CAST(GETDATE() AS DATE)) AS day_occ, "
				
				        // ── Week (Thứ 2 → hôm nay)
				        + "        (SELECT COUNT(*) FROM CourtSlotBooking csb "
				        + "         JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id "
				        + "         WHERE bs.slot_status NOT IN ('CANCELLED','NO_SHOW') "
				        + "           AND csb.booking_date >= CAST(DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE())+5)%7, GETDATE()) AS DATE) "
				        + "           AND csb.booking_date <= CAST(GETDATE() AS DATE)) AS week_occ, "
				        + "        ((DATEPART(WEEKDAY, GETDATE()) + 5) % 7) + 1 AS week_days, "
				
				        // ── Month (01 → hôm nay)
				        + "        (SELECT COUNT(*) FROM CourtSlotBooking csb "
				        + "         JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id "
				        + "         WHERE bs.slot_status NOT IN ('CANCELLED','NO_SHOW') "
				        + "           AND csb.booking_date >= CAST(DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AS DATE) "
				        + "           AND csb.booking_date <= CAST(GETDATE() AS DATE)) AS mon_occ, "
				        + "        DAY(GETDATE()) AS month_days, "
				
				        // ── Year (01/01 → hôm nay)
				        + "        (SELECT COUNT(*) FROM CourtSlotBooking csb "
				        + "         JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id "
				        + "         WHERE bs.slot_status NOT IN ('CANCELLED','NO_SHOW') "
				        + "           AND csb.booking_date >= CAST(DATEFROMPARTS(YEAR(GETDATE()), 1, 1) AS DATE) "
				        + "           AND csb.booking_date <= CAST(GETDATE() AS DATE)) AS year_occ, "
				        + "        DATEDIFF(DAY, DATEFROMPARTS(YEAR(GETDATE()), 1, 1), GETDATE()) + 1 AS year_days "
				
				        + ") AS t";
	
	@Override
	public OwnerOccupancyRateChartDTO getOccupancyRate() {
		
		try (Connection conn = DBContext.getConnection();
	             PreparedStatement ps = conn.prepareStatement(SQL);
	             ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {
	                return new OwnerOccupancyRateChartDTO(
	                    rs.getBigDecimal("day_pct"),
	                    rs.getBigDecimal("week_pct"),
	                    rs.getBigDecimal("month_pct"),
	                    rs.getBigDecimal("year_pct")
	                );
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new RuntimeException("Lỗi khi truy vấn Occupancy Rate", e);
	        }
	        return new OwnerOccupancyRateChartDTO();
	}

}
