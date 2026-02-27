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
 * Controller for Admin dashboard and management
 * URL: /admin/*
 */
@WebServlet(name = "AdminController", urlPatterns = {"/admin/*"})
public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // Get current admin user
        HttpSession session = request.getSession();
        Account admin = (Account) session.getAttribute("account");

        // Set admin info for JSP
        request.setAttribute("admin", admin);

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            // Show admin dashboard
            showDashboard(request, response);
        } else {
            // Handle other admin routes (future implementation)
            switch (pathInfo) {
                case "/users":
                    // TODO: User management
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    break;

                case "/facilities":
                    // TODO: Facility management
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Show admin dashboard
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // TODO: Load dashboard statistics
        // - Total users
        // - Total facilities
        // - Total bookings today
        // - Revenue this month

        request.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(request, response);
    }
}