package com.bcb.repository.owner.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;
import com.bcb.dto.owner.OwnerRevenueCardDTO;
import com.bcb.repository.owner.DashBoardRevenueRepository;
import com.bcb.utils.DBContext;

public class DashBoardRevenueRepositoryImpl implements DashBoardRevenueRepository{

	@Override
	public OwnerRevenueCardDTO getWeeklyRevenue() {
		
		String sql = "SELECT "
					+ "ISNULL("
						+ "SUM("
							+ "CASE "
								+ "WHEN b.booking_date >= CAST("
													+ "DATEADD(DAY, - ( DATEPART(WEEKDAY, GETDATE() ) + 5) % 7, GETDATE() ) "
													+ "AS DATE) "
													
								+ "AND b.booking_date <= CAST(GETDATE() AS DATE) "
								+ "THEN i.paid_amount "
							+ "END"
					+ "), 0.0) AS this_week, "
					
					+ "ISNULL("
						+ "SUM("
							+ "CASE "
								+ "WHEN b.booking_date >= CAST("
													+ "DATEADD(DAY, - ( DATEPART ( WEEKDAY, GETDATE() ) + 5) % 7 - 7, GETDATE() ) "
													+ "AS DATE) "
													
								+ "AND b.booking_date <= CAST("
													+ "DATEADD(DAY, - ( DATEPART ( WEEKDAY, GETDATE() ) + 5) %7 - 1, GETDATE() ) "
													+ "AS DATE) "
								+ "THEN i.paid_amount "
							+ "END), 0.0) AS last_week "
				
				+ "FROM Invoice i "
				+ "JOIN Booking b ON i.booking_id = b.booking_id "
				+ "WHERE b.booking_status = 'COMPLETED' "
				+ "  AND b.booking_date >= CAST( DATEADD( DAY, - ( DATEPART( WEEKDAY, GETDATE() ) + 5 ) % 7 - 7, GETDATE() ) AS DATE) ";
		
		try(Connection conn = DBContext.getConnection(); 
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				BigDecimal thisWeek = rs.getBigDecimal("this_week");
			    BigDecimal lastWeek = rs.getBigDecimal("last_week");

			    return new OwnerRevenueCardDTO(thisWeek, lastWeek);
			}
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Lỗi không thực hiện được câu lệnh SQL Weekly Revenue");
		}
		
