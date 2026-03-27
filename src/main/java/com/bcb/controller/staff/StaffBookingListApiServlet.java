package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffBookingListDataDTO;
import com.bcb.dto.staff.StaffBookingListItemDTO;
import com.bcb.service.impl.StaffBookingListServiceImpl;
import com.bcb.service.staff.StaffBookingListService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StaffBookingListApiServlet", urlPatterns = {"/api/staff/booking/list"})
public class StaffBookingListApiServlet extends BaseStaffApiServlet {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final StaffBookingListService staffBookingListService = new StaffBookingListServiceImpl();

    /**
     * Returns the staff booking list with optional search and filters.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        // Normalize search/status params and map "ALL" to null.
        String search = request.getParameter("search");
        if (search != null) search = search.trim();
        if (search != null && search.isEmpty()) search = null;

        String status = request.getParameter("status");
        if (status != null) status = status.trim();
        if (status != null && (status.isEmpty() || "ALL".equalsIgnoreCase(status))) status = null;

        // Today filter applies to both single and recurring bookings.
        boolean todayOnly = false;
        String todayParam = request.getParameter("today");
        if (todayParam != null) {
            todayOnly = "1".equals(todayParam) || "true".equalsIgnoreCase(todayParam);
        }

        int page = 1;
        int size = DEFAULT_PAGE_SIZE;
        try {
            String p = request.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
            String s = request.getParameter("size");
            if (s != null) size = Math.min(MAX_PAGE_SIZE, Math.max(1, Integer.parseInt(s)));
        } catch (NumberFormatException ignored) {
        }

        try {
            StaffBookingListDataDTO data = staffBookingListService.getBookingList(auth.facilityId, search, status, todayOnly, page, size);
            writeJson(response, buildListJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildListJson(StaffBookingListDataDTO data) {
        StringBuilder json = new StringBuilder(1024);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"page\":").append(data.getPage());
        json.append(",\"size\":").append(data.getSize());
        json.append(",\"totalRows\":").append(data.getTotalRows());
        json.append(",\"totalPages\":").append(data.getTotalPages());
        json.append(",\"bookings\":[");

        boolean first = true;
        for (StaffBookingListItemDTO booking : data.getBookings()) {
            if (!first) json.append(",");
            first = false;

            json.append("{\"bookingId\":").append(booking.getBookingId());
            json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(booking.getCustomerName()));
            json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(booking.getPhone()));
            json.append(",\"bookingDate\":\"").append(booking.getBookingDate()).append("\"");
            json.append(",\"isRecurring\":").append(booking.isRecurring());
            json.append(",\"recurringStartDate\":").append(StaffAuthUtil.escapeJson(booking.getRecurringStartDate()));
            json.append(",\"recurringEndDate\":").append(StaffAuthUtil.escapeJson(booking.getRecurringEndDate()));
            json.append(",\"bookingStatus\":\"").append(booking.getBookingStatus()).append("\"");
            json.append(",\"paymentStatus\":").append(StaffAuthUtil.escapeJson(booking.getPaymentStatus()));
            json.append(",\"courtDisplay\":").append(StaffAuthUtil.escapeJson(booking.getCourtDisplay()));
            json.append(",\"hasNoShow\":").append(booking.isHasNoShow());
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }
}
