package com.bcb.utils;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * VNPay helper utilities: HMAC-SHA512 signing, query-string building,
 * payment-code generation, client-IP extraction.
 * Stateless & reusable for any VNPay integration.
 *
 * @author AnhTN
 */
public final class VNPayUtil {

    private VNPayUtil() {}

    /**
     * Computes HMAC-SHA512 hash of {@code data} with the given {@code key}.
     *
     * @param key  the secret key
     * @param data the data to sign
     * @return lowercase hex string of the HMAC
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b)); // lowercase hex
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }


    /**
     * Builds a URL-encoded query string from sorted params.
     * VNPay requires alphabetical key order and US_ASCII encoding.
     *
     * @param params parameter map
     * @return URL-encoded query string
     */
    public static String buildQueryString(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!sb.isEmpty()) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                  .append("=")
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    /**
     * Builds the hash-data string for VNPay HMAC signing.
     * Per VNPay spec: sorted alphabetically, values URL-encoded with US_ASCII.
     * This is the same format as query string but keys are NOT encoded
     * (VNPay keys are plain ASCII so encoding makes no difference).
     *
     * @param params parameter map (raw values, will be URL-encoded here)
     * @return hash data string for signing
     */
    public static String buildHashData(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!sb.isEmpty()) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                  .append("=")
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    /**
     * Generates a unique payment reference code (timestamp + random 4-digit).
     *
     * @return unique transaction code string
     */
    public static String generatePaymentCode() {
        return System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * Extracts the real client IP, supporting X-Forwarded-For proxy header.
     *
     * @param request the servlet request
     * @return client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs; take the first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

