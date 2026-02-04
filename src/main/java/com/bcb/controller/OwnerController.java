package com.bcb.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for Owner dashboard and facility management
 * URL: /owner/*
 */
@WebServlet(name = "OwnerController", urlPatterns = {"/owner/*"})
public class OwnerController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // Get current owner user
        HttpSession session = request.getSession();
        Account owner = (Account) session.getAttribute("account");

        // Set owner info for JSP
        request.setAttribute("owner", owner);

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            // Show owner dashboard
            showDashboard(request, response);
        } else {
            // Handle other owner routes (future implementation)
            switch (pathInfo) {
                case "/facilities":
                    // TODO: Facility management (CRUDS)
                    response.sendRedirect(request.getContextPath() + "/owner/dashboard");
                    break;

                case "/staff":
                    // TODO: Staff management (assign, CRUDS)
                    response.sendRedirect(request.getContextPath() + "/owner/dashboard");
                    break;

                case "/bookings":
                    // TODO: View all bookings
                    response.sendRedirect(request.getContextPath() + "/owner/dashboard");
                    break;

                case "/revenue":
                    // TODO: Revenue reports
                    response.sendRedirect(request.getContextPath() + "/owner/dashboard");
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Show owner dashboard
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO: Load owner statistics
        // - Total facilities
        // - Total staff
        // - Total bookings today
        // - Revenue this month

        request.getRequestDispatcher("/jsp/owner/dashboard.jsp").forward(request, response);
    }
}