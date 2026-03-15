package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffRefundListDataDTO;
import com.bcb.dto.staff.StaffRefundListItemDTO;
import com.bcb.service.impl.StaffRefundListServiceImpl;
import com.bcb.service.staff.StaffRefundListService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffRefundListApiServlet", urlPatterns = {"/api/staff/refund/list"})
public class StaffRefundListApiServlet extends BaseStaffApiServlet {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final StaffRefundListService staffRefundListService = new StaffRefundListServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        int page = 1;
        int size = DEFAULT_PAGE_SIZE;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
            String s = request.getParameter("size");
            if (s != null) size = Math.min(MAX_PAGE_SIZE, Math.max(1, Integer.parseInt(s)));
        } catch (NumberFormatException ignored) {
        }

        String search = request.getParameter("q");

        try {
            StaffRefundListDataDTO data = staffRefundListService.getRefundList(auth.facilityId, page, size, search);
            writeJson(response, buildListJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildListJson(StaffRefundListDataDTO data) {
        StringBuilder json = new StringBuilder(1024);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"page\":").append(data.getPage());
        json.append(",\"size\":").append(data.getSize());
        json.append(",\"totalRows\":").append(data.getTotalRows());
        json.append(",\"totalPages\":").append(data.getTotalPages());
        json.append(",\"refunds\":[");

        boolean first = true;
        for (StaffRefundListItemDTO item : data.getRefunds()) {
            if (!first) json.append(",");
            first = false;

            json.append("{\"bookingId\":").append(item.getBookingId());
            json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(item.getCustomerName()));
            json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(item.getPhone()));
            json.append(",\"bookingDate\":\"").append(item.getBookingDate()).append("\"");
            json.append(",\"createdAt\":\"").append(item.getCreatedAt()).append("\"");
            json.append(",\"totalAmount\":").append(item.getTotalAmount() == null ? 0 : item.getTotalAmount());
            json.append(",\"paidAmount\":").append(item.getPaidAmount() == null ? 0 : item.getPaidAmount());
            json.append(",\"refundDue\":").append(item.getRefundDue() == null ? 0 : item.getRefundDue());
            json.append(",\"refundStatus\":").append(StaffAuthUtil.escapeJson(item.getRefundStatus()));
            json.append(",\"refundNote\":").append(StaffAuthUtil.escapeJson(item.getRefundNote()));
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }
}
