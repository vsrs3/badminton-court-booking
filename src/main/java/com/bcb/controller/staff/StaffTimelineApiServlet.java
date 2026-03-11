package com.bcb.controller.staff;

import com.bcb.dto.staff.StaffTimelineBookedCellDTO;
import com.bcb.dto.staff.StaffTimelineCourtDTO;
import com.bcb.dto.staff.StaffTimelineDataDTO;
import com.bcb.dto.staff.StaffTimelineDisabledCellDTO;
import com.bcb.dto.staff.StaffTimelineSlotDTO;
import com.bcb.service.impl.StaffTimelineServiceImpl;
import com.bcb.service.staff.StaffTimelineService;
import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffAuthUtil.AuthResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(name = "StaffTimelineApiServlet", urlPatterns = {"/api/staff/timeline"})
public class StaffTimelineApiServlet extends BaseStaffApiServlet {

    private final StaffTimelineService staffTimelineService = new StaffTimelineServiceImpl();

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
                    ? LocalDate.parse(dateParam)
                    : LocalDate.now();
        } catch (DateTimeParseException e) {
            writeError(response, 400, "Ngày không hợp lệ");
            return;
        }

        try {
            StaffTimelineDataDTO data = staffTimelineService.getTimeline(auth.facilityId, bookingDate);
            writeJson(response, buildTimelineJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response, 500, "Lỗi hệ thống");
        }
    }

    private String buildTimelineJson(StaffTimelineDataDTO data) {
        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");

        json.append("\"facilityName\":").append(StaffAuthUtil.escapeJson(data.getFacilityName()));
        json.append(",\"bookingDate\":\"").append(data.getBookingDate()).append("\"");

        json.append(",\"courts\":[");
        for (int i = 0; i < data.getCourts().size(); i++) {
            if (i > 0) json.append(",");
            StaffTimelineCourtDTO court = data.getCourts().get(i);
            json.append("{\"courtId\":").append(court.getCourtId());
            json.append(",\"courtName\":").append(StaffAuthUtil.escapeJson(court.getCourtName())).append("}");
        }
        json.append("]");

        json.append(",\"slots\":[");
        for (int i = 0; i < data.getSlots().size(); i++) {
            if (i > 0) json.append(",");
            StaffTimelineSlotDTO slot = data.getSlots().get(i);
            json.append("{\"slotId\":").append(slot.getSlotId());
            json.append(",\"startTime\":\"").append(slot.getStartTime()).append("\"");
            json.append(",\"endTime\":\"").append(slot.getEndTime()).append("\"}");
        }
        json.append("]");

        json.append(",\"cells\":[");
        boolean first = true;

        for (StaffTimelineBookedCellDTO booked : data.getBookedCells()) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"courtId\":").append(booked.getCourtId());
            json.append(",\"slotId\":").append(booked.getSlotId());
            json.append(",\"state\":\"BOOKED\"");
            json.append(",\"bookingId\":").append(booked.getBookingId());
            json.append(",\"bookingStatus\":\"").append(booked.getBookingStatus()).append("\"");
            json.append(",\"customerName\":").append(StaffAuthUtil.escapeJson(booked.getCustomerName()));
            json.append(",\"slotStatus\":\"").append(booked.getSlotStatus()).append("\"");
            json.append("}");
        }

        for (StaffTimelineDisabledCellDTO disabled : data.getDisabledCells()) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"courtId\":").append(disabled.getCourtId());
            json.append(",\"slotId\":").append(disabled.getSlotId());
            json.append(",\"state\":\"DISABLED\"");
            json.append(",\"disabledReason\":").append(StaffAuthUtil.escapeJson(disabled.getReason()));
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }
}
