package com.bcb.repository.owner.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;
import com.bcb.repository.owner.DashboardBookingStatusRepository;
import com.bcb.utils.DBContext;

public class DashboardBookingStatusRepositoryImpl implements DashboardBookingStatusRepository{
	
	private String buildDateFilter(String period) {
	    return switch (period) {
	        case "Day"   -> "b.booking_date = CAST(GETDATE() AS DATE)";
	        
	        case "Week"  -> "b.booking_date >= CAST( DATEADD( DAY, - ( DATEPART( WEEKDAY, GETDATE() ) + 5) % 7, GETDATE()) AS DATE) "
	                      + "AND b.booking_date <= CAST( GETDATE() AS DATE )";
	                      
	        case "Month" -> "MONTH(b.booking_date) = MONTH( GETDATE()) AND YEAR( b.booking_date) = YEAR( GETDATE())";
	        
	        case "Year"  -> "YEAR(b.booking_date) = YEAR(GETDATE())";
	        
	        default      -> "1=1";
	    };
	}

	@Override
	public List<OwnerBookingStatusChartDTO> getBookingStatusDistribution(String period) {
		String dateFilter = buildDateFilter(period);

	    String sql =
	        "SELECT "
	        + "    booking_status AS status, "
	        + "    COUNT(*) AS booking_count, "
	        + "    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 1) AS pct "
	        + "FROM Booking b "
	        + "WHERE b.booking_status IN ('PENDING','CONFIRMED','EXPIRED','CANCELLED','COMPLETED') "
	        + "  AND " + dateFilter + " "
	        + "GROUP BY booking_status "
	        + "ORDER BY booking_count DESC ";

	    List<OwnerBookingStatusChartDTO> result = new ArrayList<>();

	    try (Connection conn = DBContext.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        while (rs.next()) {
	            result.add(new OwnerBookingStatusChartDTO(
	                rs.getString("status"),
	                rs.getInt("booking_count"),
	                rs.getBigDecimal("pct")
	            ));
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("Lỗi khi truy vấn Booking Status Distribution [" + period + "]", e);
	    }

	    return result;
	}

}
