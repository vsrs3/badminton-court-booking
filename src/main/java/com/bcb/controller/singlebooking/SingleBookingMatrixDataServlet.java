package com.bcb.controller.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingMatrixResponseDTO;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.service.singlebooking.SingleBookingMatrixService;
import com.bcb.service.singlebooking.impl.SingleBookingMatrixServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.validation.singlebooking.SingleBookingSelectionValidator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

/**
 * GET /api/single-booking/matrix-data?facilityId={id}&amp;date=YYYY-MM-DD
 * Returns the booking matrix data as JSON.
 *
 * @author AnhTN
 */
@WebServlet(name = "SingleBookingMatrixDataServlet",
            urlPatterns = {"/api/single-booking/matrix-data"})
public class SingleBookingMatrixDataServlet extends HttpServlet {

    private final SingleBookingMatrixService matrixService = new SingleBookingMatrixServiceImpl();

    /** Handles GET requests for matrix data. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Parse facilityId
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

            // Parse and validate date
            String dateStr = req.getParameter("date");
            LocalDate bookingDate = SingleBookingSelectionValidator.parseAndValidateDate(dateStr);
            SingleBookingSelectionValidator.validateNotPastDate(bookingDate);

            // Build matrix
            SingleBookingMatrixResponseDTO data = matrixService.getMatrixData(facilityId, bookingDate);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);

        } catch (SingleBookingValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (SingleBookingNotFoundException e) {
            SingleBookingApiResponseUtil.writeError(resp, 404, e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }
}
