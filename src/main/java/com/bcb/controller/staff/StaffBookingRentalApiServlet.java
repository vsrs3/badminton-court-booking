package com.bcb.controller.staff;

import com.bcb.service.impl.StaffBookingRentalServiceImpl;
import com.bcb.service.staff.StaffBookingRentalService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffBookingRentalApiServlet", urlPatterns = {"/api/staff/booking/rental-status"})
public class StaffBookingRentalApiServlet extends BaseStaffApiServlet {

    private final StaffBookingRentalService service = new StaffBookingRentalServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readRequestBody(request);
        int bookingId = extractInt(body, "bookingId");
        int sessionIndex = extractInt(body, "sessionIndex");
        String status = extractString(body, "status");

        if (bookingId <= 0 || sessionIndex < 0 || status == null || status.trim().isEmpty()) {
            writeError(response, 400, "Thiếu bookingId, sessionIndex hoặc status.");
            return;
        }

        try {
            writeJson(response, service.updateSessionRentalStatus(
                    bookingId,
                    sessionIndex,
                    status.trim().toUpperCase(),
                    auth.facilityId
            ));
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

    private String extractString(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int idx = json.indexOf(search);
            if (idx < 0) return null;
            int colonIdx = json.indexOf(":", idx + search.length());
            if (colonIdx < 0) return null;
            int firstQuote = json.indexOf("\"", colonIdx + 1);
            if (firstQuote < 0) return null;
            int secondQuote = json.indexOf("\"", firstQuote + 1);
            if (secondQuote < 0) return null;
            return json.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            return null;
        }
    }
}
