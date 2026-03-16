package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffRefundConfirmResultDTO;
import com.bcb.service.impl.StaffRefundConfirmServiceImpl;
import com.bcb.service.staff.StaffRefundConfirmService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.utils.staff.StaffBookingSnapshotTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffRefundConfirmApiServlet", urlPatterns = {"/api/staff/refund/confirm"})
public class StaffRefundConfirmApiServlet extends BaseStaffApiServlet {

    private final StaffRefundConfirmService staffRefundConfirmService = new StaffRefundConfirmServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String body = readRequestBody(request);
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String note = StaffBookingSnapshotTokenUtil.extractString(body, "note");
        if (bookingId <= 0) {
            writeError(response, 400, "bookingId không hợp lệ");
            return;
        }

        Integer staffId = (Integer) request.getSession().getAttribute("staffId");
        if (staffId == null) {
            writeError(response, 401, "Không tìm thấy staff trong session");
            return;
        }

        try {
            StaffRefundConfirmResultDTO result = staffRefundConfirmService.confirmRefund(
                    bookingId, auth.facilityId, staffId, note);
            String json = buildConfirmJson(result);
            writeJson(response, result.isSuccess() ? 200 : 400, json);
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildConfirmJson(StaffRefundConfirmResultDTO result) {
        StringBuilder json = new StringBuilder(256);
        json.append("{\"success\":").append(result.isSuccess());
        json.append(",\"message\":").append(StaffAuthUtil.escapeJson(result.getMessage()));
        if (result.getRefundStatus() != null) {
            json.append(",\"data\":{");
            json.append("\"refundStatus\":").append(StaffAuthUtil.escapeJson(result.getRefundStatus()));
            json.append(",\"refundNote\":").append(StaffAuthUtil.escapeJson(result.getRefundNote()));
            json.append("}");
        }
        json.append("}");
        return json.toString();
    }

    private int parseInt(String value) {
        if (value == null) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }
}