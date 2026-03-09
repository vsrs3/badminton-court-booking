package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffSlotPriceDataDTO;
import com.bcb.dto.staff.StaffSlotPriceItemDTO;
import com.bcb.service.impl.StaffSlotPriceServiceImpl;
import com.bcb.service.staff.StaffSlotPriceService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(name = "StaffSlotPriceApiServlet", urlPatterns = {"/api/staff/booking/slot-prices"})
public class StaffSlotPriceApiServlet extends BaseStaffApiServlet {

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
            writeError(response, 400, "Ngày không hợp lệ");
            return;
        }

        try {
            StaffSlotPriceDataDTO data = staffSlotPriceService.getSlotPrices(auth.facilityId, bookingDate);
            writeJson(response, buildPricesJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildPricesJson(StaffSlotPriceDataDTO data) {
        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"dayType\":\"").append(data.getDayType()).append("\"");
        json.append(",\"prices\":[");

        boolean first = true;
        for (StaffSlotPriceItemDTO item : data.getPrices()) {
            if (!first) json.append(",");
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
