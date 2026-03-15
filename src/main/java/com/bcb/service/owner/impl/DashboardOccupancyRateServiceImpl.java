package com.bcb.service.owner.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.bcb.dto.owner.OwnerOccupancyRateChartDTO;
import com.bcb.repository.owner.DashboardOccupancyRateRepository;
import com.bcb.repository.owner.impl.DashboardOccupancyRateRepositoryImpl;
import com.bcb.service.owner.DashboardOccupancyRateService;
import com.google.gson.Gson;

public class DashboardOccupancyRateServiceImpl implements DashboardOccupancyRateService{

	private final DashboardOccupancyRateRepository repo = new DashboardOccupancyRateRepositoryImpl();
    private final Gson gson = new Gson();
	
	@Override
	public String getOccupancyJson() {
		
		OwnerOccupancyRateChartDTO dto = repo.getOccupancyRate();
		
		// Gson serialize Map → {"Day":58.3,"Week":62.1,"Month":65.5,"Year":70.2}
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Day",   dto.getDay());
        map.put("Week",  dto.getWeek());
        map.put("Month", dto.getMonth());
        map.put("Year",  dto.getYear());

        return gson.toJson(map);
	}
	
}
