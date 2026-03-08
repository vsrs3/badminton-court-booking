package com.bcb.service.staff;

import com.bcb.dto.staff.StaffConfirmPaymentResultDTO;

import java.math.BigDecimal;

public interface StaffConfirmPaymentService {
    StaffConfirmPaymentResultDTO confirmPayment(int bookingId, BigDecimal amount, String method,
                                                int facilityId, int staffId) throws Exception;
}

