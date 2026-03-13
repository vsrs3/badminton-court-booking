package com.bcb.controller.recurring;

import com.bcb.dto.CourtViewDTO;
import com.bcb.service.CourtService;
import com.bcb.service.impl.CourtServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * GET /api/recurring/courts?facilityId={id}
 * Returns active courts of the facility for recurring setup screen.
 *
 * @author AnhTN
 */
@WebServlet(name = "RecurringFacilityCourtsServlet", urlPatterns = {"/api/recurring/courts"})
public class RecurringFacilityCourtsServlet extends HttpServlet {

    private final CourtService courtService = new CourtServiceImpl();

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
            List<CourtViewDTO> courts = courtService.getCourtsByFacilityDTO(facilityId);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, courts);
        } catch (Exception e) {
            e.printStackTrace();
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }
}

