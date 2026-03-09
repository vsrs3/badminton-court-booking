package com.bcb.controller.singlebooking;

import com.bcb.dto.singlebooking.VoucherApplyRequestDTO;
import com.bcb.dto.singlebooking.VoucherApplyResponseDTO;
import com.bcb.exception.singlebooking.SingleBookingUnauthorizedException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.service.singlebooking.VoucherApplyService;
import com.bcb.service.singlebooking.impl.VoucherApplyServiceImpl;
import com.bcb.utils.singlebooking.SingleBookingApiResponseUtil;
import com.bcb.utils.singlebooking.SingleBookingJsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * POST /api/single-booking/apply-voucher
 * Nhận mã voucher từ preview page, kiểm tra hợp lệ và trả về thông tin giảm giá.
 * Không lưu DB – chỉ validate và tính toán. Việc lưu VoucherUsage xảy ra ở confirm-and-pay.
 *
 * @author AnhTN
 */
@WebServlet(name = "SingleBookingApplyVoucherServlet",
            urlPatterns = {"/api/single-booking/apply-voucher"})
public class SingleBookingApplyVoucherServlet extends HttpServlet {

    private final VoucherApplyService voucherApplyService = new VoucherApplyServiceImpl();

    /** Handles POST: validate voucher and return discount info. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // Auth check
            HttpSession session = req.getSession(false);
            Integer accountId = session != null ? (Integer) session.getAttribute("accountId") : null;
            if (accountId == null) {
                throw new SingleBookingUnauthorizedException("UNAUTHORIZED",
                        "Bạn cần đăng nhập để sử dụng voucher.");
            }

            VoucherApplyRequestDTO request =
                    SingleBookingJsonUtil.readBody(req, VoucherApplyRequestDTO.class);

            VoucherApplyResponseDTO data = voucherApplyService.applyVoucher(accountId, request);
            SingleBookingApiResponseUtil.writeSuccess(resp, 200, data);

        } catch (SingleBookingUnauthorizedException e) {
            SingleBookingApiResponseUtil.writeError(resp, 401, e.getCode(), e.getMessage(), null);
        } catch (SingleBookingValidationException e) {
            SingleBookingApiResponseUtil.writeError(resp, 400, e.getCode(), e.getMessage(), e.getDetails());
        } catch (Exception e) {
            SingleBookingApiResponseUtil.writeError(resp, 500, "INTERNAL_ERROR",
                    "Đã xảy ra lỗi khi kiểm tra voucher.", null);
        }
    }
}
