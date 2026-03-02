package com.bcb.controller.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingConfirmRequestDTO;
import com.bcb.dto.singlebooking.SingleBookingConfirmResponseDTO;
import com.bcb.exception.singlebooking.SingleBookingConflictException;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.exception.singlebooking.SingleBookingUnauthorizedException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.service.singlebooking.SingleBookingConfirmService;
import com.bcb.service.singlebooking.impl.SingleBookingConfirmServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/single-booking/confirm-and-pay
 * Authenticates user, validates, locks slots, creates booking + invoice + VNPay payment.
 * Returns JSON with paymentUrl (VNPay gateway URL) for direct redirect.
 *
 * @author AnhTN
 */
@WebServlet(name = "SingleBookingConfirmAndPayServlet",
            urlPatterns = {"/api/single-booking/confirm-and-pay"})
public class SingleBookingConfirmAndPayServlet extends HttpServlet {

    private final SingleBookingConfirmService confirmService = new SingleBookingConfirmServiceImpl();

    /** Handles POST requests for confirm-and-pay. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Auth check
            HttpSession session = req.getSession(false);
            Integer accountId = null;
            if (session != null) {
                accountId = (Integer) session.getAttribute("accountId");
            }
            if (accountId == null) {
                throw new SingleBookingUnauthorizedException("UNAUTHORIZED",
                        "User must be logged in to confirm booking.");
            }

            SingleBookingConfirmRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, SingleBookingConfirmRequestDTO.class);

            SingleBookingConfirmResponseDTO data = confirmService.confirmAndPay(accountId, request, req);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);

        } catch (SingleBookingUnauthorizedException e) {
            SingleBookingApiResponseUtil.writeError(resp, 401, e.getCode(), e.getMessage(), null);
        } catch (SingleBookingValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (SingleBookingNotFoundException e) {
            SingleBookingApiResponseUtil.writeError(resp, 404, e.getCode(), e.getMessage(), null);
        } catch (SingleBookingConflictException e) {
            SingleBookingApiResponseUtil.writeError(resp, 409, e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }
}
