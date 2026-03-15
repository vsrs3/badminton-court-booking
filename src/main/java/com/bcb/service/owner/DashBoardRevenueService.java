package com.bcb.service.owner;

import com.bcb.dto.owner.OwnerRevenueCardDTO;

public interface DashBoardRevenueService {
	
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
	OwnerRevenueCardDTO getMonthlyRevenue ();
	
	/** (Year)
	 * Find current amount, previous amount and change percent between 2 of them
	 * @return OwnerRevenueDTO
	 */
	OwnerRevenueCardDTO getYearlyRevenue ();
}
