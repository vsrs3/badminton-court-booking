package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffBookingCreateOutcomeDto;
import com.bcb.service.impl.StaffBookingCreateServiceImpl;
import com.bcb.service.staff.StaffBookingCreateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * REST API: POST /api/staff/booking/create
 */
@WebServlet(name = "StaffBookingCreateApiServlet", urlPatterns = {"/api/staff/booking/create"})
public class StaffBookingCreateApiServlet extends HttpServlet {

    private final StaffBookingCreateService staffBookingCreateService = new StaffBookingCreateServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readBody(request);

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");

        try {
            StaffBookingCreateOutcomeDto outcome = staffBookingCreateService.createBooking(
                    body, auth.facilityId, staffId);
            if (outcome.getStatus() > 0) {
                response.setStatus(outcome.getStatus());
            }
            response.getWriter().print(outcome.getJson());
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
}
