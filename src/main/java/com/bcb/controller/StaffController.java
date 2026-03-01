package com.bcb.controller;

import com.bcb.model.Account;
import com.bcb.model.Facility;
import com.bcb.repository.impl.StaffRepositoryImpl;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

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

        // ─── Load staff & facility info into session (once) ───
        if (session.getAttribute("staffId") == null) {
            loadStaffSession(session, staffAccount);
        }

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            // Redirect dashboard → timeline (new default)
            response.sendRedirect(request.getContextPath() + "/staff/timeline");
            return;
        }

        switch (pathInfo) {
            case "/timeline":
                showTimeline(request, response);
                break;

            case "/booking/list":
                showBookingList(request, response);
                break;

            case "/checkin":
            case "/checkout":
            case "/bookings":
            case "/walkin":
            case "/inventory":
                response.sendRedirect(request.getContextPath() + "/staff/timeline");
                break;

            default:
                // Check if it's /booking/detail/{id}
                if (pathInfo.startsWith("/booking/detail/")) {
                    showBookingDetail(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
        }
    }

    /**
     * Load staff info from DB and store in session.
     * Uses existing StaffRepositoryImpl.findFacilitiesById() for facility info
     * and a simple query for staff_id.
     */
    private void loadStaffSession(HttpSession session, Account account) {
        try {
            int accountId = account.getAccountId();

            // 1. Load staffId + facilityId from Staff table
            String sql = "SELECT staff_id, facility_id FROM Staff "
                    + "WHERE account_id = ? AND is_active = 1";

            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        session.setAttribute("staffId", rs.getInt("staff_id"));
                        session.setAttribute("facilityId", rs.getInt("facility_id"));
                    }
                }
            }

            // 2. Load facility name using existing repository method
            StaffRepositoryImpl staffRepo = new StaffRepositoryImpl();
            List<Facility> facilities = staffRepo.findFacilitiesById(accountId);

            if (!facilities.isEmpty()) {
                session.setAttribute("facilityName", facilities.get(0).getName());
            }

        } catch (Exception e) {
            System.out.println("⚠️ Could not load staff session: " + e.getMessage());
        }
    }

    /**
     * Show staff timeline page
     */
    private void showTimeline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/staff/staff-timeline.jsp").forward(request, response);
    }

    /**
     * Show booking detail page
     */
    private void showBookingDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/staff/staff-booking-detail.jsp").forward(request, response);
    }

    /**
     * Show booking list page
     */
    private void showBookingList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/staff/staff-booking-list.jsp").forward(request, response);
    }
}