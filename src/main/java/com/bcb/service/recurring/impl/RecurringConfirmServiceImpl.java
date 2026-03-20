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
import com.bcb.model.Facility;
import com.bcb.model.FacilityPriceRule;
import com.bcb.model.Invoice;
import com.bcb.model.RecurringBooking;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.booking.BookingRepository;
import com.bcb.repository.booking.BookingSlotRepository;
import com.bcb.repository.booking.CourtRepository;
import com.bcb.repository.booking.CourtSlotBookingRepository;
import com.bcb.repository.booking.FacilityRepository;
import com.bcb.repository.booking.InvoiceRepository;
import com.bcb.repository.booking.TimeSlotRepository;
import com.bcb.repository.booking.impl.BookingRepositoryImpl;
import com.bcb.repository.booking.impl.BookingSlotRepositoryImpl;
import com.bcb.repository.booking.impl.CourtRepositoryImpl;
import com.bcb.repository.booking.impl.CourtSlotBookingRepositoryImpl;
import com.bcb.repository.booking.impl.FacilityRepositoryImpl;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transactional recurring confirm-and-pay service.
 *
 * @author AnhTN
 */
public class RecurringConfirmServiceImpl implements RecurringConfirmService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_REQUIRED_SESSIONS = 4;

    private final RecurringBookingRepository recurringBookingRepo;
    private final RecurringPatternRepository recurringPatternRepo;
    private final BookingSkipRepository bookingSkipRepo;
    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final CourtRepository courtRepo;
    private final FacilityRepository facilityRepo;
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
        this.facilityRepo = new FacilityRepositoryImpl();
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
            throw new RecurringValidationException("PREVIEW_TOKEN_REQUIRED", "Preview token là bắt buộc.");
        }

        RecurringPreviewCacheEntry preview = RecurringPreviewStore.get(request.getPreviewToken());
        if (preview == null) {
            throw new RecurringNotFoundException("PREVIEW_NOT_FOUND", "Preview token không hợp lệ hoặc đã hết hạn. Vui lòng chọn khung giờ lại.");
        }

        Set<LocalDate> skipDates = parseSkipDates(request.getSkipDates(), preview.getStartDate(), preview.getEndDate());

        Facility facility = facilityRepo.findActiveById(preview.getFacilityId())
                .orElseThrow(() -> new RecurringNotFoundException("NOT_FOUND", "Không tìm thấy cơ sở với id=" + preview.getFacilityId()));
        LocalTime openTime = facility.getOpenTime() != null ? facility.getOpenTime() : LocalTime.of(6, 0);
        LocalTime closeTime = facility.getCloseTime() != null ? facility.getCloseTime() : LocalTime.of(22, 0);

        List<Court> courts = courtRepo.findActiveByFacilityId(preview.getFacilityId());
        Map<Integer, Court> courtMap = courts.stream().collect(Collectors.toMap(Court::getCourtId, c -> c));
        List<SingleBookingMatrixTimeSlotDTO> slots = timeSlotRepo.findByTimeRange(openTime, closeTime);
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));
        Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache = new HashMap<>();

        Map<String, RuntimeSession> runtimeSessionMap = buildRuntimeSessionMap(preview.getSessions(), skipDates);
        applyModifiedSessions(request.getModifiedSessions(), runtimeSessionMap, courtMap, slots,
                preview.getFacilityId(), openTime, closeTime);

        if (runtimeSessionMap.isEmpty()) {
            throw new RecurringValidationException("NO_SESSIONS_REMAINING", "Không thể bỏ qua tất cả các buổi.");
        }

        validateMinimumSessions(preview.getSessions(), skipDates, runtimeSessionMap.size());

        for (RuntimeSession runtime : runtimeSessionMap.values()) {
            if ("CONFLICT".equals(runtime.status)) {
                throw new RecurringConflictException("CONFLICT_REMAINING", "Xung đột vẫn tồn tại vào ngày " + runtime.sessionDate + ". Vui lòng bỏ qua hoặc chỉnh sửa buổi này.");
            }
        }

        // Re-check at confirm time to catch newly-booked or schedule-exception-blocked slots.
        validateRuntimeAvailability(preview.getFacilityId(), runtimeSessionMap);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SessionSlotPrice> slotPrices = new ArrayList<>();
        for (RuntimeSession session : runtimeSessionMap.values()) {
            LocalDate date = session.sessionDate;
            for (Integer slotId : session.slotIds) {
                Court court = courtMap.get(session.courtId);
                if (court == null) {
                    throw new RecurringValidationException("COURT_NOT_FOUND", "Không tìm thấy sân trong cơ sở: " + session.courtId);
                }
                BigDecimal price = resolveSlotPrice(preview.getFacilityId(), court.getCourtTypeId(), date, slotId,
                        slotMap, priceRuleCache);
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

            List<SingleBookingMatrixTimeSlotDTO> facilitySlots = slots;
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
                    throw new RecurringConflictException("SLOT_CONFLICT", "Xung đột slot: courtId=" + row.courtId + ", slotId=" + row.slotId + " vào ngày " + row.sessionDate);
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
            throw new DataAccessException("Giao dịch thất bại trong quá trình xác nhận và thanh toán đặt lịch cố định", e);
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
                                       int facilityId,
                                       LocalTime openTime,
                                       LocalTime closeTime) {
        if (modifiedSessions == null || modifiedSessions.isEmpty()) {
            return;
        }

        Set<String> touchedSessionIds = new HashSet<>();
        for (RecurringModifiedSessionDTO modified : modifiedSessions) {
            if (modified.getSessionId() == null || modified.getSessionId().isBlank()) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION", "sessionId là bắt buộc.");
            }
            if (!touchedSessionIds.add(modified.getSessionId())) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION", "Buổi chỉnh sửa bị trùng: " + modified.getSessionId());
            }

            RuntimeSession runtime = runtimeSessionMap.get(modified.getSessionId());
            if (runtime == null) {
                throw new RecurringValidationException("SESSION_NOT_FOUND", "Không tìm thấy buổi: " + modified.getSessionId());
            }
            if (!"CONFLICT".equals(runtime.status)) {
                throw new RecurringValidationException("INVALID_MODIFIED_SESSION", "Chỉ các buổi xung đột mới được chỉnh sửa: " + modified.getSessionId());
            }

            if (modified.getNewCourtId() == null || !courtMap.containsKey(modified.getNewCourtId())) {
                throw new RecurringValidationException("COURT_NOT_FOUND", "Không tìm thấy sân mới trong cơ sở " + facilityId + ": " + modified.getNewCourtId());
            }

            LocalTime newStart = parseTime(modified.getNewStartTime(), "newStartTime");
            LocalTime newEnd = parseTime(modified.getNewEndTime(), "newEndTime");
            if (!newEnd.isAfter(newStart)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "newEndTime phải sau newStartTime.");
            }
            if (newStart.isBefore(openTime) || newEnd.isAfter(closeTime)) {
                throw new RecurringValidationException("OUT_OF_OPERATING_HOURS", "Thời gian buổi chỉnh sửa nằm ngoài giờ hoạt động của cơ sở.");
            }

            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(newStart, newEnd);
            if (minutes < 60) {
                throw new RecurringValidationException("MIN_DURATION", "Buổi chỉnh sửa phải tối thiểu 60 phút.");
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
                throw new RecurringValidationException("INVALID_SKIP_DATE", "Định dạng ngày bỏ qua không hợp lệ: " + value + ". Định dạng mong đợi YYYY-MM-DD.");
            }
            if (skipDate.isBefore(startDate) || skipDate.isAfter(endDate)) {
                throw new RecurringValidationException("INVALID_SKIP_DATE", "Ngày bỏ qua nằm ngoài phạm vi đặt lịch: " + value);
            }
            parsed.add(skipDate);
        }
        return parsed;
    }

    private void validateRuntimeAvailability(int facilityId, Map<String, RuntimeSession> runtimeSessionMap) {
        if (runtimeSessionMap == null || runtimeSessionMap.isEmpty()) {
            return;
        }

        Map<LocalDate, List<RuntimeSession>> sessionsByDate = runtimeSessionMap.values().stream()
                .collect(Collectors.groupingBy(s -> s.sessionDate));

        for (Map.Entry<LocalDate, List<RuntimeSession>> entry : sessionsByDate.entrySet()) {
            LocalDate bookingDate = entry.getKey();
            Map<Integer, List<Integer>> unavailableByCourt = courtSlotBookingRepo.findUnavailableSlots(facilityId, bookingDate);

            for (RuntimeSession session : entry.getValue()) {
                List<Integer> conflictSlots = extractConflictSlots(unavailableByCourt, session.courtId, session.slotIds);
                if (!conflictSlots.isEmpty()) {
                    throw new RecurringConflictException(
                            "SLOT_CONFLICT",
                            "Khung giờ không còn khả dụng: courtId=" + session.courtId
                                    + ", slots=" + conflictSlots
                                    + ", date=" + bookingDate
                    );
                }
            }
        }
    }

    private List<Integer> extractConflictSlots(Map<Integer, List<Integer>> unavailableByCourt,
                                               Integer courtId,
                                               List<Integer> requestedSlots) {
        if (courtId == null || requestedSlots == null || requestedSlots.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> unavailable = unavailableByCourt.get(courtId);
        if (unavailable == null || unavailable.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> unavailableSet = new HashSet<>(unavailable);
        List<Integer> conflict = new ArrayList<>();
        for (Integer slotId : requestedSlots) {
            if (unavailableSet.contains(slotId)) {
                conflict.add(slotId);
            }
        }
        return conflict;
    }

    private void validateMinimumSessions(List<RecurringPreviewSessionDTO> previewSessions,
                                         Set<LocalDate> skipDates,
                                         int remainingSessions) {
        if (remainingSessions >= MIN_REQUIRED_SESSIONS) {
            return;
        }

        int skippedConflictSessions = 0;
        int skippedNonConflictSessions = 0;
        if (previewSessions != null && !skipDates.isEmpty()) {
            for (RecurringPreviewSessionDTO session : previewSessions) {
                LocalDate sessionDate = LocalDate.parse(session.getDate());
                if (!skipDates.contains(sessionDate)) {
                    continue;
                }
                if ("CONFLICT".equals(session.getStatus())) {
                    skippedConflictSessions++;
                } else {
                    skippedNonConflictSessions++;
                }
            }
        }

        boolean skipOnlyConflict = !skipDates.isEmpty() && skippedNonConflictSessions == 0 && skippedConflictSessions > 0;
        if (skipOnlyConflict) {
            return;
        }

        throw new RecurringValidationException(
                "MIN_SESSIONS_REQUIRED",
                "Đặt lịch cố định phải có ít nhất " + MIN_REQUIRED_SESSIONS
                        + " buổi. Hiện tại bạn chỉ còn " + remainingSessions + " buổi."
        );
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
            throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian mẫu không khớp với slot.");
        }
        if (!start.equals(firstStart) || !end.equals(lastEnd)) {
            throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian mẫu phải khớp với ranh giới slot 30 phút.");
        }
        return matched;
    }

    private BigDecimal resolveSlotPrice(int facilityId,
                                        int courtTypeId,
                                        LocalDate bookingDate,
                                        int slotId,
                                        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap,
                                        Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache) {
        SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
        if (slot == null) {
            throw new RecurringValidationException("INVALID_SLOT", "slotId không hợp lệ=" + slotId);
        }

        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        PriceRuleCacheKey cacheKey = new PriceRuleCacheKey(facilityId, courtTypeId, dayType);
        List<FacilityPriceRule> rules = priceRuleCache.computeIfAbsent(
                cacheKey,
                k -> priceRuleRepo.findByFacilityAndCourtTypeAndDayType(k.facilityId, k.courtTypeId, k.dayType)
        );

        LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
        LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);

        List<FacilityPriceRule> matching = rules.stream()
                .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            throw new RecurringValidationException("PRICE_RULE_MISSING", "Không có quy tắc giá cho slot " + slot.getStartTime() + "-" + slot.getEndTime());
        }
        if (matching.size() > 1) {
            throw new RecurringValidationException("PRICE_RULE_OVERLAPPED", "Nhiều quy tắc giá khớp với slot " + slot.getStartTime() + "-" + slot.getEndTime());
        }
        return matching.get(0).getPrice();
    }

    private LocalTime parseTime(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " là bắt buộc.");
        }
        try {
            return LocalTime.parse(value, TF);
        } catch (DateTimeParseException e) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " định dạng không hợp lệ. Định dạng mong đợi HH:mm.");
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

    private static final class PriceRuleCacheKey {
        private final int facilityId;
        private final int courtTypeId;
        private final String dayType;

        private PriceRuleCacheKey(int facilityId, int courtTypeId, String dayType) {
            this.facilityId = facilityId;
            this.courtTypeId = courtTypeId;
            this.dayType = dayType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PriceRuleCacheKey that)) {
                return false;
            }
            return facilityId == that.facilityId
                    && courtTypeId == that.courtTypeId
                    && Objects.equals(dayType, that.dayType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(facilityId, courtTypeId, dayType);
        }
    }
}
