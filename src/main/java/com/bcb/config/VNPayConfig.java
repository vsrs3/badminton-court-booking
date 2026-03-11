package com.bcb.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * VNPay gateway configuration.
 * Loads settings from vnpay.properties on classpath.
 * Reusable across any payment flow (booking, membership, tournament, etc.).
 *
 * @author AnhTN
 */
public final class VNPayConfig {

    private static final Properties PROPS = new Properties();
    private static final String CONFIG_FILE = "/vnpay.properties";

    static {
        try (InputStream is = VNPayConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException("VNPay config not found: " + CONFIG_FILE);
            }
            PROPS.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load VNPay config", e);
        }
    }

    /** VNPay terminal code. */
    public static final String TMN_CODE = PROPS.getProperty("vnpay.tmnCode", "");

    /** VNPay HMAC secret key. */
    public static final String HASH_SECRET = PROPS.getProperty("vnpay.hashSecret", "");

    /** VNPay payment gateway URL. */
    public static final String PAY_URL = PROPS.getProperty("vnpay.payUrl",
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

    /** URL VNPay redirects the browser to after payment. */
    public static final String RETURN_URL = PROPS.getProperty("vnpay.returnUrl",
            "http://localhost:8080/payment/vnpay-return");

    /** Server-to-server IPN callback URL. */
    public static final String IPN_URL = PROPS.getProperty("vnpay.ipnUrl",
            "http://localhost:8080/payment/vnpay-ipn");

    /** VNPay API version. */
    public static final String VERSION = PROPS.getProperty("vnpay.version", "2.1.0");

    /** VNPay command type. */
    public static final String COMMAND = PROPS.getProperty("vnpay.command", "pay");

    /** VNPay order type. */
    public static final String ORDER_TYPE = PROPS.getProperty("vnpay.orderType", "other");

    /** Payment expiry in minutes (also hold duration). */
    public static final int EXPIRE_MINUTES = Integer.parseInt(
            PROPS.getProperty("vnpay.expireMinutes", "15"));

    private VNPayConfig() {}
}

