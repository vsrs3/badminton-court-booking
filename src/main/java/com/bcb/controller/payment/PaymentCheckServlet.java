package com.bcb.controller.payment;

import com.bcb.dto.payment.PaymentStatusDTO;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /api/payment/check?txnCode=xxx
 * Manual payment status check — called when user clicks "Kiểm tra thanh toán".
 * Returns JSON with current status of the payment.
 *
 * @author AnhTN
 */
@WebServlet(name = "PaymentCheckServlet", urlPatterns = {"/api/payment/check"})
public class PaymentCheckServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentServiceImpl();

    /** Handles GET requests for payment status check. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String txnCode = req.getParameter("txnCode");
        if (txnCode == null || txnCode.trim().isEmpty()) {
            resp.setStatus(400);
            writeJson(resp, false, null, "Thiếu mã giao dịch.");
            return;
        }

        try {
            PaymentStatusDTO status = paymentService.checkPaymentStatus(txnCode.trim());
            writeJson(resp, true, status, null);
        } catch (Exception e) {
            resp.setStatus(500);
            writeJson(resp, false, null, "Lỗi kiểm tra thanh toán.");
        }
    }

    /** Writes a JSON response with success wrapper. */
    private void writeJson(HttpServletResponse resp, boolean success, Object data, String errorMsg)
            throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        if (success) {
            body.put("data", data);
        } else {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", errorMsg);
            body.put("error", error);
        }
        resp.getWriter().write(SingleBookingJsonUtil.toJson(body));
        resp.getWriter().flush();
    }
}

