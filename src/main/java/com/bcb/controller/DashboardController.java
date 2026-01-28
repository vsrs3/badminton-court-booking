package com.bcb.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
 *   @Author: AnhTN
 *
 */

@WebServlet("/admin/dashboard")
public class DashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set dashboard statistics
        request.setAttribute("totalLocations", 5);        // From FacilityService
        request.setAttribute("totalCourts", 12);          // From CourtService
        request.setAttribute("activeCourts", 10);         // From CourtService
        request.setAttribute("monthlyBookings", 47);      // From BookingService

        // Forward to JSP
        request.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(request, response);
    }
}
