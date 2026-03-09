package com.bcb.controller.payment;

import com.bcb.dto.payment.PaymentStatusDTO;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /payment/vnpay-ipn — VNPay server-to-server IPN (Instant Payment Notification).
 * Must respond with JSON {@code {"RspCode":"00","Message":"Confirm Success"}}.
 * This is the authoritative callback that updates payment/booking status.
 *
 * @author AnhTN
 */
@WebServlet(name = "VNPayIPNServlet", urlPatterns = {"/payment/vnpay-ipn"})
public class VNPayIPNServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(VNPayIPNServlet.class.getName());
    private final PaymentService paymentService = new PaymentServiceImpl();

    /** Handles VNPay IPN server-to-server callback. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Map<String, String> params = new HashMap<>();
            Enumeration<String> names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                params.put(name, req.getParameter(name));
            }

            PaymentStatusDTO result = paymentService.processVNPayCallback(params);

            if ("SUCCESS".equals(result.getStatus()) || "FAILED".equals(result.getStatus())) {
                out.write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            } else {
                out.write("{\"RspCode\":\"99\",\"Message\":\"" + escapeJson(result.getMessage()) + "\"}");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error processing VNPay IPN", e);
            out.write("{\"RspCode\":\"99\",\"Message\":\"Internal error\"}");
        }
        out.flush();
    }

    /** Escapes a string for safe JSON embedding. */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

