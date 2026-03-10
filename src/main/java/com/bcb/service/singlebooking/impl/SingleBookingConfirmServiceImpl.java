package com.bcb.service.singlebooking.impl;

import com.bcb.config.VNPayConfig;
import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.dto.singlebooking.*;
import com.bcb.exception.DataAccessException;
import com.bcb.exception.singlebooking.SingleBookingConflictException;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.model.*;
import com.bcb.repository.booking.*;
import com.bcb.repository.booking.impl.*;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.repository.voucher.impl.VoucherRepositoryImpl;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import com.bcb.service.singlebooking.SingleBookingConfirmService;
import com.bcb.service.singlebooking.VoucherApplyService;
import com.bcb.utils.DBContext;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;
import com.bcb.validation.singlebooking.SingleBookingSelectionValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transactional confirm-and-pay service.
 * Creates Booking (PENDING) + BookingSlot + CourtSlotBooking locks + Invoice
 * inside a single JDBC transaction. Hold duration synced with VNPay expire.
 *
 * @author AnhTN
 */
public class SingleBookingConfirmServiceImpl implements SingleBookingConfirmService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final FacilityRepository facilityRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;
    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentService paymentService;
    private final VoucherApplyService voucherApplyService;
    private final VoucherRepository voucherRepo;

    public SingleBookingConfirmServiceImpl() {
        this.facilityRepo = new FacilityRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
        this.bookingRepo = new BookingRepositoryImpl();
        this.bookingSlotRepo = new BookingSlotRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
        this.invoiceRepo = new InvoiceRepositoryImpl();
        this.paymentService = new PaymentServiceImpl();
        this.voucherApplyService = new VoucherApplyServiceImpl();
        this.voucherRepo = new VoucherRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public SingleBookingConfirmResponseDTO confirmAndPay(int accountId,
                                                            SingleBookingConfirmRequestDTO request,
                                                            HttpServletRequest httpReq) {
        // --- Pre-transaction validation ---
        SingleBookingSelectionValidator.validateSelectionsNotEmpty(request.getFacilityId(), request.getSelections());
        LocalDate bookingDate = SingleBookingSelectionValidator.parseAndValidateDate(request.getBookingDate());
        SingleBookingSelectionValidator.validateNotPastDate(bookingDate);
        SingleBookingSelectionValidator.validateDepositPercent(request.getDepositPercent());

        Facility facility = facilityRepo.findActiveById(request.getFacilityId())
                .orElseThrow(() -> new SingleBookingNotFoundException("NOT_FOUND",
                        "Facility not found with id=" + request.getFacilityId()));

        List<SingleBookingMatrixTimeSlotDTO> slots =
                timeSlotRepo.findByTimeRange(facility.getOpenTime(), facility.getCloseTime());
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));

        SingleBookingSelectionValidator.validateNoPastSlots(bookingDate, request.getSelections(), slotMap);
        SingleBookingSelectionValidator.validateMin60MinBlocks(request.getSelections(), slotMap);

        List<Court> courts = courtRepo.findActiveByFacilityId(request.getFacilityId());
        Map<Integer, Court> courtMap = courts.stream()
                .collect(Collectors.toMap(Court::getCourtId, c -> c));

        // Compute prices
        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        Map<String, BigDecimal> priceMap = new HashMap<>();
        for (SingleBookingSelectionItemDTO sel : request.getSelections()) {
            Court court = courtMap.get(sel.getCourtId());
            if (court == null) {
                throw new SingleBookingNotFoundException("NOT_FOUND", "Court not found with id=" + sel.getCourtId());
            }
            String key = sel.getCourtId() + ":" + sel.getSlotId();
            if (!priceMap.containsKey(key)) {
                BigDecimal price = resolvePrice(facility.getFacilityId(), court.getCourtTypeId(),
                        dayType, sel.getSlotId(), slotMap);
                priceMap.put(key, price);
            }
        }

        BigDecimal totalAmount = priceMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- Voucher validation (pre-transaction, read-only check) ---
        VoucherApplyResponseDTO voucherResult = null;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherApplyRequestDTO voucherReq = new VoucherApplyRequestDTO();
            voucherReq.setVoucherCode(request.getVoucherCode().trim());
            voucherReq.setFacilityId(request.getFacilityId());
            voucherReq.setTotalAmount(totalAmount);
            // Throws SingleBookingValidationException if invalid — aborts before any DB write
            voucherResult = voucherApplyService.applyVoucher(accountId, voucherReq);
        }

        final BigDecimal discountAmount = voucherResult != null
                ? voucherResult.getDiscountAmount() : BigDecimal.ZERO;
        final BigDecimal finalAmount    = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        final Integer    appliedVoucherId = voucherResult != null ? voucherResult.getVoucherId() : null;

        // --- Transaction ---
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Create Booking PENDING — hold synced with VNPay expire
            LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(VNPayConfig.EXPIRE_MINUTES);
            Booking booking = new Booking();
            booking.setFacilityId(facility.getFacilityId());
            booking.setBookingDate(bookingDate);
            booking.setAccountId(accountId);
            booking.setBookingStatus("PENDING");
            booking.setHoldExpiredAt(holdExpiredAt);

            int bookingId = bookingRepo.insertBooking(conn, booking);

            // 2. Insert BookingSlots + CourtSlotBooking locks
            for (SingleBookingSelectionItemDTO sel : request.getSelections()) {
                String key = sel.getCourtId() + ":" + sel.getSlotId();
                BigDecimal price = priceMap.get(key);

                BookingSlot bs = new BookingSlot();
                bs.setBookingId(bookingId);
                bs.setCourtID(sel.getCourtId());
                bs.setSlotId(sel.getSlotId());
                bs.setPrice(price);

                int bookingSlotId = bookingSlotRepo.insertBookingSlot(conn, bs);

                try {
                    courtSlotBookingRepo.insertLock(conn, sel.getCourtId(), bookingDate,
                            sel.getSlotId(), bookingSlotId);
                } catch (DataAccessException e) {
                    // PK conflict -> SLOT_CONFLICT
                    conn.rollback();
                    throw new SingleBookingConflictException("SLOT_CONFLICT",
                            "Slot conflict: courtId=" + sel.getCourtId() + ", slotId=" + sel.getSlotId()
                            + " on " + bookingDate);
                }
            }

            // 3. Create Invoice (total = finalAmount after voucher discount)
            Invoice invoice = new Invoice();
            invoice.setBookingId(bookingId);
            invoice.setTotalAmount(finalAmount);
            invoice.setDepositPercent(request.getDepositPercent());
            invoice.setVoucherId(appliedVoucherId);
            invoice.setDiscountAmount(discountAmount);

            final boolean isFreeOrder = finalAmount.compareTo(BigDecimal.ZERO) == 0;

            if (isFreeOrder) {
                // Free order: voucher covers 100% → mark PAID immediately, skip payment gateway
                invoice.setPaymentStatus("PAID");
                invoice.setPaidAmount(BigDecimal.ZERO);   // paid 0 ₫ (fully covered by voucher)
            } else {
                invoice.setPaymentStatus("UNPAID");
                invoice.setPaidAmount(BigDecimal.ZERO);
            }

            int invoiceId = invoiceRepo.insertInvoice(conn, invoice);

            // 4. For free orders: confirm Booking immediately (mirrors VNPay success flow)
            if (isFreeOrder) {
                bookingRepo.updateStatus(conn, bookingId, "CONFIRMED");
            }

            // 5. Insert VoucherUsage (inside same transaction — rolled back on failure)
            if (appliedVoucherId != null) {
                voucherRepo.insertVoucherUsage(conn, appliedVoucherId, accountId,
                        bookingId, invoiceId, discountAmount);
            }

            // 6. Commit
            conn.commit();

            // 7. Create VNPay payment — skip when order is free (paidAmount = 0)
            BigDecimal paidAmount = isFreeOrder
                    ? BigDecimal.ZERO
                    : finalAmount.multiply(BigDecimal.valueOf(request.getDepositPercent()))
                                 .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            String paymentType = request.getDepositPercent() >= 100 ? "FULL" : "DEPOSIT";
            String desc = "Thanh toan dat san #" + bookingId + " tai " + facility.getName();

            // 8. Build response
            SingleBookingConfirmResponseDTO resp = new SingleBookingConfirmResponseDTO();
            resp.setBookingId(bookingId);
            resp.setInvoiceId(invoiceId);
            // Free orders are already CONFIRMED — no hold needed
            resp.setHoldExpiredAt(isFreeOrder ? null
                    : holdExpiredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            resp.setTotalAmount(totalAmount);
            resp.setDiscountAmount(discountAmount);
            resp.setFinalAmount(finalAmount);
            resp.setPayAmount(paidAmount);
            resp.setDepositPercent(request.getDepositPercent());

            if (!isFreeOrder && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Normal path: create VNPay payment URL
                PaymentCreateResult payResult = paymentService.createVNPayPayment(
                        invoiceId, paidAmount.longValue(), paymentType, desc, httpReq);
                if (payResult.isSuccess()) {
                    resp.setPaymentUrl(payResult.getPaymentUrl());
                    resp.setTransactionCode(payResult.getTransactionCode());
                }
            }
            // isFreeOrder → paymentUrl stays null → frontend redirects to booking-success directly
            return resp;

        } catch (SingleBookingConflictException e) {
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new DataAccessException("Transaction failed during confirm-and-pay", e);
        } catch (DataAccessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Resolves price for a single slot. Throws on missing/overlapped rules.
     */
    private BigDecimal resolvePrice(int facilityId, int courtTypeId, String dayType,
                                    int slotId, Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
        if (slot == null) {
            throw new SingleBookingValidationException("VALIDATION_ERROR",
                    "Invalid slotId=" + slotId,
                    List.of(Map.of("field", "slotId", "issue", "invalid", "rejectedValue", slotId)));
        }
        LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
        LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);

        List<FacilityPriceRule> rules =
                priceRuleRepo.findByFacilityAndCourtTypeAndDayType(facilityId, courtTypeId, dayType);
        List<FacilityPriceRule> matching = rules.stream()
                .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            throw new SingleBookingValidationException("PRICE_RULE_MISSING",
                    "No price rule for slot " + slot.getStartTime() + "-" + slot.getEndTime(),
                    List.of(Map.of("field", "slotId", "issue", "no_price_rule", "rejectedValue", slotId)));
        }
        if (matching.size() > 1) {
            throw new SingleBookingValidationException("PRICE_RULE_OVERLAPPED",
                    "Multiple price rules match slot " + slot.getStartTime() + "-" + slot.getEndTime(),
                    List.of(Map.of("field", "slotId", "issue", "overlapped_price_rules", "rejectedValue", slotId)));
        }
        return matching.get(0).getPrice();
    }

    /** Quietly rolls back a connection, ignoring errors. */
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

