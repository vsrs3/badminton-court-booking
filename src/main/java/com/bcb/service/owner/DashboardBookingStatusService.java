package com.bcb.service.owner;
import java.util.List;
import java.util.Map;
import com.bcb.dto.owner.OwnerBookingStatusChartDTO;

import com.google.gson.Gson;


public interface DashboardBookingStatusService {
	
	Map<String, List<OwnerBookingStatusChartDTO>> getAllBookingStatusDistribution();

	String getBookingStatusJson();
}
