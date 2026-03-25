package com.bcb.repository.owner;

import com.bcb.dto.owner.OwnerRevenueChartDTO;

public interface DashboardRevenueChartRepository {
	/**
	 * Get each day in the week revenue - Ex: Monday : 50000 VND | Tuesday : 100000 VND ...
	 * @return OwnerRevenueChartDTO dto
	 */
    OwnerRevenueChartDTO getDailyRevenueThisWeek();
    OwnerRevenueChartDTO getDailyRevenuePreviousWeek();

    /**
     * Get each month in the year revenue - Ex: May : 50.000.000 VND | October : 0 VND ...
     * @return OwnerRevenueChartDTO dto
     */
    OwnerRevenueChartDTO getMonthlyRevenueThisYear();
    OwnerRevenueChartDTO getMonthlyRevenuePreviousYear();
    
}