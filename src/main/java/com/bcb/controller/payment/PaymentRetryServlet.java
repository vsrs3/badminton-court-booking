package com.bcb.controller.payment;

import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/payment/retry — thin controller for retrying payment on an existing PENDING booking.
 * All business logic (ownership check, hold extension, invoice lookup, VNPay URL creation)
 * is delegated to {@link PaymentService#retryPaymentForBooking}.
 *
 * @author AnhTN
 */
@WebServlet(name = "PaymentRetryServlet", urlPatterns = {"/api/payment/retry"})
public class PaymentRetryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final PaymentService paymentService = new PaymentServiceImpl();

    /** Handles POST request — parses input and delegates to service layer. @author AnhTN */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        try {
            // 1. Auth check
            HttpSession session = req.getSession(false);
            Integer accountId = session != null ? (Integer) session.getAttribute("accountId") : null;
            if (accountId == null) {
                writeError(resp, 401, "UNAUTHORIZED", "Vui lòng đăng nhập.");
                return;
            }

            // 2. Parse request body
            JsonObject body = GSON.fromJson(req.getReader(), JsonObject.class);
            if (body == null || !body.has("bookingId")) {
                writeError(resp, 400, "VALIDATION_ERROR", "bookingId is required.");
                return;
            }
            int bookingId = body.get("bookingId").getAsInt();

            // 3. Delegate to service
            PaymentCreateResult result = paymentService.retryPaymentForBooking(bookingId, accountId, req);

            if (!result.isSuccess()) {
                int status = result.getHttpStatus() != null ? result.getHttpStatus() : 500;
                String code = result.getErrorCode() != null ? result.getErrorCode() : "PAYMENT_ERROR";
                writeError(resp, status, code, result.getMessage());
                return;
            }

            // 4. Return payment URL
            JsonObject data = new JsonObject();
            data.addProperty("paymentUrl", result.getPaymentUrl());
            data.addProperty("transactionCode", result.getTransactionCode());
            data.addProperty("bookingId", result.getBookingId());

            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("data", data);

            resp.setStatus(200);
            resp.getWriter().write(GSON.toJson(json));

        } catch (Exception e) {
            e.printStackTrace();
            writeError(resp, 500, "INTERNAL_ERROR", "Đã xảy ra lỗi. Vui lòng thử lại.");
        }
    }

    /** Writes a standard error JSON response. @author AnhTN */
    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.add("error", error);
        resp.getWriter().write(GSON.toJson(json));
    }
}
