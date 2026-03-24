package com.bcb.controller.customer;

import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.ReviewService;
import com.bcb.service.impl.ReviewServiceImpl;
import com.bcb.service.mybooking.MyBookingService;
import com.bcb.service.mybooking.impl.MyBookingServiceImpl;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet for customer "My Bookings" feature.
 *
 * GET  /my-bookings                         → list bookings (with optional filters)
 * GET  /my-bookings?action=detail&id=X      → view booking detail
 * POST /my-bookings?action=cancel           → cancel a booking
 * POST /my-bookings?action=retryPayment     → retry/pay for PENDING booking
 * POST /my-bookings?action=payRemaining     → pay remaining amount for CONFIRMED+PARTIAL booking
 *
 * @author AnhTN
 */
@WebServlet(name = "MyBookingsServlet", urlPatterns = {"/my-bookings"})
public class MyBookingsServlet extends HttpServlet {

    private static final int BOOKING_PAGE_SIZE = 10;
    private static final int RECURRING_DAY_PAGE_SIZE = 5;
    private static final DateTimeFormatter DATE_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Gson GSON = new Gson();

    private final MyBookingService bookingService = new MyBookingServiceImpl();
    private final ReviewService reviewService = new ReviewServiceImpl();
    private final PaymentService paymentService = new PaymentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Auth check
        Account account = getAuthenticatedCustomer(request, response);
        if (account == null) return;

        String action = request.getParameter("action");

