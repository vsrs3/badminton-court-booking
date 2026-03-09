package com.bcb.service.payment.impl;

import com.bcb.config.VNPayConfig;
import com.bcb.dto.payment.VNPayCallbackResult;
import com.bcb.service.payment.VNPayService;
import com.bcb.utils.VNPayUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link VNPayService}.
 * Pure gateway logic — no DB access, no business rules.
 *
 * @author AnhTN
 */
public class VNPayServiceImpl implements VNPayService {

    private static final DateTimeFormatter VN_DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Builds a signed VNPay redirect URL.
     * Uses TreeMap for alphabetical sorting, US_ASCII URL-encoding for hash.
     *
     * @author AnhTN
     */
    @Override
    public String createPaymentUrl(String txnRef, long amountVND,
                                   String orderInfo, String clientIp,
                                   LocalDateTime expireAt) {
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    VNPayConfig.VERSION);
        params.put("vnp_Command",    VNPayConfig.COMMAND);
        params.put("vnp_TmnCode",    VNPayConfig.TMN_CODE);
        params.put("vnp_Amount",     String.valueOf(amountVND * 100));
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  VNPayConfig.ORDER_TYPE);
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  VNPayConfig.RETURN_URL);
        params.put("vnp_IpAddr",     clientIp != null ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", now.format(VN_DT));
        if (expireAt != null) {
            params.put("vnp_ExpireDate", expireAt.format(VN_DT));
        }

        // Build hash data (sorted, URL-encoded values) and sign
        String hashData   = VNPayUtil.buildHashData(params);
        String secureHash = VNPayUtil.hmacSHA512(VNPayConfig.HASH_SECRET, hashData);

        // Build query string (same encoding) and append signature
        String queryString = VNPayUtil.buildQueryString(params);
        return VNPayConfig.PAY_URL + "?" + queryString
                + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Verifies VNPay callback signature and extracts result fields.
     * Params from servlet are already URL-decoded by the container,
     * so buildHashData will re-encode them to match the original hash.
     *
     * @author AnhTN
     */
    @Override
    public VNPayCallbackResult verifyCallback(Map<String, String> params) {
        VNPayCallbackResult result = new VNPayCallbackResult();

        String receivedHash = params.get("vnp_SecureHash");
        result.setTxnRef(params.get("vnp_TxnRef"));
        result.setResponseCode(params.get("vnp_ResponseCode"));
        result.setVnpayTxnNo(params.get("vnp_TransactionNo"));

        // Parse amount (VNPay sends amount × 100)
        String amtStr = params.get("vnp_Amount");
        if (amtStr != null && !amtStr.isEmpty()) {
            result.setAmount(Long.parseLong(amtStr) / 100);
        }

        // Remove hash fields before re-computing signature
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        // buildHashData sorts + URL-encodes values → same as when we created the URL
        String hashData       = VNPayUtil.buildHashData(verifyParams);
        String calculatedHash = VNPayUtil.hmacSHA512(VNPayConfig.HASH_SECRET, hashData);

        result.setValid(calculatedHash.equalsIgnoreCase(receivedHash));
        return result;
    }
}

