package com.bcb.service.payment;

import com.bcb.dto.payment.VNPayCallbackResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Core VNPay gateway service — handles URL generation and callback verification.
 * Stateless and completely decoupled from any business domain (booking, membership, etc.).
 * Any module needing VNPay integration delegates to this service.
 *
 * @author AnhTN
 */
public interface VNPayService {

    /**
     * Builds a signed VNPay payment URL that the browser should redirect to.
     *
     * @param txnRef    our internal unique transaction reference
     * @param amountVND payment amount in VND (no decimals)
     * @param orderInfo description shown to payer
     * @param clientIp  customer IP address
     * @param expireAt  payment expiry datetime
     * @return complete VNPay redirect URL with HMAC signature
     */
    String createPaymentUrl(String txnRef, long amountVND, String orderInfo,
                            String clientIp, LocalDateTime expireAt);

    /**
     * Verifies a VNPay callback (return or IPN) by checking the HMAC signature
     * and extracting the relevant fields.
     *
     * @param params all query parameters from VNPay
     * @return parsed and verified callback result
     */
    VNPayCallbackResult verifyCallback(Map<String, String> params);
}

