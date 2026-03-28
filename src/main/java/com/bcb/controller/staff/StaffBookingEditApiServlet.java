package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffBookingEditOutcomeDTO;
import com.bcb.service.impl.StaffBookingEditServiceImpl;
import com.bcb.service.staff.StaffBookingEditService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
public class StaffBookingEditApiServlet extends BaseStaffApiServlet {

    private final StaffBookingEditService staffBookingEditService = new StaffBookingEditServiceImpl();

    /**
     * Routes booking edit actions including NO_SHOW slot release with snapshot-token validation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            writeError(response, 403, "Staff chưa được gán");
            return;
        }

        String body = readRequestBody(request);
        String path = request.getServletPath();

        try {
            // Dispatch to the edit/release handlers; each validates etag and booking state.
            StaffBookingEditOutcomeDTO result = staffBookingEditService.process(path, auth.facilityId, staffId, body);
            writeJson(response, result.getStatus(), result.getJson());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }
}
