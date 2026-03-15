package com.bcb.service.owner.impl;

import com.bcb.dto.owner.OwnerRevenueCardDTO;
import com.bcb.repository.owner.DashBoardRevenueRepository;
import com.bcb.repository.owner.impl.DashBoardRevenueRepositoryImpl;
import com.bcb.service.owner.DashBoardRevenueService;

public class DashBoardRevenueServiceImpl implements DashBoardRevenueService{
	
	//repository owner
	DashBoardRevenueRepository repo = new DashBoardRevenueRepositoryImpl();

	@Override
	public OwnerRevenueCardDTO getWeeklyRevenue() {
		return repo.getWeeklyRevenue();
	}

	@Override
	public OwnerRevenueCardDTO getMonthlyRevenue() {
		return repo.getMontlyRevenue();
	}

	@Override
	public OwnerRevenueCardDTO getYearlyRevenue() {
		return repo.getYearlyRevenue();
	}

	@Override
	public OwnerRevenueCardDTO getDailyRevenue() {
		return repo.getDailyRevenue();
	}
	
}
