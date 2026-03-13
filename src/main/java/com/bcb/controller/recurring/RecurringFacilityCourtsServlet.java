package com.bcb.controller.recurring;

import com.bcb.dto.CourtViewDTO;
import com.bcb.dto.recurring.RecurringFacilitySetupDTO;
import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.model.Facility;
import com.bcb.repository.booking.FacilityRepository;
import com.bcb.repository.booking.TimeSlotRepository;
import com.bcb.repository.booking.impl.FacilityRepositoryImpl;
import com.bcb.repository.booking.impl.TimeSlotRepositoryImpl;
import com.bcb.service.CourtService;
import com.bcb.service.impl.CourtServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GET /api/recurring/courts?facilityId={id}
 * Returns active courts of the facility for recurring setup screen.
 *
 * @author AnhTN
 */
@WebServlet(name = "RecurringFacilityCourtsServlet", urlPatterns = {"/api/recurring/courts"})
public class RecurringFacilityCourtsServlet extends HttpServlet {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final CourtService courtService = new CourtServiceImpl();
    private final FacilityRepository facilityRepository = new FacilityRepositoryImpl();
    private final TimeSlotRepository timeSlotRepository = new TimeSlotRepositoryImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String facilityIdStr = req.getParameter("facilityId");
        if (facilityIdStr == null || facilityIdStr.isBlank()) {
            SingleBookingApiResponseUtil.writeError(resp, 400, "VALIDATION_ERROR",
                    "facilityId is required.", null);
            return;
        }

        int facilityId;
        try {
            facilityId = Integer.parseInt(facilityIdStr);
        } catch (NumberFormatException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, "VALIDATION_ERROR",
                    "facilityId must be an integer.", null);
            return;
        }

        try {
            Facility facility = facilityRepository.findActiveById(facilityId)
                    .orElseThrow(() -> new IllegalArgumentException("Facility not found."));

            List<CourtViewDTO> courts = courtService.getCourtsByFacilityDTO(facilityId);
            LocalTime openTime = facility.getOpenTime() != null ? facility.getOpenTime() : LocalTime.of(6, 0);
            LocalTime closeTime = facility.getCloseTime() != null ? facility.getCloseTime() : LocalTime.of(22, 0);
            List<SingleBookingMatrixTimeSlotDTO> slots = timeSlotRepository.findByTimeRange(openTime, closeTime);

            RecurringFacilitySetupDTO data = new RecurringFacilitySetupDTO();
            data.setFacilityName(facility.getName());
            data.setCourts(courts);
            data.setOpenTime(openTime.format(TF));
            data.setCloseTime(closeTime.format(TF));
            data.setTimeOptions(buildTimeOptions(slots));

            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);
        } catch (IllegalArgumentException e) {
            SingleBookingApiResponseUtil.writeError(resp, 404, "NOT_FOUND", e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }

    private List<String> buildTimeOptions(List<SingleBookingMatrixTimeSlotDTO> slots) {
        List<String> values = new ArrayList<>();
        if (slots == null || slots.isEmpty()) {
            return values;
        }
        for (SingleBookingMatrixTimeSlotDTO slot : slots) {
            if (slot.getStartTime() != null && !values.contains(slot.getStartTime())) {
                values.add(slot.getStartTime());
            }
        }
        SingleBookingMatrixTimeSlotDTO last = slots.get(slots.size() - 1);
        if (last.getEndTime() != null && !values.contains(last.getEndTime())) {
            values.add(last.getEndTime());
        }
        return values;
    }
}

