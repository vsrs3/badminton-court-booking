package com.bcb.repository.owner;

import java.math.BigDecimal;
import java.util.List;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;
import com.bcb.dto.owner.OwnerRevenueCardDTO;

public interface DashBoardRevenueRepository {
	
	/** (Daily)
	 * Find current amount, previous amount and change percent between 2 of them
	 * @return OwnerRevenueDTO
	 */
	OwnerRevenueCardDTO getDailyRevenue ();
	
	/** (Week)
	 * Find current amount, previous amount and change percent between 2 of them
	 * @return OwnerRevenueDTO
	 */
	OwnerRevenueCardDTO getWeeklyRevenue ();
	
	/** (Month)
	 * Find current amount, previous amount and change percent between 2 of them
	 * @return OwnerRevenueDTO
	 */
	OwnerRevenueCardDTO getMontlyRevenue();
	
	/** (Year)
	 * Find current amount, previous amount and change percent between 2 of them
	 * @return OwnerRevenueDTO
	 */
	OwnerRevenueCardDTO getYearlyRevenue();
	
}
