package com.bcb.controller.owner;

import com.bcb.dto.owner.OwnerBookingStatusChartDTO;
import com.bcb.dto.owner.OwnerRevenueCardDTO;
import com.bcb.service.owner.DashBoardRevenueService;
import com.bcb.service.owner.DashboardBookingStatusService;
import com.bcb.service.owner.DashboardRevenueChartService;
import com.bcb.service.owner.DashboardOccupancyRateService;
import com.bcb.service.owner.DashboardPeakHourService;
import com.bcb.service.owner.impl.DashBoardRevenueServiceImpl;
import com.bcb.service.owner.impl.DashboardBookingStatusServiceImpl;
import com.bcb.service.owner.impl.DashboardOccupancyRateServiceImpl;
import com.bcb.service.owner.impl.DashboardPeakHourServiceImpl;
import com.bcb.service.owner.impl.DashboardRevenueChartServiceImpl;
import com.bcb.utils.BreadcrumbUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
 *   @Author: AnhTN
 *
 */

@WebServlet("/owner/dashboard")
public class DashboardController extends HttpServlet {
	
	private final DashBoardRevenueService serviceRevenue = new DashBoardRevenueServiceImpl();
	
	private final DashboardBookingStatusService serviceBooking = new DashboardBookingStatusServiceImpl();
	
	private final DashboardRevenueChartService serviceChart = new DashboardRevenueChartServiceImpl();
	
	private final DashboardOccupancyRateService serviceOccupancy = new DashboardOccupancyRateServiceImpl();
	
	private final DashboardPeakHourService servicePeakHour = new DashboardPeakHourServiceImpl();
	
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	HttpSession session  = request.getSession();
    	
    	//Revenue Card
    	OwnerRevenueCardDTO dtoDay = serviceRevenue.getDailyRevenue();
    	OwnerRevenueCardDTO dtoWeek = serviceRevenue.getWeeklyRevenue();
    	OwnerRevenueCardDTO dtoMonth = serviceRevenue.getMonthlyRevenue();
    	OwnerRevenueCardDTO dtoYear = serviceRevenue.getYearlyRevenue();
    	
    	//Booking Status Chart
    	String bookingStatusJson = serviceBooking.getBookingStatusJson();
    	
    	//Revenue Chart
    	String revenueChartJson = serviceChart.getRevenueChartJson();
    	
    	//Occupancy Rate Chart
    	String occupancyJson = serviceOccupancy.getOccupancyJson();
    	
    	//Peak Hour Heat Chart
    	String peakHourJson = servicePeakHour.getPeakHourJson();
   
    	// Session set attribute
    	request.setAttribute("peakHourJson", peakHourJson);
    	request.setAttribute("occupancyJson", occupancyJson);
    	
    	session.setAttribute("revenueChartJson", revenueChartJson); 
    	session.setAttribute("bookingStatusJson", bookingStatusJson);
    	
    	session.setAttribute("dailyRevenue", dtoDay);
    	session.setAttribute("weeklyRevenue", dtoWeek);
    	session.setAttribute("monthlyRevenue", dtoMonth);
    	session.setAttribute("yearlyRevenue", dtoYear);
   

        // Breadcrumb
        BreadcrumbUtils.builder(request).active("Dashboard").build();

        // Forward to JSP
        request.getRequestDispatcher("/jsp/owner/dashboard.jsp").forward(request, response);
    }
}

