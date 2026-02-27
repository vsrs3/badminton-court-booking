package com.bcb.service.singlebooking.impl;

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
import com.bcb.service.singlebooking.SingleBookingConfirmService;
import com.bcb.utils.DBContext;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;
import com.bcb.validation.singlebooking.SingleBookingSelectionValidator;

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
 * inside a single JDBC transaction. Hold duration: 10 minutes.
 *
 * @author AnhTN
 */
public class SingleBookingConfirmServiceImpl implements SingleBookingConfirmService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final int HOLD_MINUTES = 10;

    private final FacilityRepository facilityRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;
    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;
    private final InvoiceRepository invoiceRepo;

    public SingleBookingConfirmServiceImpl() {
        this.facilityRepo = new FacilityRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
        this.bookingRepo = new BookingRepositoryImpl();
        this.bookingSlotRepo = new BookingSlotRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
        this.invoiceRepo = new InvoiceRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public SingleBookingConfirmResponseDTO confirmAndPay(int accountId, SingleBookingConfirmRequestDTO request) {
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

        // --- Transaction ---
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Create Booking PENDING
            LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(HOLD_MINUTES);
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

            // 3. Create Invoice
            Invoice invoice = new Invoice();
            invoice.setBookingId(bookingId);
            invoice.setTotalAmount(totalAmount);
            invoice.setDepositPercent(request.getDepositPercent());
            invoice.setPaymentStatus("UNPAID");

            BigDecimal paidAmount = totalAmount.multiply(BigDecimal.valueOf(request.getDepositPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            invoice.setPaidAmount(BigDecimal.ZERO);

            int invoiceId = invoiceRepo.insertInvoice(conn, invoice);

            // 4. Commit
            conn.commit();

            // 5. Build response
            SingleBookingConfirmResponseDTO resp = new SingleBookingConfirmResponseDTO();
            resp.setBookingId(bookingId);
            resp.setInvoiceId(invoiceId);
            resp.setHoldExpiredAt(holdExpiredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            resp.setTotalAmount(totalAmount);
            resp.setDepositPercent(request.getDepositPercent());
            resp.setPaymentUrlStub("/payment/stub?invoiceId=" + invoiceId
                    + "&amount=" + paidAmount + "&bookingId=" + bookingId);
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