        if ("future-sessions".equals(action)) {
            loadFutureRecurringSessions(request, response, account);
        } else if ("detail".equals(action)) {
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

        switch (action != null ? action : "") {
            case "cancel"        -> cancelBooking(request, response, account);
            case "retryPayment"  -> retryPayment(request, response, account);
            case "payRemaining"  -> payRemaining(request, response, account);
            default              -> response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }

    /* ════════════════════════════════════════════════════════════════════
       Handlers
       ════════════════════════════════════════════════════════════════════ */

    /**
     * Displays the booking list with optional search/filter.
     */
    private void showBookingList(HttpServletRequest request, HttpServletResponse response,
                                  Account account) throws ServletException, IOException {
        // Read filter params
        String status       = request.getParameter("status");
        String bookingType  = parseBookingType(request.getParameter("bookingType"));
        String dateFromStr = request.getParameter("dateFrom");
        String dateToStr   = request.getParameter("dateTo");

        LocalDate dateFrom = parseDate(dateFromStr);
        LocalDate dateTo   = parseDate(dateToStr);
        int page = parsePositiveInt(request.getParameter("page"), 1);
        int offset = (page - 1) * BOOKING_PAGE_SIZE;

        try {
            List<MyBookingListDTO> bookings = bookingService.getMyBookings(
                    account.getAccountId(), status, bookingType, dateFrom, dateTo, offset, BOOKING_PAGE_SIZE + 1);

            boolean hasMore = bookings.size() > BOOKING_PAGE_SIZE;
            if (hasMore) {
                bookings = bookings.subList(0, BOOKING_PAGE_SIZE);
            }

            // Set isReviewed trong MyBookingDTO
            // true -> bảng Review tồn tại booking_id
            // false -> bảng Review không tồn tại booking_id
            Set<Integer> reviewedIds = reviewService.getReviewedBookingIds(account.getAccountId());
            for (MyBookingListDTO booking : bookings) {
                booking.setReviewed(reviewedIds.contains(booking.getBookingId()));
            }

            request.setAttribute("bookings", bookings);
            request.setAttribute("selectedStatus", status != null ? status : "all");
            request.setAttribute("selectedBookingType", bookingType);
            request.setAttribute("dateFrom", dateFromStr);
            request.setAttribute("dateTo", dateToStr);
            request.setAttribute("page", page);
            request.setAttribute("hasMore", hasMore);
            request.setAttribute("pageSize", BOOKING_PAGE_SIZE);
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
            boolean expandAll = parseBooleanFlag(request.getParameter("expandAll"));
            boolean showPast = parseBooleanFlag(request.getParameter("showPast"));
            int futurePage = parsePositiveInt(request.getParameter("futurePage"), 1);
            int pastPage = parsePositiveInt(request.getParameter("pastPage"), 1);

            int futureDayLimit = expandAll
                    ? futurePage * RECURRING_DAY_PAGE_SIZE
                    : RECURRING_DAY_PAGE_SIZE;
            int pastDayLimit = showPast
                    ? pastPage * RECURRING_DAY_PAGE_SIZE
                    : 0;

            MyBookingDetailDTO detail = bookingService.getBookingDetail(
                    bookingId,
                    account.getAccountId(),
                    futureDayLimit,
                    showPast,
                    pastDayLimit,
                    LocalDate.now());

            request.setAttribute("bookingDetail", detail);
            request.setAttribute("expandAll", expandAll);
            request.setAttribute("showPast", showPast);
            request.setAttribute("futurePage", futurePage);
            request.setAttribute("pastPage", pastPage);
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

    private void loadFutureRecurringSessions(HttpServletRequest request, HttpServletResponse response,
                                             Account account) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            writeSessionJson(response, false, "ID booking không hợp lệ.", null);
            return;
        }

        try {
            int bookingId = Integer.parseInt(idStr);
            int futurePage = parsePositiveInt(request.getParameter("futurePage"), 1);
            int futureDayLimit = futurePage * RECURRING_DAY_PAGE_SIZE;

            MyBookingDetailDTO detail = bookingService.getBookingDetail(
                    bookingId,
                    account.getAccountId(),
                    futureDayLimit,
                    false,
                    0,
                    LocalDate.now());

            int fromIndex = Math.max(0, (futurePage - 1) * RECURRING_DAY_PAGE_SIZE);
            int toIndex = detail.getRecurringSessions() != null
                    ? detail.getRecurringSessions().size()
                    : 0;

            List<Map<String, Object>> sessions = new ArrayList<>();
            if (detail.getRecurringSessions() != null && fromIndex < toIndex) {
                detail.getRecurringSessions().subList(fromIndex, toIndex).forEach(ms -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("bookingDate", ms.getBookingDate() != null ? ms.getBookingDate().toString() : "");
                    row.put("bookingDateDisplay", ms.getBookingDate() != null
                            ? ms.getBookingDate().format(DATE_DDMMYYYY) : "");
                    row.put("courtName", ms.getCourtName());
                    row.put("startTime", ms.getStartTime());
                    row.put("endTime", ms.getEndTime());
                    row.put("slotCount", ms.getSlotCount());
                    row.put("totalPrice", ms.getTotalPrice() != null ? ms.getTotalPrice().toPlainString() : "0");
                    sessions.add(row);
                });
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessions", sessions);
            payload.put("hasMore", detail.isHasMoreFutureSessions());
            payload.put("nextPage", futurePage + 1);
            writeSessionJson(response, true, "OK", payload);
        } catch (NumberFormatException e) {
            writeSessionJson(response, false, "ID booking không hợp lệ.", null);
        } catch (BusinessException e) {
            writeSessionJson(response, false, e.getMessage(), null);
        } catch (Exception e) {
            writeSessionJson(response, false, "Không thể tải thêm lịch tương lai.", null);
        }
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
     * Handles retry/pay for a PENDING booking OR pay remaining for CONFIRMED+PARTIAL booking.
     *
     * <p>Best practice — double-payment guard:
     * {@link PaymentService#retryPaymentForBooking} checks for an existing PENDING payment
     * linked to the same invoice. If one still exists and hasn't expired, it returns the same
     * VNPay URL (no new payment record created). Only when the old payment is expired/failed
     * does it create a new Payment row. This prevents duplicate charges even if the user
     * opens multiple tabs and clicks "Pay" simultaneously.
     *
     * <p>Race condition note: if two requests slip through at the exact same instant,
     * the VNPay callback handler is idempotent — it checks {@code payment_status != 'PENDING'}
     * before processing, so only the first successful callback updates the booking/invoice.
     *
     * @author AnhTN
     */
    private void retryPayment(HttpServletRequest request, HttpServletResponse response,
                               Account account) throws IOException {
        processPaymentRequest(request, response, account, false);
    }

    /**
     * Handles pay-remaining for CONFIRMED + PARTIAL bookings.
     */
    private void payRemaining(HttpServletRequest request, HttpServletResponse response,
                              Account account) throws IOException {
        processPaymentRequest(request, response, account, true);
    }

    private void processPaymentRequest(HttpServletRequest request, HttpServletResponse response,
                                       Account account, boolean payRemainingOnly) throws IOException {
        String idStr = request.getParameter("bookingId");
        HttpSession session = request.getSession();

        if (idStr == null || idStr.isEmpty()) {
            session.setAttribute("errorMessage", "ID booking không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        try {
            int bookingId = Integer.parseInt(idStr);
            PaymentCreateResult result = payRemainingOnly
                    ? paymentService.payRemainingForBooking(bookingId, account.getAccountId(), request)
                    : paymentService.retryPaymentForBooking(bookingId, account.getAccountId(), request);

            if (result.isSuccess()) {
                // Redirect to VNPay payment gateway
                response.sendRedirect(result.getPaymentUrl());
            } else {
                session.setAttribute("errorMessage", result.getMessage());
                response.sendRedirect(request.getContextPath() + "/my-bookings");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID booking không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", payRemainingOnly
                    ? "Không thể thanh toán phần còn lại. Vui lòng thử lại."
                    : "Không thể tạo thanh toán. Vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/my-bookings");
        }
    }

    /* ════════════════════════════════════════════════════════════════════
       Helpers
       ════════════════════════════════════════════════════════════════════ */

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
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBooleanFlag(String value) {
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    private String parseBookingType(String bookingType) {
        if (bookingType == null || bookingType.trim().isEmpty()) return "all";
        String normalized = bookingType.trim().toUpperCase();
        if ("SINGLE".equals(normalized) || "RECURRING".equals(normalized)) {
            return normalized;
        }
        return "all";
    }

    private void writeSessionJson(HttpServletResponse response,
                                  boolean success,
                                  String message,
                                  Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        body.put("data", data);
        response.getWriter().write(GSON.toJson(body));
    }
}

