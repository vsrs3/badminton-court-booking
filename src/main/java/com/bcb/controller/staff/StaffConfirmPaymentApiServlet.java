package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffConfirmPaymentResultDto;
import com.bcb.service.impl.StaffConfirmPaymentServiceImpl;
import com.bcb.service.staff.StaffConfirmPaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * REST API for staff to confirm full payment before check-in/check-out.
 *
 * POST /api/staff/payment/confirm
 * Body: {"bookingId": 9, "amount": 110000, "method":"CASH"}
 */
@WebServlet(name = "StaffConfirmPaymentApiServlet", urlPatterns = {"/api/staff/payment/confirm"})
public class StaffConfirmPaymentApiServlet extends HttpServlet {

    private final StaffConfirmPaymentService staffConfirmPaymentService = new StaffConfirmPaymentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readBody(request);
        int bookingId = extractInt(body, "bookingId");
        BigDecimal amount = extractDecimal(body, "amount");
        String method = extractString(body, "method");

        if (bookingId <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thieu bookingId\"}");
            return;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"So tien phai lon hon 0\"}");
            return;
        }

        if (method == null || method.trim().isEmpty()) method = "CASH";
        method = method.trim().toUpperCase();
        if (!"CASH".equals(method) && !"BANK_TRANSFER".equals(method) && !"VNPAY".equals(method)) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Phuong thuc thanh toan khong hop le\"}");
            return;
        }

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null || staffId <= 0) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Khong xac dinh duoc staff\"}");
            return;
        }

        try {
            StaffConfirmPaymentResultDto result = staffConfirmPaymentService.confirmPayment(
                    bookingId, amount, method, auth.facilityId, staffId);
            response.getWriter().print(buildResponseJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Loi he thong\"}");
        }
    }

    private String buildResponseJson(StaffConfirmPaymentResultDto result) {
        if (!result.isSuccess()) {
            return "{\"success\":false,\"message\":\"" + result.getMessage() + "\"}";
        }
        return "{\"success\":true,\"message\":\"" + result.getMessage() + "\"," +
                "\"data\":{\"paidAmount\":" + result.getPaidAmount() +
                ",\"paymentStatus\":\"" + result.getPaymentStatus() + "\",\"method\":\"" + result.getMethod() + "\"}}";
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
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
