package com.bcb.repository.owner;

import java.util.List;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;

public interface DashboardBookingStatusRepository {

	/**
	 * Filter the booking by their status
	 * @param period   
	 * @return List of booking
	 */
	List<OwnerBookingStatusChartDTO> getBookingStatusDistribution(String period);
	// period: "Day"  "Week"  "Month"  "Year"
}
