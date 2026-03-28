package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffConfirmPaymentResultDTO;
import com.bcb.service.impl.StaffConfirmPaymentServiceImpl;
import com.bcb.service.staff.StaffConfirmPaymentService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * REST API for staff to confirm full payment before check-in/check-out.
 */
@WebServlet(name = "StaffConfirmPaymentApiServlet", urlPatterns = {"/api/staff/payment/confirm"})
public class StaffConfirmPaymentApiServlet extends BaseStaffApiServlet {

    private final StaffConfirmPaymentService staffConfirmPaymentService = new StaffConfirmPaymentServiceImpl();

    /**
     * Confirms full payment for a booking by staff action.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readRequestBody(request);
        int bookingId = extractInt(body, "bookingId");
        BigDecimal amount = extractDecimal(body, "amount");
        String method = extractString(body, "method");

        if (bookingId <= 0) {
            writeError(response, 400, "Thieu bookingId");
            return;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            writeError(response, 400, "So tien phai lon hon 0");
            return;
        }

        // Validate booking ownership and payment method.
        if (method == null || method.trim().isEmpty()) method = "CASH";
        method = method.trim().toUpperCase();
        if (!"CASH".equals(method) && !"BANK_TRANSFER".equals(method) && !"VNPAY".equals(method)) {
            writeError(response, 400, "Phuong thuc thanh toan khong hop le");
            return;
        }

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null || staffId <= 0) {
            writeError(response, 403, "Khong xac dinh duoc staff");
            return;
        }

        try {
            StaffConfirmPaymentResultDTO result = staffConfirmPaymentService.confirmPayment(
                    bookingId, amount, method, auth.facilityId, staffId);
            writeJson(response, buildResponseJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Loi he thong");
        }
    }

    private String buildResponseJson(StaffConfirmPaymentResultDTO result) {
        if (!result.isSuccess()) {
            return "{\"success\":false,\"message\":\"" + result.getMessage() + "\"}";
        }
        return "{\"success\":true,\"message\":\"" + result.getMessage() + "\"," +
                "\"data\":{\"paidAmount\":" + result.getPaidAmount() +
                ",\"totalAmount\":" + result.getTotalAmount() +
                ",\"paymentStatus\":\"" + result.getPaymentStatus() + "\",\"method\":\"" + result.getMethod() + "\"}}";
    }

    private int extractInt(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return -1;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return -1;

            StringBuilder num = new StringBuilder();
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c >= '0' && c <= '9') num.append(c);
                else if (num.length() > 0) break;
            }
            return num.length() > 0 ? Integer.parseInt(num.toString()) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal extractDecimal(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;

            StringBuilder num = new StringBuilder();
            boolean hasDot = false;
            for (int i = colonIdx + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c >= '0' && c <= '9') {
                    num.append(c);
                } else if (c == '.' && !hasDot) {
                    num.append(c);
                    hasDot = true;
                } else if (num.length() > 0) {
                    break;
                }
            }
            return num.length() > 0 ? new BigDecimal(num.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractString(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;

            int firstQuote = json.indexOf("\"", colonIdx + 1);
            if (firstQuote < 0) return null;
            int endQuote = json.indexOf("\"", firstQuote + 1);
            if (endQuote < 0) return null;
            return json.substring(firstQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }
}
