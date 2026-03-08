package com.bcb.controller.staff;

import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffBookingEditOutcomeDto;
import com.bcb.service.impl.StaffBookingEditServiceImpl;
import com.bcb.service.staff.StaffBookingEditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Booking edit/cancel/release APIs with snapshot-token optimistic lock.
 */
@WebServlet(name = "StaffBookingEditApiServlet", urlPatterns = {
        "/api/staff/booking/edit/preview",
        "/api/staff/booking/edit/save",
        "/api/staff/booking/release-slot",
        "/api/staff/booking/cancel"
})
public class StaffBookingEditApiServlet extends HttpServlet {

    private final StaffBookingEditService staffBookingEditService = new StaffBookingEditServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Staff chưa được gán\"}");
            return;
        }

        String body = readBody(request);
        String path = request.getServletPath();

        try {
            StaffBookingEditOutcomeDto result = staffBookingEditService.process(path, auth.facilityId, staffId, body);
            response.setStatus(result.getStatus());
            response.getWriter().print(result.getJson());
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
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}



