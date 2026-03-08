package com.bcb.controller.staff;

import com.bcb.service.impl.StaffCheckinServiceImpl;
import com.bcb.service.staff.StaffCheckinService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API for staff check-in / check-out / no-show per SESSION (phiên chơi).
 */
@WebServlet(name = "StaffCheckinApiServlet", urlPatterns = {
        "/api/staff/checkin",
        "/api/staff/checkout",
        "/api/staff/noshow"
})
public class StaffCheckinApiServlet extends BaseStaffApiServlet {

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

        String body = readRequestBody(request);
        int bookingId = extractInt(body, "bookingId");
        int sessionIndex = extractInt(body, "sessionIndex");

        if (bookingId <= 0 || sessionIndex < 0) {
            writeError(response, 400, "Thiếu bookingId hoặc sessionIndex");
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
            writeJson(response, result);
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
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
