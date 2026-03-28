package com.bcb.controller.owner;

import com.bcb.service.owner.DashBoardRevenueService;
import com.bcb.service.owner.DashboardBookingStatusService;
import com.bcb.service.owner.DashboardOccupancyRateService;
import com.bcb.service.owner.DashboardPeakHourService;
import com.bcb.service.owner.DashboardRevenueChartService;
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

import java.io.IOException;

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
        request.setAttribute("dailyRevenue", serviceRevenue.getDailyRevenue());
        request.setAttribute("weeklyRevenue", serviceRevenue.getWeeklyRevenue());
        request.setAttribute("monthlyRevenue", serviceRevenue.getMonthlyRevenue());
        request.setAttribute("yearlyRevenue", serviceRevenue.getYearlyRevenue());

        request.setAttribute("bookingStatusJson", serviceBooking.getBookingStatusJson());
        request.setAttribute("revenueChartJson", serviceChart.getRevenueChartJson());
        request.setAttribute("occupancyJson", serviceOccupancy.getOccupancyJson());
        request.setAttribute("peakHourJson", servicePeakHour.getPeakHourJson());

        BreadcrumbUtils.builder(request).active("Báo cáo doanh thu đặt sân").build();
        request.getRequestDispatcher("/jsp/owner/dashboard.jsp").forward(request, response);
    }
}
