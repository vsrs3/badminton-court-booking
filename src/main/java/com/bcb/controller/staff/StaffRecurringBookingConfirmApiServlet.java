package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffRecurringBookingOutcomeDTO;
import com.bcb.service.impl.StaffRecurringBookingServiceImpl;
import com.bcb.service.staff.StaffRecurringBookingService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API: POST /api/staff/recurring-booking/confirm
 */
@WebServlet(name = "StaffRecurringBookingConfirmApiServlet", urlPatterns = {"/api/staff/recurring-booking/confirm"})
public class StaffRecurringBookingConfirmApiServlet extends BaseStaffApiServlet {

    private final StaffRecurringBookingService service = new StaffRecurringBookingServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readRequestBody(request);
        Integer staffId = (Integer) request.getSession().getAttribute("staffId");

        try {
            StaffRecurringBookingOutcomeDTO outcome = service.confirm(body, auth.facilityId, staffId);
            writeJson(response, outcome.getStatus(), outcome.getJson());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }
}
