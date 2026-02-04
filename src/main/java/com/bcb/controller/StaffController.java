package com.bcb.controller;

import com.bcb.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for Staff dashboard and daily operations
 * URL: /staff/*
 */
@WebServlet(name = "StaffController", urlPatterns = {"/staff/*"})
public class StaffController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // Get current staff user
        HttpSession session = request.getSession();
        Account staffAccount = (Account) session.getAttribute("account");

        // Set staff info for JSP
        request.setAttribute("staffAccount", staffAccount);

        // TODO: Get staff info from Staff table (facility_id, etc.)
        // For now, just show dashboard

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            // Show staff dashboard
            showDashboard(request, response);
        } else {
            // Handle other staff routes (future implementation)
            switch (pathInfo) {
                case "/checkin":
                    // TODO: Check-in page
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    break;

                case "/checkout":
                    // TODO: Check-out page
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    break;

                case "/bookings":
                    // TODO: Today's bookings
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    break;

                case "/walkin":
                    // TODO: Add walk-in booking
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    break;

                case "/inventory":
                    // TODO: Manage racket inventory
                    response.sendRedirect(request.getContextPath() + "/staff/dashboard");
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Show staff dashboard
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO: Load staff dashboard data
        // - Facility they belong to
        // - Today's bookings
        // - Pending check-ins
        // - Available inventory

        request.getRequestDispatcher("/jsp/staff/dashboard.jsp").forward(request, response);
    }
}