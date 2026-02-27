package com.bcb.controller.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingPreviewRequestDTO;
import com.bcb.dto.singlebooking.SingleBookingPreviewResponseDTO;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.service.singlebooking.SingleBookingPreviewService;
import com.bcb.service.singlebooking.impl.SingleBookingPreviewServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/single-booking/preview
 * Validates selections and returns pricing summary (no hold).
 *
 * @author AnhTN
 */
@WebServlet(name = "SingleBookingPreviewServlet",
            urlPatterns = {"/api/single-booking/preview"})
public class SingleBookingPreviewServlet extends HttpServlet {

    private final SingleBookingPreviewService previewService = new SingleBookingPreviewServiceImpl();

    /** Handles POST requests for booking preview. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            SingleBookingPreviewRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, SingleBookingPreviewRequestDTO.class);

            SingleBookingPreviewResponseDTO data = previewService.preview(request);
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
