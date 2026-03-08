package com.bcb.service.staff;

import com.bcb.dto.staff.StaffConfirmPaymentResultDto;

import java.math.BigDecimal;

public interface StaffConfirmPaymentService {
    StaffConfirmPaymentResultDto confirmPayment(int bookingId, BigDecimal amount, String method,
                                                int facilityId, int staffId) throws Exception;
}
