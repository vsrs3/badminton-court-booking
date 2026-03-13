package com.bcb.controller.recurring;

import com.bcb.dto.recurring.RecurringPreviewRequestDTO;
import com.bcb.dto.recurring.RecurringPreviewResponseDTO;
import com.bcb.exception.recurring.RecurringNotFoundException;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.service.recurring.RecurringPreviewService;
import com.bcb.service.recurring.impl.RecurringPreviewServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/recurring/preview
 *
 * @author AnhTN
 */
@WebServlet(name = "RecurringPreviewServlet", urlPatterns = {"/api/recurring/preview"})
public class RecurringPreviewServlet extends HttpServlet {

    private final RecurringPreviewService previewService = new RecurringPreviewServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RecurringPreviewRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, RecurringPreviewRequestDTO.class);

            RecurringPreviewResponseDTO data = previewService.preview(request);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);

        } catch (RecurringValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (RecurringNotFoundException e) {
            SingleBookingApiResponseUtil.writeError(resp, 404, e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }
}