		return new OwnerRevenueCardDTO(); 
	}

	@Override
	public OwnerRevenueCardDTO getMontlyRevenue() {
		
		String sql =
		        "SELECT "
		        // Tháng này: từ đầu tháng đến hôm nay
		        	+ "ISNULL("
		        		+ "SUM("
		        			+ "CASE "
		        				+ "WHEN b.booking_date >= CAST("
		        										+ "DATEFROMPARTS( YEAR(GETDATE() ), MONTH(GETDATE() ), 1) AS DATE ) "
						        + "AND b.booking_date <= CAST( "
						        						+ "GETDATE() AS DATE) "
						        + "THEN i.paid_amount "
					        + "END)"
				        + ", 0.0) AS this_month, "
		        
		        // Tháng trước: từ đầu tháng trước đến cuối tháng trước
	        		+ "ISNULL("
	        			+ "SUM("
	        				+ "CASE "
						        + "WHEN b.booking_date >= CAST( "
					        			+ "DATEFROMPARTS( YEAR( DATEADD( MONTH, -1, GETDATE() ) ), MONTH( DATEADD(MONTH,-1,GETDATE() ) ), 1 ) AS DATE ) "
						        
						        + "AND b.booking_date <= CAST( "
						        		+ "EOMONTH( DATEADD( MONTH, -1, GETDATE() ) ) AS DATE ) "
						        + "THEN i.paid_amount "
					        + "END)"
				        + ", 0.0) AS last_month "
					        
		        + "FROM Invoice i "
		        + "JOIN Booking b ON i.booking_id = b.booking_id "
		        + "WHERE b.booking_status = 'COMPLETED' "
		        + "  AND b.booking_date >= CAST("
		        						+ "DATEFROMPARTS( YEAR( DATEADD( MONTH, -1, GETDATE() ) ), MONTH( DATEADD( MONTH,- 1 ,GETDATE() ) ), 1) AS DATE )";
		
		try(Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				BigDecimal thisMonth = rs.getBigDecimal("this_month");
			    BigDecimal lastMonth= rs.getBigDecimal("last_month");

			    return new OwnerRevenueCardDTO(thisMonth, lastMonth);
			}
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Lỗi không thực hiện được câu lệnh SQL Monthly Revenue");
		}
		
		return new OwnerRevenueCardDTO(); 
	}

	@Override
	public OwnerRevenueCardDTO getYearlyRevenue() {
		 String sql =
			        "SELECT "
			        // Năm này: từ 01/01 đến hôm nay
				        + "ISNULL("
				        	+ "SUM("
				        		+ "CASE "
				        			+ "WHEN b.booking_date >= CAST("
				        								+ "DATEFROMPARTS( YEAR( GETDATE() ), 1, 1) AS DATE ) "
							        + "AND b.booking_date <= CAST(GETDATE() AS DATE) "
							        +  "THEN i.paid_amount "
						        + "END)"
					        + ", 0.0) AS this_year, "
						        
			        // Năm trước: từ 01/01 đến 31/12 năm trước
			        + "ISNULL("
			        	+ "SUM("
				        	+ "CASE "
				        		+ "WHEN b.booking_date >= CAST("
			        									+ "DATEFROMPARTS( YEAR( GETDATE() ) - 1, 1,  1 ) AS DATE ) "
						        +  "AND b.booking_date <= CAST("
						        						+ "DATEFROMPARTS( YEAR( GETDATE() ) - 1, 12, 31) AS DATE) "
						        + "THEN i.paid_amount "
					        + "END)"
				        + ", 0.0) AS last_year "
					        
			        + "FROM Invoice i "
			        + "JOIN Booking b ON i.booking_id = b.booking_id "
			        + "WHERE b.booking_status = 'COMPLETED' "
			        + "AND b.booking_date >= CAST("
	        						+ "DATEFROMPARTS( YEAR( GETDATE() ) - 1, 1, 1) AS DATE )";
		 
		 try(Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
				
				ResultSet rs = ps.executeQuery();
				
				if(rs.next()) {
					BigDecimal thisMonth = rs.getBigDecimal("this_year");
				    BigDecimal lastMonth= rs.getBigDecimal("last_year");

				    return new OwnerRevenueCardDTO(thisMonth, lastMonth);
				}
				
			} catch (Exception e) {
				throw new IllegalArgumentException("Lỗi không thực hiện được câu lệnh SQL Yearly Revenue");
			}
			
			return new OwnerRevenueCardDTO(); 
	}

	@Override
	public OwnerRevenueCardDTO getDailyRevenue() {
		String sql = "SELECT "
				
				+ "    ISNULL(SUM(CASE "
				+ "        WHEN b.booking_date = CAST(GETDATE() AS DATE) "
				+ "        THEN i.paid_amount END), 0.0) AS this_day, "
				
				+ "    ISNULL(SUM(CASE "
				+ "        WHEN b.booking_date = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) "
				+ "        THEN i.paid_amount END), 0.0) AS last_day "
				
				+ "FROM Invoice i "
				+ "JOIN Booking b ON i.booking_id = b.booking_id "
				+ "WHERE b.booking_status = 'COMPLETED' "
				+ "AND b.booking_date >= CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)";
		
		try(Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				BigDecimal thisDay = rs.getBigDecimal("this_day");
			    BigDecimal lastDay= rs.getBigDecimal("last_day");

			    return new OwnerRevenueCardDTO(thisDay, lastDay);
			}
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Lỗi không thực hiện được câu lệnh SQL Daily Revenue");
		}
		
		return new OwnerRevenueCardDTO(); 
	}

	
}
