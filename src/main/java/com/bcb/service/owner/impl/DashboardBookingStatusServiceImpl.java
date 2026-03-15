package com.bcb.service.owner.impl;

import com.bcb.service.owner.DashboardBookingStatusService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;
import com.bcb.repository.owner.DashboardBookingStatusRepository;
import com.bcb.repository.owner.impl.DashboardBookingStatusRepositoryImpl;
import com.google.gson.Gson;

public class DashboardBookingStatusServiceImpl implements DashboardBookingStatusService{
	
	private final DashboardBookingStatusRepository repo = new DashboardBookingStatusRepositoryImpl();
	private final Gson gson = new Gson();

	// Trả về Map 4 period → inject 1 lần vào JSP
	public Map<String, List<OwnerBookingStatusChartDTO>> getAllBookingStatusDistribution() {
		
	    Map<String, List<OwnerBookingStatusChartDTO>> map = new LinkedHashMap<>();
	    map.put("Day",   repo.getBookingStatusDistribution("Day"));
	    map.put("Week",  repo.getBookingStatusDistribution("Week"));
	    map.put("Month", repo.getBookingStatusDistribution("Month"));
	    map.put("Year",  repo.getBookingStatusDistribution("Year"));
	    return map;
	}

	// Serialize thành JSON string để nhúng vào JS
	public String getBookingStatusJson() {
	    return gson.toJson(getAllBookingStatusDistribution());
	}
}
