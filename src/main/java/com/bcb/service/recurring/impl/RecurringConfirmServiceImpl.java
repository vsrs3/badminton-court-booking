package com.bcb.service.recurring.impl;

import com.bcb.config.VNPayConfig;
import com.bcb.dto.payment.PaymentCreateResult;
import com.bcb.dto.recurring.RecurringConfirmRequestDTO;
import com.bcb.dto.recurring.RecurringConfirmResponseDTO;
import com.bcb.dto.recurring.RecurringModifiedSessionDTO;
import com.bcb.dto.recurring.RecurringPatternDTO;
import com.bcb.dto.recurring.RecurringPreviewSessionDTO;
import com.bcb.dto.recurring.RecurringVoucherApplyRequestDTO;
import com.bcb.dto.recurring.RecurringVoucherApplyResponseDTO;
import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.exception.recurring.RecurringConflictException;
import com.bcb.exception.recurring.RecurringNotFoundException;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.model.Booking;
import com.bcb.model.BookingSlot;
import com.bcb.model.Court;
import com.bcb.model.FacilityPriceRule;
import com.bcb.model.Invoice;
import com.bcb.model.RecurringBooking;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.booking.BookingRepository;
import com.bcb.repository.booking.BookingSlotRepository;
import com.bcb.repository.booking.CourtRepository;
import com.bcb.repository.booking.CourtSlotBookingRepository;
import com.bcb.repository.booking.InvoiceRepository;
import com.bcb.repository.booking.TimeSlotRepository;
import com.bcb.repository.booking.impl.BookingRepositoryImpl;
import com.bcb.repository.booking.impl.BookingSlotRepositoryImpl;
import com.bcb.repository.booking.impl.CourtRepositoryImpl;
import com.bcb.repository.booking.impl.CourtSlotBookingRepositoryImpl;
import com.bcb.repository.booking.impl.InvoiceRepositoryImpl;
import com.bcb.repository.booking.impl.TimeSlotRepositoryImpl;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.repository.recurring.BookingSkipRepository;
import com.bcb.repository.recurring.RecurringBookingRepository;
import com.bcb.repository.recurring.RecurringPatternRepository;
import com.bcb.repository.recurring.impl.BookingSkipRepositoryImpl;
import com.bcb.repository.recurring.impl.RecurringBookingRepositoryImpl;
import com.bcb.repository.recurring.impl.RecurringPatternRepositoryImpl;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.repository.voucher.impl.VoucherRepositoryImpl;
import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
import com.bcb.service.recurring.RecurringConfirmService;
import com.bcb.service.recurring.RecurringVoucherApplyService;
import com.bcb.service.recurring.internal.RecurringPreviewCacheEntry;
import com.bcb.service.recurring.internal.RecurringPreviewStore;
import com.bcb.utils.DBContext;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transactional recurring confirm-and-pay service.
 *
 * @author AnhTN
 */
