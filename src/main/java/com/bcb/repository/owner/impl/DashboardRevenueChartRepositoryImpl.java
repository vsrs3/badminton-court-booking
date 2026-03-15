package com.bcb.repository.owner.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bcb.dto.owner.OwnerRevenueChartDTO;
import com.bcb.repository.owner.DashboardRevenueChartRepository;
import com.bcb.utils.DBContext;

public class DashboardRevenueChartRepositoryImpl implements DashboardRevenueChartRepository {

    // query: execute sql to DTO
    private OwnerRevenueChartDTO query(String sql, String labelCol, String dataCol) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal>   data   = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labels.add(rs.getString(labelCol));
                data.add(rs.getBigDecimal(dataCol));
            }
            return new OwnerRevenueChartDTO(labels, data);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn chart: " + sql, e);
        }
    }

    // Daily Revenue - This Week
    private static final String SQL_DAILY_THIS_WEEK = "SELECT "
									        + "    LEFT(DATENAME(WEEKDAY, b.booking_date), 3) AS day_label, "
									        + "    ISNULL(SUM(i.paid_amount), 0)              AS revenue "
									        
										        + "FROM Invoice i "
											        + "JOIN Booking b ON i.booking_id = b.booking_id "
											        + "WHERE b.booking_status = 'COMPLETED' "
											        + "  AND b.booking_date >= CAST(DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE())+5)%7,   GETDATE()) AS DATE) "
											        + "  AND b.booking_date <= CAST(GETDATE() AS DATE) "
									        
											+ "GROUP BY b.booking_date, DATENAME(WEEKDAY, b.booking_date) "
									        + "ORDER BY b.booking_date";

    // Daily Revenue — Previous Week 
    private static final String SQL_DAILY_PREV_WEEK = "SELECT "
									        + "LEFT(DATENAME(WEEKDAY, b.booking_date), 3) 	AS day_label, "
									        + "ISNULL(SUM(i.paid_amount), 0)              	AS revenue "
									        
									        	+ "FROM Invoice i "
											        + "JOIN Booking b ON i.booking_id = b.booking_id "
											        + "WHERE b.booking_status = 'COMPLETED' "
											        + "  AND b.booking_date >= CAST(DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE())+5)%7-7, GETDATE()) AS DATE) "
											        + "  AND b.booking_date <= CAST(DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE())+5)%7-1, GETDATE()) AS DATE) "
									        
									        + "GROUP BY b.booking_date, DATENAME(WEEKDAY, b.booking_date) "
									        + "ORDER BY b.booking_date";

    // Monthly Revenue — This Year
    private static final String SQL_MONTHLY_THIS_YEAR = "SELECT "
									        + "LEFT(DATENAME(MONTH, b.booking_date), 3) 	AS month_label, "
									        + "ISNULL(SUM(i.paid_amount), 0)            	AS revenue "
									        
									        	+ "FROM Invoice i "
											        + "JOIN Booking b ON i.booking_id = b.booking_id "
											        + "WHERE b.booking_status = 'COMPLETED' "
											        + "  AND YEAR(b.booking_date) = YEAR(GETDATE()) "
											        
									        + "GROUP BY MONTH(b.booking_date), DATENAME(MONTH, b.booking_date) "
									        + "ORDER BY MONTH(b.booking_date)";

    // Monthly Revenue — Previous Year
    private static final String SQL_MONTHLY_PREV_YEAR = "SELECT "
									        + "LEFT(DATENAME(MONTH, b.booking_date), 3) 	AS month_label, "
									        + "ISNULL(SUM(i.paid_amount), 0)             	AS revenue "
									        	+ "FROM Invoice i "
											        + "JOIN Booking b ON i.booking_id = b.booking_id "
											        + "WHERE b.booking_status = 'COMPLETED' "
											        + "  AND YEAR(b.booking_date) = YEAR(GETDATE()) - 1 "
											        
									        + "GROUP BY MONTH(b.booking_date), DATENAME(MONTH, b.booking_date) "
									        + "ORDER BY MONTH(b.booking_date)";

    // Revenue Trend — Monthly
    /** private static final String SQL_TREND_MONTHLY = "SELECT "
						    			    + "LEFT(DATENAME(MONTH, b.booking_date), 3) AS month_label, "
						    			    + "ISNULL(SUM(i.paid_amount), 0)            AS revenue "
						    			    + "FROM Invoice i "
						    			    + "JOIN Booking b ON i.booking_id = b.booking_id "
						    			    + "WHERE b.booking_status = 'COMPLETED' "
						    			    + "  AND YEAR(b.booking_date) = YEAR(GETDATE()) " // ← chỉ năm hiện tại
						    			    + "GROUP BY MONTH(b.booking_date), DATENAME(MONTH, b.booking_date) "
						    			    + "ORDER BY MONTH(b.booking_date)";
						    			    **/

    // 5 năm trước — currentYear-4 → currentYear
    private static final String SQL_TREND_YEARLY_PAST = "SELECT "
									        + "CAST(YEAR(b.booking_date) AS NVARCHAR) AS year_label, "
									        + "ISNULL(SUM(i.paid_amount), 0)          AS revenue "
									        + "FROM Invoice i "
									        + "JOIN Booking b ON i.booking_id = b.booking_id "
									        + "WHERE b.booking_status = 'COMPLETED' "
									        + "  AND YEAR(b.booking_date) >= YEAR(GETDATE()) - 4 "
									        + "  AND YEAR(b.booking_date) <= YEAR(GETDATE()) "
									        + "GROUP BY YEAR(b.booking_date) "
									        + "ORDER BY YEAR(b.booking_date)";

    // 5 năm tới — currentYear → currentYear+4
    private static final String SQL_TREND_YEARLY_FUTURE = "SELECT "
									        + "CAST(YEAR(b.booking_date) AS NVARCHAR) AS year_label, "
									        + "ISNULL(SUM(i.paid_amount), 0)          AS revenue "
									        + "FROM Invoice i "
									        + "JOIN Booking b ON i.booking_id = b.booking_id "
									        + "WHERE b.booking_status = 'COMPLETED' "
									        + "  AND YEAR(b.booking_date) >= YEAR(GETDATE()) "
									        + "  AND YEAR(b.booking_date) <= YEAR(GETDATE()) + 4 "
									        + "GROUP BY YEAR(b.booking_date) "
									        + "ORDER BY YEAR(b.booking_date)";

    // Override methods
    @Override
    public OwnerRevenueChartDTO getDailyRevenueThisWeek() { 
    	return query(SQL_DAILY_THIS_WEEK, "day_label", "revenue"); 
    }
    
    @Override
    public OwnerRevenueChartDTO getDailyRevenuePreviousWeek() { 
    	return query(SQL_DAILY_PREV_WEEK, "day_label", "revenue"); 
    }
    
    @Override
    public OwnerRevenueChartDTO getMonthlyRevenueThisYear() { 
    	return query(SQL_MONTHLY_THIS_YEAR, "month_label", "revenue"); 
    }
    
    @Override
    public OwnerRevenueChartDTO getMonthlyRevenuePreviousYear() { 
    	return query(SQL_MONTHLY_PREV_YEAR, "month_label", "revenue"); 
    }
    
    @Override
    public OwnerRevenueChartDTO getRevenueTrendYearlyPast() {
        return query(SQL_TREND_YEARLY_PAST, "year_label", "revenue");
    }

    @Override
    public OwnerRevenueChartDTO getRevenueTrendYearlyFuture() {
        return query(SQL_TREND_YEARLY_FUTURE, "year_label", "revenue");
    }

    
	/*
	 * @Override public OwnerRevenueChartDTO getRevenueTrendMonthly() { return
	 * query(SQL_TREND_MONTHLY, "month_label", "revenue"); }
	 */
    
	/*
	 * @Override public OwnerRevenueChartDTO getRevenueTrendYearly() { return
	 * query(SQL_TREND_YEARLY, "year_label", "revenue"); }
	 */
}