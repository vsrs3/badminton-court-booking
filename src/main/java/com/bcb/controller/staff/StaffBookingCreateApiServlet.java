package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffBookingCreateOutcomeDTO;
import com.bcb.service.impl.StaffBookingCreateServiceImpl;
import com.bcb.service.staff.StaffBookingCreateService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API: POST /api/staff/booking/create
 */
@WebServlet(name = "StaffBookingCreateApiServlet", urlPatterns = {"/api/staff/booking/create"})
public class StaffBookingCreateApiServlet extends BaseStaffApiServlet {

    private final StaffBookingCreateService staffBookingCreateService = new StaffBookingCreateServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readRequestBody(request);
        Integer staffId = (Integer) request.getSession().getAttribute("staffId");

        try {
            StaffBookingCreateOutcomeDTO outcome = staffBookingCreateService.createBooking(body, auth.facilityId, staffId);
            writeJson(response, outcome.getStatus(), outcome.getJson());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }
}