public class RecurringConfirmServiceImpl implements RecurringConfirmService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final RecurringBookingRepository recurringBookingRepo;
    private final RecurringPatternRepository recurringPatternRepo;
    private final BookingSkipRepository bookingSkipRepo;
    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;
    private final PaymentService paymentService;
    private final VoucherRepository voucherRepo;
    private final RecurringVoucherApplyService recurringVoucherApplyService;

    public RecurringConfirmServiceImpl() {
        this.recurringBookingRepo = new RecurringBookingRepositoryImpl();
        this.recurringPatternRepo = new RecurringPatternRepositoryImpl();
        this.bookingSkipRepo = new BookingSkipRepositoryImpl();
        this.bookingRepo = new BookingRepositoryImpl();
        this.bookingSlotRepo = new BookingSlotRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
        this.invoiceRepo = new InvoiceRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
        this.paymentService = new PaymentServiceImpl();
        this.voucherRepo = new VoucherRepositoryImpl();
        this.recurringVoucherApplyService = new RecurringVoucherApplyServiceImpl();
    }

    @Override
    public RecurringConfirmResponseDTO confirmAndPay(int accountId,
                                                     RecurringConfirmRequestDTO request,
                                                     HttpServletRequest httpReq) {
        if (request == null || request.getPreviewToken() == null || request.getPreviewToken().isBlank()) {
            throw new RecurringValidationException("PREVIEW_TOKEN_REQUIRED", "previewToken is required.");
        }

        RecurringPreviewCacheEntry preview = RecurringPreviewStore.get(request.getPreviewToken());
        if (preview == null) {
            throw new RecurringNotFoundException("PREVIEW_NOT_FOUND", "Preview token invalid or expired. Please preview again.");
        }

        Set<LocalDate> skipDates = parseSkipDates(request.getSkipDates(), preview.getStartDate(), preview.getEndDate());

        List<Court> courts = courtRepo.findActiveByFacilityId(preview.getFacilityId());
        Map<Integer, Court> courtMap = courts.stream().collect(Collectors.toMap(Court::getCourtId, c -> c));
        List<SingleBookingMatrixTimeSlotDTO> slots = timeSlotRepo.findByTimeRange(LocalTime.MIN, LocalTime.MAX);
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));

        Map<String, RuntimeSession> runtimeSessionMap = buildRuntimeSessionMap(preview.getSessions(), skipDates);
        applyModifiedSessions(request.getModifiedSessions(), runtimeSessionMap, courtMap, slots, preview.getFacilityId());

        if (runtimeSessionMap.isEmpty()) {
            throw new RecurringValidationException("NO_SESSIONS_REMAINING", "Cannot skip all sessions.");
        }

        for (RuntimeSession runtime : runtimeSessionMap.values()) {
            if ("CONFLICT".equals(runtime.status)) {
                throw new RecurringConflictException("CONFLICT_REMAINING",
                        "Session conflict still exists on " + runtime.sessionDate + ". Please skip or modify this session.");
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SessionSlotPrice> slotPrices = new ArrayList<>();
        for (RuntimeSession session : runtimeSessionMap.values()) {
            LocalDate date = session.sessionDate;
            for (Integer slotId : session.slotIds) {
                Court court = courtMap.get(session.courtId);
                if (court == null) {
                    throw new RecurringValidationException("COURT_NOT_FOUND",
                            "Court not found in facility: " + session.courtId);
                }
                BigDecimal price = resolveSlotPrice(preview.getFacilityId(), court.getCourtTypeId(), date, slotId, slotMap);
                totalAmount = totalAmount.add(price);

                SessionSlotPrice row = new SessionSlotPrice();
                row.sessionDate = date;
                row.courtId = session.courtId;
                row.slotId = slotId;
                row.price = price;
                slotPrices.add(row);
            }
        }

        Integer appliedVoucherId = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            RecurringVoucherApplyRequestDTO voucherReq = new RecurringVoucherApplyRequestDTO();
            voucherReq.setVoucherCode(request.getVoucherCode().trim());
            voucherReq.setFacilityId(preview.getFacilityId());
            voucherReq.setTotalAmount(totalAmount);

            RecurringVoucherApplyResponseDTO voucher = recurringVoucherApplyService.applyVoucher(accountId, voucherReq);
            appliedVoucherId = voucher.getVoucherId();
            discountAmount = voucher.getDiscountAmount() != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;
            finalAmount = voucher.getFinalAmount() != null ? voucher.getFinalAmount() : totalAmount.subtract(discountAmount);
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            RecurringBooking recurring = new RecurringBooking();
            recurring.setFacilityId(preview.getFacilityId());
            recurring.setStartDate(preview.getStartDate());
            recurring.setEndDate(preview.getEndDate());
            recurring.setStatus("ACTIVE");
            int recurringId = recurringBookingRepo.insert(conn, recurring);

            List<SingleBookingMatrixTimeSlotDTO> facilitySlots = timeSlotRepo.findByTimeRange(LocalTime.MIN, LocalTime.MAX);
            for (RecurringPatternDTO pattern : preview.getPatterns()) {
                List<Integer> patternSlotIds = convertTimeRangeToSlots(
                        parseTime(pattern.getStartTime(), "startTime"),
                        parseTime(pattern.getEndTime(), "endTime"),
                        facilitySlots);
                for (Integer slotId : patternSlotIds) {
                    recurringPatternRepo.insert(conn, recurringId, pattern.getCourtId(), pattern.getDayOfWeek(), slotId);
                }
            }

            for (LocalDate skipDate : skipDates) {
                bookingSkipRepo.insert(conn, recurringId, skipDate, "Skip by customer");
            }

            LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(VNPayConfig.EXPIRE_MINUTES);
            Booking booking = new Booking();
            booking.setRecurringId(recurringId);
            booking.setFacilityId(preview.getFacilityId());
//            booking.setBookingDate(preview.getStartDate());
            booking.setAccountId(accountId);
            booking.setBookingStatus("PENDING");
            booking.setHoldExpiredAt(holdExpiredAt);
            int bookingId = bookingRepo.insertBooking(conn, booking);

            for (SessionSlotPrice row : slotPrices) {
                BookingSlot bookingSlot = new BookingSlot();
                bookingSlot.setBookingId(bookingId);
                bookingSlot.setBookingDate(row.sessionDate);
                bookingSlot.setCourtID(row.courtId);
                bookingSlot.setSlotId(row.slotId);
                bookingSlot.setPrice(row.price);

                int bookingSlotId = bookingSlotRepo.insertBookingSlot(conn, bookingSlot);
                try {
                    courtSlotBookingRepo.insertLock(conn, row.courtId, row.sessionDate, row.slotId, bookingSlotId);
                } catch (DataAccessException e) {
                    conn.rollback();
                    throw new RecurringConflictException("SLOT_CONFLICT",
                            "Slot conflict: courtId=" + row.courtId + ", slotId=" + row.slotId
                                    + " on " + row.sessionDate);
                }
            }

            Invoice invoice = new Invoice();
            invoice.setBookingId(bookingId);
            invoice.setTotalAmount(finalAmount);
            invoice.setDepositPercent(100);
            invoice.setPaymentStatus(finalAmount.compareTo(BigDecimal.ZERO) > 0 ? "UNPAID" : "PAID");
            invoice.setPaidAmount(BigDecimal.ZERO);
            invoice.setVoucherId(appliedVoucherId);
            invoice.setDiscountAmount(discountAmount);
            int invoiceId = invoiceRepo.insertInvoice(conn, invoice);

            if (appliedVoucherId != null) {
                voucherRepo.insertVoucherUsage(conn, appliedVoucherId, accountId,
                        bookingId, invoiceId, discountAmount);
            }

            if (finalAmount.compareTo(BigDecimal.ZERO) == 0) {
                bookingRepo.updateStatus(conn, bookingId, "CONFIRMED");
            }

            conn.commit();

            RecurringConfirmResponseDTO response = new RecurringConfirmResponseDTO();
            response.setRecurringId(recurringId);
            response.setBookingId(bookingId);
            response.setInvoiceId(invoiceId);
            response.setHoldExpiredAt(holdExpiredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            response.setTotalAmount(totalAmount);
            response.setDiscountAmount(discountAmount);
            response.setFinalAmount(finalAmount);
            response.setPayAmount(finalAmount);

            if (finalAmount.compareTo(BigDecimal.ZERO) > 0) {
                PaymentCreateResult payment = paymentService.createVNPayPayment(
                        invoiceId,
                        finalAmount.longValue(),
                        "FULL",
                        "Thanh toan dat lich co dinh #" + bookingId,
                        httpReq
                );
                if (payment.isSuccess()) {
                    response.setPaymentUrl(payment.getPaymentUrl());
                    response.setTransactionCode(payment.getTransactionCode());
                }
            }
            return response;
        } catch (RecurringConflictException e) {
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new DataAccessException("Transaction failed during recurring confirm-and-pay", e);
        } catch (DataAccessException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Builds mutable runtime session map after removing skipped dates.
     */
    private Map<String, RuntimeSession> buildRuntimeSessionMap(List<RecurringPreviewSessionDTO> sessions,
                                                               Set<LocalDate> skipDates) {
        Map<String, RuntimeSession> map = new HashMap<>();
        if (sessions == null) {
            return map;
        }
        for (RecurringPreviewSessionDTO session : sessions) {
            LocalDate date = LocalDate.parse(session.getDate());
            if (skipDates.contains(date)) {
                continue;
            }

            RuntimeSession runtime = new RuntimeSession();
            runtime.sessionId = session.getSessionId();
            runtime.sessionDate = date;
            runtime.courtId = session.getCourtId();
            runtime.slotIds = new ArrayList<>(session.getSlots());
            runtime.status = session.getStatus();
            map.put(runtime.sessionId, runtime);
        }
        return map;
    }

    /**
     * Applies one-time changes for conflicted sessions from confirm request.
     */
    private void applyModifiedSessions(List<RecurringModifiedSessionDTO> modifiedSessions,
                                       Map<String, RuntimeSession> runtimeSessionMap,
                                       Map<Integer, Court> courtMap,
                                       List<SingleBookingMatrixTimeSlotDTO> slots,
                                       int facilityId) {
        if (modifiedSessions == null || modifiedSessions.isEmpty()) {
            return;
        }

        Set<String> touchedSessionIds = new HashSet<>();
        for (RecurringModifiedSessionDTO modified : modifiedSessions) {
            if (modified.getSessionId() == null || modified.getSessionId().isBlank()) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION", "sessionId is required.");
            }
            if (!touchedSessionIds.add(modified.getSessionId())) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION",
                        "Duplicate modified session: " + modified.getSessionId());
            }

            RuntimeSession runtime = runtimeSessionMap.get(modified.getSessionId());
            if (runtime == null) {
                throw new RecurringValidationException("SESSION_NOT_FOUND",
                        "Session not found: " + modified.getSessionId());
            }
            if (!"CONFLICT".equals(runtime.status)) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION",
                        "Only conflict sessions can be modified: " + modified.getSessionId());
            }

            if (modified.getNewCourtId() == null || !courtMap.containsKey(modified.getNewCourtId())) {
                throw new RecurringValidationException("COURT_NOT_FOUND",
                        "New court not found in facility " + facilityId + ": " + modified.getNewCourtId());
            }

            LocalTime newStart = parseTime(modified.getNewStartTime(), "newStartTime");
            LocalTime newEnd = parseTime(modified.getNewEndTime(), "newEndTime");
            if (!newEnd.isAfter(newStart)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "newEndTime must be after newStartTime.");
            }

            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(newStart, newEnd);
            if (minutes < 60) {
                throw new RecurringValidationException("MIN_DURATION", "Modified session must be at least 60 minutes.");
            }

            List<Integer> newSlotIds = convertTimeRangeToSlots(newStart, newEnd, slots);
            runtime.courtId = modified.getNewCourtId();
            runtime.slotIds = newSlotIds;
            runtime.status = "MODIFIED";
        }
    }

    private Set<LocalDate> parseSkipDates(List<String> values, LocalDate startDate, LocalDate endDate) {
        if (values == null || values.isEmpty()) {
            return new HashSet<>();
        }
        Set<LocalDate> parsed = new LinkedHashSet<>();
        for (String value : values) {
            LocalDate skipDate;
            try {
                skipDate = LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                throw new RecurringValidationException("INVALID_SKIP_DATE",
                        "Invalid skip date format: " + value + ". Expected YYYY-MM-DD.");
            }
            if (skipDate.isBefore(startDate) || skipDate.isAfter(endDate)) {
                throw new RecurringValidationException("INVALID_SKIP_DATE",
                        "Skip date outside recurring range: " + value);
            }
            parsed.add(skipDate);
        }
        return parsed;
    }

    private List<Integer> convertTimeRangeToSlots(LocalTime start,
                                                  LocalTime end,
                                                  List<SingleBookingMatrixTimeSlotDTO> slots) {
        List<Integer> matched = new ArrayList<>();
        LocalTime firstStart = null;
        LocalTime lastEnd = null;
        for (SingleBookingMatrixTimeSlotDTO slot : slots) {
            LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
            LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);
            if (!slotStart.isBefore(start) && !slotEnd.isAfter(end)) {
                matched.add(slot.getSlotId());
                if (firstStart == null) {
                    firstStart = slotStart;
                }
                lastEnd = slotEnd;
            }
        }
        if (matched.isEmpty()) {
            throw new RecurringValidationException("INVALID_TIME_RANGE", "Pattern time range does not map to slots.");
        }
        if (!start.equals(firstStart) || !end.equals(lastEnd)) {
            throw new RecurringValidationException("INVALID_TIME_RANGE",
                    "Pattern time range must align with 30-minute slot boundaries.");
        }
        return matched;
    }

    private BigDecimal resolveSlotPrice(int facilityId,
                                        int courtTypeId,
                                        LocalDate bookingDate,
                                        int slotId,
                                        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
        if (slot == null) {
            throw new RecurringValidationException("INVALID_SLOT", "Invalid slotId=" + slotId);
        }

        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        List<FacilityPriceRule> rules =
                priceRuleRepo.findByFacilityAndCourtTypeAndDayType(facilityId, courtTypeId, dayType);

        LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
        LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);

        List<FacilityPriceRule> matching = rules.stream()
                .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            throw new RecurringValidationException("PRICE_RULE_MISSING",
                    "No price rule for slot " + slot.getStartTime() + "-" + slot.getEndTime());
        }
        if (matching.size() > 1) {
            throw new RecurringValidationException("PRICE_RULE_OVERLAPPED",
                    "Multiple price rules match slot " + slot.getStartTime() + "-" + slot.getEndTime());
        }
        return matching.get(0).getPrice();
    }

    private LocalTime parseTime(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " is required.");
        }
        try {
            return LocalTime.parse(value, TF);
        } catch (DateTimeParseException e) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " format invalid. Expected HH:mm.");
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // ignored
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // ignored
            }
        }
    }

    private static class SessionSlotPrice {
        private LocalDate sessionDate;
        private Integer courtId;
        private Integer slotId;
        private BigDecimal price;
    }

    /**
     * Mutable session state used during confirm transformations.
     */
    private static class RuntimeSession {
        private String sessionId;
        private LocalDate sessionDate;
        private Integer courtId;
        private List<Integer> slotIds;
        private String status;
    }
}








