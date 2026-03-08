package com.bcb.controller.staff;

import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffSlotPriceDataDto;
import com.bcb.dto.staff.StaffSlotPriceItemDto;
import com.bcb.service.impl.StaffSlotPriceServiceImpl;
import com.bcb.service.staff.StaffSlotPriceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * REST API: GET /api/staff/booking/slot-prices?date=yyyy-MM-dd
 *
 * Returns price for every (court, slot) combination on the given date.
 * Price is looked up from FacilityPriceRule based on court_type_id + day_type.
 * Price returned is per 30-minute slot (as stored in DB).
 */
@WebServlet(name = "StaffSlotPriceApiServlet", urlPatterns = {"/api/staff/booking/slot-prices"})
public class StaffSlotPriceApiServlet extends HttpServlet {

    private final StaffSlotPriceService staffSlotPriceService = new StaffSlotPriceServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        AuthResult auth = StaffAuthUtil.validateStaff(request, response);
        if (!auth.valid) return;

        String dateParam = request.getParameter("date");
        LocalDate bookingDate;
        try {
            bookingDate = (dateParam != null && !dateParam.isEmpty())
                    ? LocalDate.parse(dateParam) : LocalDate.now();
        } catch (DateTimeParseException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Ngày không hợp lệ\"}");
            return;
        }

        try {
            StaffSlotPriceDataDto data = staffSlotPriceService.getSlotPrices(auth.facilityId, bookingDate);
            response.getWriter().print(buildPricesJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildPricesJson(StaffSlotPriceDataDto data) {
        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"dayType\":\"").append(data.getDayType()).append("\"");
        json.append(",\"prices\":[");

        boolean first = true;
        for (StaffSlotPriceItemDto item : data.getPrices()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("{\"courtId\":").append(item.getCourtId());
            json.append(",\"slotId\":").append(item.getSlotId());
            json.append(",\"price\":").append(item.getPrice());
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }
}



