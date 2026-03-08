package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffBookingDetailDataDto;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDto;
import com.bcb.dto.staff.StaffBookingDetailSessionDto;
import com.bcb.dto.staff.StaffBookingDetailSlotDto;
import com.bcb.service.impl.StaffBookingDetailServiceImpl;
import com.bcb.service.staff.StaffBookingDetailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API: GET /api/staff/booking/detail/{bookingId}
 */
@WebServlet(name = "StaffBookingDetailApiServlet", urlPatterns = {"/api/staff/booking/detail/*"})
public class StaffBookingDetailApiServlet extends HttpServlet {

    private final StaffBookingDetailService staffBookingDetailService = new StaffBookingDetailServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Thiếu booking ID\"}");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Booking ID không hợp lệ\"}");
            return;
        }

        try {
            StaffBookingDetailDataDto data = staffBookingDetailService.getBookingDetail(bookingId, auth.facilityId);
            if (data == null) {
                response.setStatus(404);
                response.getWriter().print("{\"success\":false,\"message\":\"Không tìm thấy booking\"}");
            } else {
                response.getWriter().print(buildDetailJson(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildDetailJson(StaffBookingDetailDataDto data) {
        StringBuilder json = new StringBuilder(4096);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"bookingId\":").append(data.getBookingId());
        json.append(",\"bookingDate\":\"").append(data.getBookingDate()).append("\"");
        json.append(",\"bookingStatus\":\"").append(data.getBookingStatus()).append("\"");
        json.append(",\"createdAt\":").append(esc(data.getCreatedAt()));
        json.append(",\"customerName\":").append(esc(data.getCustomerName()));
        json.append(",\"customerPhone\":").append(esc(data.getCustomerPhone()));
        json.append(",\"customerType\":\"").append(data.getCustomerType()).append("\"");

        json.append(",\"sessions\":[");
        for (int i = 0; i < data.getSessions().size(); i++) {
            if (i > 0) json.append(",");
            StaffBookingDetailSessionDto session = data.getSessions().get(i);

            json.append("{\"sessionIndex\":").append(session.getSessionIndex());
            json.append(",\"courtId\":").append(session.getCourtId());
            json.append(",\"courtName\":").append(esc(session.getCourtName()));
            json.append(",\"startTime\":\"").append(session.getStartTime()).append("\"");
            json.append(",\"endTime\":\"").append(session.getEndTime()).append("\"");
            json.append(",\"slotCount\":").append(session.getSlotCount());
            json.append(",\"totalPrice\":").append(session.getTotalPrice());
            json.append(",\"sessionStatus\":\"").append(session.getSessionStatus()).append("\"");
            json.append(",\"checkinTime\":").append(esc(session.getCheckinTime()));
            json.append(",\"checkoutTime\":").append(esc(session.getCheckoutTime()));

            json.append(",\"bookingSlotIds\":[");
            for (int j = 0; j < session.getBookingSlotIds().size(); j++) {
                if (j > 0) json.append(",");
                json.append(session.getBookingSlotIds().get(j));
            }
            json.append("]");

            json.append(",\"bookingSlots\":[");
            for (int j = 0; j < session.getBookingSlots().size(); j++) {
                if (j > 0) json.append(",");
                appendSlotJson(json, session.getBookingSlots().get(j));
            }
            json.append("]");
            json.append("}");
        }
        json.append("]");

        json.append(",\"slots\":[");
        for (int i = 0; i < data.getSlots().size(); i++) {
            if (i > 0) json.append(",");
            appendSlotJson(json, data.getSlots().get(i));
        }
        json.append("]");

        json.append(",\"invoice\":");
        StaffBookingDetailInvoiceDto invoice = data.getInvoice();
        if (invoice != null) {
            json.append("{\"totalAmount\":").append(invoice.getTotalAmount());
            json.append(",\"paidAmount\":").append(invoice.getPaidAmount());
            json.append(",\"paymentStatus\":\"").append(invoice.getPaymentStatus()).append("\"");
            json.append(",\"refundDue\":").append(invoice.getRefundDue());
            json.append(",\"refundStatus\":\"").append(invoice.getRefundStatus()).append("\"}");
        } else {
            json.append("null");
        }

        json.append(",\"etag\":").append(esc(data.getEtag()));
        json.append("}}");
        return json.toString();
    }

    private void appendSlotJson(StringBuilder json, StaffBookingDetailSlotDto slot) {
        json.append("{\"bookingSlotId\":").append(slot.getBookingSlotId());
        json.append(",\"courtId\":").append(slot.getCourtId());
        json.append(",\"slotId\":").append(slot.getSlotId());
        json.append(",\"startTime\":\"").append(slot.getStartTime()).append("\"");
        json.append(",\"endTime\":\"").append(slot.getEndTime()).append("\"");
        json.append(",\"slotStatus\":\"").append(slot.getSlotStatus()).append("\"");
        json.append(",\"released\":").append(slot.isReleased());
        json.append("}");
    }

    private String esc(String val) {
        return StaffAuthUtil.escapeJson(val);
    }
}
