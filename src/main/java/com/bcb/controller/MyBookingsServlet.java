package com.bcb.controller;

import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.ReviewService;
import com.bcb.service.impl.ReviewServiceImpl;
import com.bcb.service.mybooking.MyBookingService;
import com.bcb.service.mybooking.impl.MyBookingServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

/**
 * Servlet for customer "My Bookings" feature.
 *
 * GET  /my-bookings                     → list bookings (with optional filters)
 * GET  /my-bookings?action=detail&id=X  → view booking detail
 * POST /my-bookings?action=cancel       → cancel a booking
 */
@WebServlet(name = "MyBookingsServlet", urlPatterns = {"/my-bookings"})
public class MyBookingsServlet extends HttpServlet {

    private final MyBookingService bookingService = new MyBookingServiceImpl();
    private final ReviewService reviewService = new ReviewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Auth check
        Account account = getAuthenticatedCustomer(request, response);
        if (account == null) return;

        String action = request.getParameter("action");

        if ("detail".equals(action)) {
            showBookingDetail(request, response, account);
        } else {
            showBookingList(request, response, account);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Auth check
        Account account = getAuthenticatedCustomer(request, response);
        if (account == null) return;

        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            cancelBooking(request, response, account);
        } else {
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }

    /**
     * Displays the booking list with optional search/filter.
     */
    private void showBookingList(HttpServletRequest request, HttpServletResponse response,
                                  Account account) throws ServletException, IOException {
        // Read filter params
        String status = request.getParameter("status");
        String dateFromStr = request.getParameter("dateFrom");
        String dateToStr = request.getParameter("dateTo");

        LocalDate dateFrom = parseDate(dateFromStr);
        LocalDate dateTo = parseDate(dateToStr);

        try {
            List<MyBookingListDTO> bookings = bookingService.getMyBookings(
                    account.getAccountId(), status, dateFrom, dateTo);
            
            // Set isReviewed trong MyBookingDTO
            // true -> bảng Review tồn tại booking_id
            // false -> bảng Review không tồn tại booking_id
            Set<Integer> reviewedIds = reviewService.getReviewedBookingIds(account.getAccountId());
            for (MyBookingListDTO booking : bookings) {
                booking.setReviewed(reviewedIds.contains(booking.getBookingId()));
            } //

            request.setAttribute("bookings", bookings);
            request.setAttribute("selectedStatus", status != null ? status : "all");
            request.setAttribute("dateFrom", dateFromStr);
            request.setAttribute("dateTo", dateToStr);
            request.setAttribute("section", "history");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải danh sách booking. Vui lòng thử lại.");
            request.setAttribute("section", "history");
        }

        request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
    }

    /**
     * Displays booking detail.
     */
    private void showBookingDetail(HttpServletRequest request, HttpServletResponse response,
                                    Account account) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            int bookingId = Integer.parseInt(idStr);
            MyBookingDetailDTO detail = bookingService.getBookingDetail(bookingId, account.getAccountId());

            request.setAttribute("bookingDetail", detail);
            request.setAttribute("section", "booking-detail");

        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "ID booking không hợp lệ.");
            request.setAttribute("section", "history");
        } catch (BusinessException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("section", "history");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải chi tiết booking.");
            request.setAttribute("section", "history");
        }

        request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
    }

    /**
     * Cancels a booking and redirects back to list.
     */
    private void cancelBooking(HttpServletRequest request, HttpServletResponse response,
                                Account account) throws IOException {
        String idStr = request.getParameter("bookingId");
        HttpSession session = request.getSession();

        if (idStr == null || idStr.isEmpty()) {
            session.setAttribute("errorMessage", "ID booking không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            int bookingId = Integer.parseInt(idStr);
            bookingService.cancelBooking(bookingId, account.getAccountId());
            session.setAttribute("successMessage", "Đã hủy booking #" + bookingId + " thành công.");

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID booking không hợp lệ.");
        } catch (BusinessException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Không thể hủy booking. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/my-bookings");
    }

    /**
     * Authenticates and returns CUSTOMER account. Returns null and redirects if not valid.
     */
    private Account getAuthenticatedCustomer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return null;
        }

        Account account = (Account) session.getAttribute("account");
        if (!"CUSTOMER".equals(account.getRole())) {
            response.sendRedirect(request.getContextPath() + "/");
            return null;
        }

        return account;
    }

    /**
     * Safely parses a date string (yyyy-MM-dd). Returns null on invalid input.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
