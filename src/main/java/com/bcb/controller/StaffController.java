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

@WebServlet(name = "StaffController", urlPatterns = {"/staff/*"})
public class StaffController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        HttpSession session = request.getSession();
        Account staffAccount = (Account) session.getAttribute("account");
        request.setAttribute("staffAccount", staffAccount);

        if (session.getAttribute("staffId") == null) {
            loadStaffSession(session, staffAccount);
        }

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
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
                if (pathInfo.startsWith("/booking/detail/")) {
                    showBookingDetail(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
        }
    }

    private void loadStaffSession(HttpSession session, Account account) {
        try {
            int accountId = account.getAccountId();

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

            StaffRepositoryImpl staffRepo = new StaffRepositoryImpl();
            List<Facility> facilities = staffRepo.findFacilitiesById(accountId);
            if (!facilities.isEmpty()) {
                session.setAttribute("facilityName", facilities.get(0).getName());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not load staff session: " + e.getMessage());
        }
    }

    private void showTimeline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/staff/staff-timeline.jsp").forward(request, response);
    }

    private void showBookingDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Extract booking ID and pass to JSP as attribute
        String pathInfo = request.getPathInfo(); // "/booking/detail/123"
        String idStr = pathInfo.substring("/booking/detail/".length());
        request.setAttribute("bookingId", idStr);
        request.getRequestDispatcher("/jsp/staff/staff-booking-detail.jsp").forward(request, response);
    }

    private void showBookingList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/staff/staff-booking-list.jsp").forward(request, response);
    }
}