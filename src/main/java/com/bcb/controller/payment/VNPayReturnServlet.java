package com.bcb.controller.payment;

import com.bcb.dto.payment.PaymentStatusDTO;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * GET /payment/vnpay-return — VNPay redirects the user's browser here after payment.
 * Verifies the signature, processes the callback, and forwards to a result page.
 *
 * @author AnhTN
 */
@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/payment/vnpay-return"})
public class VNPayReturnServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(VNPayReturnServlet.class.getName());
    private final PaymentService paymentService = new PaymentServiceImpl();

    /** Handles VNPay browser redirect after payment attempt. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, String> params = extractParams(req);
        LOG.info("[VNPay Return] Received params: " + params);

        PaymentStatusDTO result = paymentService.processVNPayCallback(params);
        LOG.info("[VNPay Return] Result: status=" + result.getStatus() + ", msg=" + result.getMessage());

        req.setAttribute("paymentResult", result);

        if ("SUCCESS".equals(result.getStatus())) {
            // Clear payment session data
            if (req.getSession(false) != null) {
                req.getSession().removeAttribute("paymentPageData");
            }
            req.getRequestDispatcher("/jsp/payment/payment-success.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/jsp/payment/payment-failed.jsp").forward(req, resp);
        }
    }

    /** Extracts all query parameters from VNPay callback into a Map. */
    private Map<String, String> extractParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, req.getParameter(name));
        }
        return params;
    }
}

