package com.bcb.controller.staff;

import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffBookingListDataDto;
import com.bcb.dto.staff.StaffBookingListItemDto;
import com.bcb.service.impl.StaffBookingListServiceImpl;
import com.bcb.service.staff.StaffBookingListService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * REST API: GET /api/staff/booking/list?search=...&page=...&size=...
 * Search by: customer_name, phone, booking_id
 * Data scope: Booking.facility_id = staff's facility
 *
 * Updated: thêm hasNoShow flag — true nếu booking có ít nhất 1 slot NO_SHOW
 */
@WebServlet(name = "StaffBookingListApiServlet", urlPatterns = {"/api/staff/booking/list"})
public class StaffBookingListApiServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final StaffBookingListService staffBookingListService = new StaffBookingListServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String search = request.getParameter("search");
        if (search != null) search = search.trim();
        if (search != null && search.isEmpty()) search = null;

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
            StaffBookingListDataDto data = staffBookingListService.getBookingList(auth.facilityId, search, page, size);
            response.getWriter().print(buildListJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildListJson(StaffBookingListDataDto data) {
        StringBuilder json = new StringBuilder(1024);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"page\":").append(data.getPage());
        json.append(",\"size\":").append(data.getSize());
        json.append(",\"totalRows\":").append(data.getTotalRows());
        json.append(",\"totalPages\":").append(data.getTotalPages());
        json.append(",\"bookings\":[");

        boolean first = true;
        for (StaffBookingListItemDto booking : data.getBookings()) {
            if (!first) json.append(",");
            first = false;

            json.append("{\"bookingId\":").append(booking.getBookingId());
            json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(booking.getCustomerName()));
            json.append(",\"phone\":").append(StaffAuthUtil.escapeJson(booking.getPhone()));
            json.append(",\"bookingDate\":\"").append(booking.getBookingDate()).append("\"");
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



