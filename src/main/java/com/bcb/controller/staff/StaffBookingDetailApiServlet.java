package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffBookingDetailDataDTO;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalRowDTO;
import com.bcb.dto.staff.StaffBookingDetailSessionDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;
import com.bcb.service.impl.StaffBookingDetailServiceImpl;
import com.bcb.service.staff.StaffBookingDetailService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API: GET /api/staff/booking/detail/{bookingId}
 */
@WebServlet(name = "StaffBookingDetailApiServlet", urlPatterns = {"/api/staff/booking/detail/*"})
public class StaffBookingDetailApiServlet extends BaseStaffApiServlet {

    private final StaffBookingDetailService staffBookingDetailService = new StaffBookingDetailServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            writeError(response, 400, "Thiếu booking ID");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            writeError(response, 400, "Booking ID không hợp lệ");
            return;
        }

        try {
            StaffBookingDetailDataDTO data = staffBookingDetailService.getBookingDetail(bookingId, auth.facilityId);
            if (data == null) {
                writeError(response, 404, "Không tìm thấy booking");
            } else {
                writeJson(response, buildDetailJson(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildDetailJson(StaffBookingDetailDataDTO data) {
        StringBuilder json = new StringBuilder(8192);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"bookingId\":").append(data.getBookingId());
        json.append(",\"bookingDate\":\"").append(data.getBookingDate()).append("\"");
        json.append(",\"isRecurring\":").append(data.isRecurring());
        json.append(",\"recurringStartDate\":").append(esc(data.getRecurringStartDate()));
        json.append(",\"recurringEndDate\":").append(esc(data.getRecurringEndDate()));
        json.append(",\"bookingStatus\":\"").append(data.getBookingStatus()).append("\"");
        json.append(",\"createdAt\":").append(esc(data.getCreatedAt()));
        json.append(",\"customerName\":").append(esc(data.getCustomerName()));
        json.append(",\"customerPhone\":").append(esc(data.getCustomerPhone()));
        json.append(",\"customerType\":\"").append(data.getCustomerType()).append("\"");

        json.append(",\"sessions\":[");
        for (int i = 0; i < data.getSessions().size(); i++) {
            if (i > 0) json.append(",");
            StaffBookingDetailSessionDTO session = data.getSessions().get(i);

            json.append("{\"sessionIndex\":").append(session.getSessionIndex());
            json.append(",\"courtId\":").append(session.getCourtId());
            json.append(",\"courtName\":").append(esc(session.getCourtName()));
            json.append(",\"sessionDate\":").append(esc(session.getSessionDate()));
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
        StaffBookingDetailInvoiceDTO invoice = data.getInvoice();
        if (invoice != null) {
            json.append("{\"totalAmount\":").append(invoice.getTotalAmount());
            json.append(",\"paidAmount\":").append(invoice.getPaidAmount());
            json.append(",\"paymentStatus\":\"").append(invoice.getPaymentStatus()).append("\"");
            json.append(",\"refundDue\":").append(invoice.getRefundDue());
            json.append(",\"refundStatus\":\"").append(invoice.getRefundStatus()).append("\"}");
        } else {
            json.append("null");
        }

        json.append(",\"rentalRows\":[");
        for (int i = 0; i < data.getRentalRows().size(); i++) {
            if (i > 0) json.append(",");
            StaffBookingDetailRentalRowDTO row = data.getRentalRows().get(i);

            json.append("{");
            json.append("\"courtName\":").append(esc(row.getCourtName()));
            json.append(",\"startTime\":\"").append(row.getStartTime()).append("\"");
            json.append(",\"endTime\":\"").append(row.getEndTime()).append("\"");
            json.append(",\"rentalItemsText\":").append(esc(row.getRentalItemsText()));
            json.append(",\"rentalTotal\":").append(row.getRentalTotal());
            json.append("}");
        }
        json.append("]");

        json.append(",\"courtTotal\":").append(data.getCourtTotal());
        json.append(",\"rentalTotal\":").append(data.getRentalTotal());
        json.append(",\"grandTotal\":").append(data.getGrandTotal());

        json.append(",\"etag\":").append(esc(data.getEtag()));
        json.append("}}");
        return json.toString();
    }

    private void appendSlotJson(StringBuilder json, StaffBookingDetailSlotDTO slot) {
        json.append("{\"bookingSlotId\":").append(slot.getBookingSlotId());
        json.append(",\"courtId\":").append(slot.getCourtId());
        json.append(",\"bookingDate\":").append(esc(slot.getBookingDate()));
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
