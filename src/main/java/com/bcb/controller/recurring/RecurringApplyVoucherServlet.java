package com.bcb.controller.recurring;

import com.bcb.dto.recurring.RecurringVoucherApplyRequestDTO;
import com.bcb.dto.recurring.RecurringVoucherApplyResponseDTO;
import com.bcb.exception.recurring.RecurringUnauthorizedException;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.service.recurring.RecurringVoucherApplyService;
import com.bcb.service.recurring.impl.RecurringVoucherApplyServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/recurring/apply-voucher
 * Validates voucher for recurring booking and returns discount preview.
 *
 * @author AnhTN
 */
@WebServlet(name = "RecurringApplyVoucherServlet", urlPatterns = {"/api/recurring/apply-voucher"})
public class RecurringApplyVoucherServlet extends HttpServlet {

    private final RecurringVoucherApplyService voucherApplyService = new RecurringVoucherApplyServiceImpl();

    /**
     * Handles voucher validation request in recurring flow.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession session = req.getSession(false);
            Integer accountId = session != null ? (Integer) session.getAttribute("accountId") : null;
            if (accountId == null) {
                throw new RecurringUnauthorizedException("UNAUTHORIZED",
                        "Bạn cần đăng nhập để sử dụng voucher.");
            }

            RecurringVoucherApplyRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, RecurringVoucherApplyRequestDTO.class);

            RecurringVoucherApplyResponseDTO data = voucherApplyService.applyVoucher(accountId, request);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);
        } catch (RecurringUnauthorizedException e) {
            SingleBookingApiResponseUtil.writeError(resp, 401, e.getCode(), e.getMessage(), null);
        } catch (RecurringValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (Exception e) {
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "Đã xảy ra lỗi khi kiểm tra voucher đặt lịch cố định.", null);
        }
    }
}

