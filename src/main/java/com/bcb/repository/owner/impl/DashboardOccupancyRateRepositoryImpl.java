package com.bcb.repository.owner.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bcb.dto.owner.OwnerOccupancyRateChartDTO;
import com.bcb.repository.owner.DashboardOccupancyRateRepository;
import com.bcb.utils.DBContext;

public class DashboardOccupancyRateRepositoryImpl implements DashboardOccupancyRateRepository{

	private static final String SQL =
		    "SELECT "
		    + "  ROUND(CAST(day_occ  AS FLOAT) / NULLIF(courts * slots * 1,          0) * 100, 2) AS day_pct, "
		    + "  ROUND(CAST(week_occ AS FLOAT) / NULLIF(courts * slots * week_days,  0) * 100, 2) AS week_pct, "
		    + "  ROUND(CAST(mon_occ  AS FLOAT) / NULLIF(courts * slots * month_days, 0) * 100, 2) AS month_pct, "
		    + "  ROUND(CAST(year_occ AS FLOAT) / NULLIF(courts * slots * year_days,  0) * 100, 2) AS year_pct "
		    + "FROM ( "
		    + "  SELECT "
		    + "    (SELECT COUNT(*) FROM Court WHERE is_active = 1) AS courts, "
		    + "    (SELECT COUNT(*) FROM TimeSlot)                  AS slots, "

		    + "    (SELECT COUNT(*) FROM BookingSlot "
		    + "     WHERE slot_status != 'CANCELLED' "
		    + "       AND booking_date = CAST(GETDATE() AS DATE)) AS day_occ, "

		    + "    (SELECT COUNT(*) FROM BookingSlot "
		    + "     WHERE slot_status != 'CANCELLED' "
		    + "       AND booking_date >= CAST(DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE())+5)%7, GETDATE()) AS DATE) "
		    + "       AND booking_date <= CAST(GETDATE() AS DATE)) AS week_occ, "
		    + "    ((DATEPART(WEEKDAY, GETDATE()) + 5) % 7) + 1 AS week_days, "

		    + "    (SELECT COUNT(*) FROM BookingSlot "
		    + "     WHERE slot_status != 'CANCELLED' "
		    + "       AND booking_date >= CAST(DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AS DATE) "
		    + "       AND booking_date <= CAST(GETDATE() AS DATE)) AS mon_occ, "
		    + "    DAY(GETDATE()) AS month_days, "

		    + "    (SELECT COUNT(*) FROM BookingSlot "
		    + "     WHERE slot_status != 'CANCELLED' "
		    + "       AND booking_date >= CAST(DATEFROMPARTS(YEAR(GETDATE()), 1, 1) AS DATE) "
		    + "       AND booking_date <= CAST(GETDATE() AS DATE)) AS year_occ, "
		    + "    DATEDIFF(DAY, DATEFROMPARTS(YEAR(GETDATE()), 1, 1), GETDATE()) + 1 AS year_days "

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
