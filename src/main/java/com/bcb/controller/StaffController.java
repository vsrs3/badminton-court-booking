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
 * Front controller for all /staff/* page routes.
 * API routes are handled by separate servlets under /api/staff/*.
 *
 * Task 9c: Added /booking/create route.
 */
@WebServlet(name = "StaffController", urlPatterns = {"/staff/*"})
public class StaffController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ─── Auth guard ───
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        Account staffAccount = (Account) session.getAttribute("account");
        if (staffAccount == null || !"STAFF".equals(staffAccount.getRole())) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        request.setAttribute("staffAccount", staffAccount);

        // Load staff session data (facility_id, etc.) if not yet loaded
        if (session.getAttribute("staffId") == null) {
            loadStaffSession(session, staffAccount);
        }

        // ─── Routing ───
        String pathInfo = request.getPathInfo();

        // Default: redirect to timeline
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            response.sendRedirect(request.getContextPath() + "/staff/timeline");
            return;
        }

        switch (pathInfo) {
            case "/timeline":
                forward(request, response, "/jsp/staff/staff-timeline.jsp");
                break;

            case "/booking/list":
                forward(request, response, "/jsp/staff/staff-booking-list.jsp");
                break;

            case "/booking/create":
                forward(request, response, "/jsp/staff/staff-booking-create.jsp");
                break;

            case "/rental/status":
                forward(request, response, "/jsp/staff/staff-rental-status.jsp");
                break;

            default:
                if (pathInfo.startsWith("/booking/detail/")) {
                    String idStr = pathInfo.substring("/booking/detail/".length());
                    // Validate ID format before forwarding
                    if (idStr.matches("\\d+")) {
                        request.setAttribute("bookingId", idStr);
                        forward(request, response, "/jsp/staff/staff-booking-detail.jsp");
                    } else {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking ID");
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(jspPath).forward(request, response);
    }

    private void loadStaffSession(HttpSession session, Account account) {
        try {
            int accountId = account.getAccountId();

            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT staff_id, facility_id FROM Staff WHERE account_id = ? AND is_active = 1")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        session.setAttribute("staffId", rs.getInt("staff_id"));
                        session.setAttribute("facilityId", rs.getInt("facility_id"));
                    }
                }
            }

            StaffRepositoryImpl staffRepo = new StaffRepositoryImpl();
            List<Facility> facilities = staffRepo.findFacilitiesById(accountId);
            if (!facilities.isEmpty()) {
                session.setAttribute("facilityName", facilities.get(0).getName());
            }
        } catch (Exception e) {
            System.err.println("Could not load staff session: " + e.getMessage());
        }
    }
}
