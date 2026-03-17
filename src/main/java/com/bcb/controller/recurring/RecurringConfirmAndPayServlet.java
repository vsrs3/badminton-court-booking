package com.bcb.controller.recurring;

import com.bcb.dto.recurring.RecurringConfirmRequestDTO;
import com.bcb.dto.recurring.RecurringConfirmResponseDTO;
import com.bcb.exception.recurring.RecurringConflictException;
import com.bcb.exception.recurring.RecurringNotFoundException;
import com.bcb.exception.recurring.RecurringUnauthorizedException;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.service.recurring.RecurringConfirmService;
import com.bcb.service.recurring.impl.RecurringConfirmServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/recurring/confirm-and-pay
 *
 * @author AnhTN
 */
@WebServlet(name = "RecurringConfirmAndPayServlet", urlPatterns = {"/api/recurring/confirm-and-pay"})
public class RecurringConfirmAndPayServlet extends HttpServlet {

    private final RecurringConfirmService confirmService = new RecurringConfirmServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession session = req.getSession(false);
            Integer accountId = session != null ? (Integer) session.getAttribute("accountId") : null;
            if (accountId == null) {
                throw new RecurringUnauthorizedException("UNAUTHORIZED",
                        "User must be logged in to confirm recurring booking.");
            }

            RecurringConfirmRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, RecurringConfirmRequestDTO.class);

            RecurringConfirmResponseDTO data = confirmService.confirmAndPay(accountId, request, req);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);

        } catch (RecurringUnauthorizedException e) {
            SingleBookingApiResponseUtil.writeError(resp, 401, e.getCode(), e.getMessage(), null);
        } catch (RecurringValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (RecurringNotFoundException e) {
            SingleBookingApiResponseUtil.writeError(resp, 404, e.getCode(), e.getMessage(), null);
        } catch (RecurringConflictException e) {
            SingleBookingApiResponseUtil.writeError(resp, 409, e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "An internal error occurred.", null);
        }
    }
}

