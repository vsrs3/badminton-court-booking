package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.service.impl.StaffCheckinServiceImpl;
import com.bcb.service.staff.StaffCheckinService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * REST API for staff check-in / check-out / no-show per SESSION (phiên chơi).
 *
 * POST /api/staff/checkin
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * POST /api/staff/checkout
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 *
 * POST /api/staff/noshow
 *   Body: {"bookingId": 7, "sessionIndex": 0}
 */
@WebServlet(name = "StaffCheckinApiServlet", urlPatterns = {
        "/api/staff/checkin",
        "/api/staff/checkout",
        "/api/staff/noshow"
})
public class StaffCheckinApiServlet extends HttpServlet {

    private final StaffCheckinService staffCheckinService = new StaffCheckinServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String path = request.getServletPath();
        boolean isCheckin = path.contains("/checkin");
        boolean isCheckout = path.contains("/checkout");

        String body = readBody(request);
        int bookingId = extractInt(body, "bookingId");
        int sessionIndex = extractInt(body, "sessionIndex");

        if (bookingId <= 0 || sessionIndex < 0) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thiếu bookingId hoặc sessionIndex\"}");
            return;
        }

        try {
            String result;
            if (isCheckin) {
                result = staffCheckinService.doCheckin(bookingId, sessionIndex, auth.facilityId);
            } else if (isCheckout) {
                result = staffCheckinService.doCheckout(bookingId, sessionIndex, auth.facilityId);
            } else {
                result = staffCheckinService.doNoShow(bookingId, sessionIndex, auth.facilityId);
            }
            response.getWriter().print(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
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
}
