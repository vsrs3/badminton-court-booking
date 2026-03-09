package com.bcb.service.payment.impl;

import com.bcb.config.VNPayConfig;
import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.dto.payment.PaymentStatusDTO;
import com.bcb.dto.payment.VNPayCallbackResult;
import com.bcb.model.Invoice;
import com.bcb.model.Payment;
import com.bcb.repository.booking.BookingRepository;
import com.bcb.repository.booking.InvoiceRepository;
import com.bcb.repository.booking.impl.BookingRepositoryImpl;
import com.bcb.repository.booking.impl.InvoiceRepositoryImpl;
import com.bcb.repository.payment.PaymentRepository;
import com.bcb.repository.payment.impl.PaymentRepositoryImpl;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.VNPayService;
import com.bcb.utils.DBContext;
import com.bcb.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link PaymentService}.
 * Orchestrates Payment ↔ Invoice ↔ Booking status transitions.
 *
 * @author AnhTN
 */
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentServiceImpl.class.getName());
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final BookingRepository bookingRepo;
    private final VNPayService vnPayService;

    /** Default constructor wiring concrete implementations. */
    public PaymentServiceImpl() {
        this.paymentRepo = new PaymentRepositoryImpl();
        this.invoiceRepo = new InvoiceRepositoryImpl();
        this.bookingRepo = new BookingRepositoryImpl();
        this.vnPayService = new VNPayServiceImpl();
    }

    /** {@inheritDoc} */
    @Override
    public PaymentCreateResult createVNPayPayment(int invoiceId, long payAmountVND,
                                                   String paymentType, String description,
                                                   HttpServletRequest httpReq) {
        PaymentCreateResult result = new PaymentCreateResult();
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String txnCode = VNPayUtil.generatePaymentCode();
            LocalDateTime expireAt = LocalDateTime.now().plusMinutes(VNPayConfig.EXPIRE_MINUTES);

            // 1. Persist PENDING payment
            Payment payment = new Payment();
            payment.setInvoiceId(invoiceId);
            payment.setGateway("VNPAY");
            payment.setTransactionCode(txnCode);
            payment.setPaidAmount(BigDecimal.valueOf(payAmountVND));
            payment.setPaymentType(paymentType);
            payment.setMethod("VNPAY");
            payment.setPaymentStatus("PENDING");
            payment.setExpireAt(expireAt);

            int paymentId = paymentRepo.insertPayment(conn, payment);

            conn.commit();

            // 2. Build VNPay URL (stateless, no DB needed)
            String clientIp = VNPayUtil.getClientIp(httpReq);
            String paymentUrl = vnPayService.createPaymentUrl(
                    txnCode, payAmountVND,
                    description != null ? description : "Thanh toan dat san",
                    clientIp, expireAt);

            result.setSuccess(true);
            result.setPaymentId(paymentId);
            result.setTransactionCode(txnCode);
            result.setPaymentUrl(paymentUrl);
            result.setExpireAt(expireAt.format(ISO));

        } catch (Exception e) {
            rollbackQuietly(conn);
            LOG.log(Level.SEVERE, "Failed to create VNPay payment", e);
            result.setSuccess(false);
            result.setMessage("Failed to create payment: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public PaymentStatusDTO processVNPayCallback(Map<String, String> params) {
        PaymentStatusDTO dto = new PaymentStatusDTO();

        // 1. Verify signature
        VNPayCallbackResult cbResult = vnPayService.verifyCallback(params);
        dto.setTransactionCode(cbResult.getTxnRef());

        if (!cbResult.isValid()) {
            dto.setStatus("FAILED");
            dto.setMessage("Chữ ký không hợp lệ.");
            return dto;
        }

        // 2. Find payment
        Optional<Payment> optPayment = paymentRepo.findByTransactionCode(cbResult.getTxnRef());
        if (optPayment.isEmpty()) {
            dto.setStatus("FAILED");
            dto.setMessage("Không tìm thấy thanh toán.");
            return dto;
        }
        Payment payment = optPayment.get();
        dto.setAmount(payment.getPaidAmount().longValue());

        // 3. Verify amount matches
        if (payment.getPaidAmount().longValue() != cbResult.getAmount()) {
            dto.setStatus("FAILED");
            dto.setMessage("Số tiền không khớp.");
            paymentRepo.updateVNPayResult(cbResult.getTxnRef(), "FAILED",
                    cbResult.getVnpayTxnNo(), cbResult.getResponseCode());
            return dto;
        }

        // 4. Already processed? (idempotent)
        if (!"PENDING".equals(payment.getPaymentStatus())) {
            dto.setStatus(payment.getPaymentStatus());
            dto.setVnpayTxnNo(payment.getVnpayTxnNo());
            dto.setMessage("Thanh toán đã được xử lý trước đó.");
            return dto;
        }

        // 5. Update based on response code
        String newStatus;
        if ("00".equals(cbResult.getResponseCode())) {
            newStatus = "SUCCESS";
            dto.setMessage("Thanh toán thành công!");

            // Update Invoice → PAID (or PARTIAL if deposit)
            updateInvoiceAfterSuccess(payment);

            // Update Booking → CONFIRMED
            updateBookingAfterSuccess(payment.getInvoiceId());

        } else {
            newStatus = "FAILED";
            dto.setMessage("Thanh toán thất bại. Mã lỗi VNPay: " + cbResult.getResponseCode());
        }

        paymentRepo.updateVNPayResult(cbResult.getTxnRef(), newStatus,
                cbResult.getVnpayTxnNo(), cbResult.getResponseCode());

        dto.setStatus(newStatus);
        dto.setVnpayTxnNo(cbResult.getVnpayTxnNo());

        // Attach bookingId for frontend redirect
        Optional<Invoice> optInv = invoiceRepo.findById(payment.getInvoiceId());
        optInv.ifPresent(inv -> dto.setBookingId(inv.getBookingId()));

        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public PaymentStatusDTO checkPaymentStatus(String transactionCode) {
        PaymentStatusDTO dto = new PaymentStatusDTO();
        dto.setTransactionCode(transactionCode);

        Optional<Payment> opt = paymentRepo.findByTransactionCode(transactionCode);
        if (opt.isEmpty()) {
            dto.setStatus("NOT_FOUND");
            dto.setMessage("Không tìm thấy thanh toán.");
            return dto;
        }

        Payment p = opt.get();
        dto.setAmount(p.getPaidAmount() != null ? p.getPaidAmount().longValue() : 0L);
        dto.setVnpayTxnNo(p.getVnpayTxnNo());

        // Check late expiry
        if ("PENDING".equals(p.getPaymentStatus())
                && p.getExpireAt() != null
                && LocalDateTime.now().isAfter(p.getExpireAt())) {
            paymentRepo.updateVNPayResult(transactionCode, "FAILED", null, null);
            dto.setStatus("EXPIRED");
            dto.setMessage("Hết thời gian thanh toán.");
            return dto;
        }

        dto.setStatus(p.getPaymentStatus());
        switch (p.getPaymentStatus()) {
            case "SUCCESS" -> dto.setMessage("Thanh toán thành công!");
            case "PENDING" -> dto.setMessage("Đang chờ thanh toán...");
            case "FAILED"  -> dto.setMessage("Thanh toán thất bại.");
            default        -> dto.setMessage(p.getPaymentStatus());
        }

        Optional<Invoice> optInv = invoiceRepo.findById(p.getInvoiceId());
        optInv.ifPresent(inv -> dto.setBookingId(inv.getBookingId()));

        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public void expireOverduePayments() {
        List<Payment> expired = paymentRepo.findExpiredPendingPayments();
        if (expired.isEmpty()) return;

        for (Payment p : expired) {
            try {
                // Mark payment as FAILED
                paymentRepo.updateVNPayResult(p.getTransactionCode(), "FAILED", null, null);

                // Find invoice → booking → expire booking
                Optional<Invoice> optInv = invoiceRepo.findById(p.getInvoiceId());
                if (optInv.isPresent()) {
                    int bookingId = optInv.get().getBookingId();
                    // Use a connection for the booking status update
                    try (Connection conn = DBContext.getConnection()) {
                        bookingRepo.updateStatus(conn, bookingId, "EXPIRED");
                    }
                }
                LOG.info("Expired payment txnCode=" + p.getTransactionCode());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error expiring payment " + p.getTransactionCode(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public PaymentCreateResult retryPaymentForBooking(int bookingId, int accountId, HttpServletRequest httpReq) {
        PaymentCreateResult result = new PaymentCreateResult();

        // 1. Verify booking belongs to user and is PENDING
        String[] bookingInfo = bookingRepo.findBookingOwnershipInfo(bookingId, accountId);
        if (bookingInfo == null) {
            result.setSuccess(false);
            result.setMessage("Không tìm thấy booking hoặc booking không thuộc tài khoản này.");
            result.setErrorCode("NOT_FOUND");
            result.setHttpStatus(404);
            return result;
        }
        String bookingStatus = bookingInfo[0];
        String facilityName = bookingInfo[1];

        if (!"PENDING".equals(bookingStatus)) {
            result.setSuccess(false);
            result.setMessage("Booking #" + bookingId + " không ở trạng thái chờ thanh toán (hiện tại: " + bookingStatus + ").");
            result.setErrorCode("INVALID_STATUS");
            result.setHttpStatus(400);
            return result;
        }

        // 2. Extend hold — synced with VNPay expire
        bookingRepo.extendHold(bookingId, LocalDateTime.now().plusMinutes(VNPayConfig.EXPIRE_MINUTES));

        // 3. Find invoice
        Optional<Invoice> optInvoice = invoiceRepo.findByBookingId(bookingId);
        if (optInvoice.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Không tìm thấy hóa đơn cho booking #" + bookingId);
            result.setErrorCode("NOT_FOUND");
            result.setHttpStatus(404);
            return result;
        }
        Invoice invoice = optInvoice.get();

        // 4. Check if already paid
        if ("PAID".equals(invoice.getPaymentStatus())) {
            result.setSuccess(false);
            result.setMessage("Booking #" + bookingId + " đã được thanh toán.");
            result.setErrorCode("ALREADY_PAID");
            result.setHttpStatus(400);
            return result;
        }

        // 4b. Double-payment guard: reuse existing PENDING payment if not expired yet.
        // This covers the case where user opens 2 tabs and clicks Pay on both.
        // Instead of creating 2 Payment rows, we return the existing VNPay URL.
        Optional<Payment> existingPending = paymentRepo.findPendingByInvoiceId(invoice.getInvoiceId());
        if (existingPending.isPresent()) {
            Payment ep = existingPending.get();
            if (ep.getExpireAt() != null && LocalDateTime.now().isBefore(ep.getExpireAt())) {
                // Reuse existing payment URL — regenerate stateless VNPay URL from same txnCode
                String clientIp = VNPayUtil.getClientIp(httpReq);
                String paymentUrl = vnPayService.createPaymentUrl(
                        ep.getTransactionCode(),
                        ep.getPaidAmount().longValue(),
                        "Thanh toan dat san #" + bookingId + " tai " + facilityName,
                        clientIp,
                        ep.getExpireAt());
                result.setSuccess(true);
                result.setPaymentId(ep.getPaymentId());
                result.setTransactionCode(ep.getTransactionCode());
                result.setPaymentUrl(paymentUrl);
                result.setExpireAt(ep.getExpireAt().format(ISO));
                result.setBookingId(bookingId);
                return result;
            }
            // Expired pending → fall through to create new payment
        }

        // 5. Calculate amount to pay
        BigDecimal totalAmount = invoice.getTotalAmount();
        BigDecimal paidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        int depositPercent = invoice.getDepositPercent() != null ? invoice.getDepositPercent() : 100;

        BigDecimal amountToPay;
        String paymentType;

        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            amountToPay = totalAmount.subtract(paidAmount);
            paymentType = "REMAINING";
        } else {
            amountToPay = totalAmount.multiply(BigDecimal.valueOf(depositPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            paymentType = depositPercent >= 100 ? "FULL" : "DEPOSIT";
        }

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            result.setSuccess(false);
            result.setMessage("Không còn số tiền cần thanh toán.");
            result.setErrorCode("ALREADY_PAID");
            result.setHttpStatus(400);
            return result;
        }

        // 6. Create new VNPay payment (delegates to createVNPayPayment)
        String description = "Thanh toan dat san #" + bookingId + " tai " + facilityName;
        PaymentCreateResult payResult = createVNPayPayment(
                invoice.getInvoiceId(), amountToPay.longValue(), paymentType, description, httpReq);

        if (!payResult.isSuccess()) {
            payResult.setErrorCode("PAYMENT_ERROR");
            payResult.setHttpStatus(500);
        }
        payResult.setBookingId(bookingId);
        return payResult;
    }

    /* ════════════════════════════════════════════════════════
       Private helpers
       ════════════════════════════════════════════════════════ */

    /** Updates Invoice paid_amount and status after successful VNPay payment. */
    private void updateInvoiceAfterSuccess(Payment payment) {
        try {
            Optional<Invoice> optInv = invoiceRepo.findById(payment.getInvoiceId());
            if (optInv.isEmpty()) return;

            Invoice inv = optInv.get();
            BigDecimal newPaid = (inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO)
                    .add(payment.getPaidAmount());

            String newStatus;
            if (newPaid.compareTo(inv.getTotalAmount()) >= 0) {
                newStatus = "PAID";
            } else {
                newStatus = "PARTIAL";
            }
            invoiceRepo.updatePaymentStatus(inv.getInvoiceId(), newStatus, newPaid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to update invoice after payment success", e);
        }
    }

    /** Updates Booking status to CONFIRMED after successful payment. */
    private void updateBookingAfterSuccess(int invoiceId) {
        try {
            Optional<Invoice> optInv = invoiceRepo.findById(invoiceId);
            if (optInv.isEmpty()) return;

            try (Connection conn = DBContext.getConnection()) {
                bookingRepo.updateStatus(conn, optInv.get().getBookingId(), "CONFIRMED");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to update booking after payment success", e);
        }
    }

    /** Quietly rolls back, ignoring errors. */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) { /* ignored */ }
        }
    }

    /** Quietly closes a connection, ignoring errors. */
    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { /* ignored */ }
        }
    }
}
