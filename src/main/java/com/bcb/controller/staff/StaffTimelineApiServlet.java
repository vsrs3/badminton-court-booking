package com.bcb.controller.staff;

import com.bcb.controller.staff.StaffAuthUtil.AuthResult;
import com.bcb.dto.staff.StaffTimelineBookedCellDto;
import com.bcb.dto.staff.StaffTimelineCourtDto;
import com.bcb.dto.staff.StaffTimelineDataDto;
import com.bcb.dto.staff.StaffTimelineDisabledCellDto;
import com.bcb.dto.staff.StaffTimelineSlotDto;
import com.bcb.service.impl.StaffTimelineServiceImpl;
import com.bcb.service.staff.StaffTimelineService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * REST API: GET /api/staff/timeline?date=yyyy-MM-dd
 * Returns JSON for the staff timeline grid.
 *
 * Performance: Only returns BOOKED and DISABLED cells.
 * AVAILABLE cells are implied (frontend treats missing keys as available).
 */
@WebServlet(name = "StaffTimelineApiServlet", urlPatterns = {"/api/staff/timeline"})
public class StaffTimelineApiServlet extends HttpServlet {

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
            response.setStatus(400);
            response.getWriter().print("{\"success\":false,\"message\":\"Ngày không hợp lệ\"}");
            return;
        }

        try {
            StaffTimelineDataDto data = staffTimelineService.getTimeline(auth.facilityId, bookingDate);
            response.getWriter().print(buildTimelineJson(data));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"success\":false,\"message\":\"Lỗi hệ thống\"}");
        }
    }

    private String buildTimelineJson(StaffTimelineDataDto data) {
        StringBuilder json = new StringBuilder(2048);
        json.append("{\"success\":true,\"data\":{");

        json.append("\"facilityName\":").append(StaffAuthUtil.escapeJson(data.getFacilityName()));
        json.append(",\"bookingDate\":\"").append(data.getBookingDate()).append("\"");

        json.append(",\"courts\":[");
        for (int i = 0; i < data.getCourts().size(); i++) {
            if (i > 0) json.append(",");
            StaffTimelineCourtDto court = data.getCourts().get(i);
            json.append("{\"courtId\":").append(court.getCourtId());
            json.append(",\"courtName\":").append(StaffAuthUtil.escapeJson(court.getCourtName())).append("}");
        }
        json.append("]");

        json.append(",\"slots\":[");
        for (int i = 0; i < data.getSlots().size(); i++) {
            if (i > 0) json.append(",");
            StaffTimelineSlotDto slot = data.getSlots().get(i);
            json.append("{\"slotId\":").append(slot.getSlotId());
            json.append(",\"startTime\":\"").append(slot.getStartTime()).append("\"");
            json.append(",\"endTime\":\"").append(slot.getEndTime()).append("\"}");
        }
        json.append("]");

        json.append(",\"cells\":[");
        boolean first = true;

        for (StaffTimelineBookedCellDto booked : data.getBookedCells()) {
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

        for (StaffTimelineDisabledCellDto disabled : data.getDisabledCells()) {
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
