package com.bcb.service.recurring.impl;

import com.bcb.dto.recurring.RecurringPatternDTO;
import com.bcb.dto.recurring.RecurringPreviewRequestDTO;
import com.bcb.dto.recurring.RecurringPreviewResponseDTO;
import com.bcb.dto.recurring.RecurringPreviewSessionDTO;
import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.exception.recurring.RecurringNotFoundException;
import com.bcb.exception.recurring.RecurringValidationException;
import com.bcb.model.Court;
import com.bcb.model.Facility;
import com.bcb.model.FacilityPriceRule;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.booking.CourtRepository;
import com.bcb.repository.booking.CourtSlotBookingRepository;
import com.bcb.repository.booking.FacilityRepository;
import com.bcb.repository.booking.TimeSlotRepository;
import com.bcb.repository.booking.impl.CourtRepositoryImpl;
import com.bcb.repository.booking.impl.CourtSlotBookingRepositoryImpl;
import com.bcb.repository.booking.impl.FacilityRepositoryImpl;
import com.bcb.repository.booking.impl.TimeSlotRepositoryImpl;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.service.recurring.RecurringPreviewService;
import com.bcb.service.recurring.internal.RecurringPreviewCacheEntry;
import com.bcb.service.recurring.internal.RecurringPreviewStore;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds recurring preview from weekly patterns.
 *
 * @author AnhTN
 */
public class RecurringPreviewServiceImpl implements RecurringPreviewService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final FacilityRepository facilityRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;

    public RecurringPreviewServiceImpl() {
        this.facilityRepo = new FacilityRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
    }

    @Override
    public RecurringPreviewResponseDTO preview(RecurringPreviewRequestDTO request) {
        if (request == null) {
            throw new RecurringValidationException("VALIDATION_ERROR", "Request body is required.");
        }

        LocalDate startDate = parseDate(request.getStartDate(), "startDate");
        LocalDate endDate = parseDate(request.getEndDate(), "endDate");

        if (request.getFacilityId() == null) {
            throw new RecurringValidationException("VALIDATION_ERROR", "facilityId is required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new RecurringValidationException("INVALID_DATE_RANGE", "startDate must be before or equal to endDate.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new RecurringValidationException("PAST_DATE", "startDate cannot be in the past.");
        }
        if (ChronoUnit.WEEKS.between(startDate, endDate) > 26) {
            throw new RecurringValidationException("DURATION_TOO_LONG", "Recurring booking cannot exceed 6 months.");
        }

        if (request.getPatterns() == null || request.getPatterns().isEmpty()) {
            throw new RecurringValidationException("PATTERNS_REQUIRED", "At least one pattern is required.");
        }

        validateUniqueDays(request.getPatterns());

        Facility facility = facilityRepo.findActiveById(request.getFacilityId())
                .orElseThrow(() -> new RecurringNotFoundException("NOT_FOUND",
                        "Facility not found with id=" + request.getFacilityId()));

        List<Court> courts = courtRepo.findActiveByFacilityId(request.getFacilityId());
        if (courts.isEmpty()) {
            throw new RecurringValidationException("COURTS_EMPTY", "No active courts found for this facility.");
        }
        Map<Integer, Court> courtMap = courts.stream().collect(Collectors.toMap(Court::getCourtId, c -> c));

        List<SingleBookingMatrixTimeSlotDTO> slots =
                timeSlotRepo.findByTimeRange(facility.getOpenTime(), facility.getCloseTime());
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));

        List<PatternPrepared> preparedPatterns = preparePatterns(request.getPatterns(), facility, courtMap, slots);

        List<RecurringPreviewSessionDTO> sessions = new ArrayList<>();
        int conflictSessions = 0;
        int availableSessions = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = toPlanDayOfWeek(current);
            Map<Integer, List<Integer>> bookedByCourt = courtSlotBookingRepo.findBookedSlots(request.getFacilityId(), current);

            for (PatternPrepared prepared : preparedPatterns) {
                if (prepared.dayOfWeek != dayOfWeek) {
                    continue;
                }

                BigDecimal sessionPrice = calculateSessionPrice(
                        request.getFacilityId(),
                        prepared.court.getCourtTypeId(),
                        current,
                        prepared.slotIds,
                        slotMap
                );

                boolean hasConflict = hasConflict(bookedByCourt, prepared.court.getCourtId(), prepared.slotIds);

                RecurringPreviewSessionDTO session = new RecurringPreviewSessionDTO();
                session.setSessionId("temp-" + UUID.randomUUID());
                session.setDate(current.toString());
                session.setDayOfWeek(dayOfWeek);
                session.setCourtId(prepared.court.getCourtId());
                session.setCourtName(prepared.court.getCourtName());
                session.setStartTime(prepared.pattern.getStartTime());
                session.setEndTime(prepared.pattern.getEndTime());
                session.setSlots(prepared.slotIds);
                session.setPrice(sessionPrice);
                session.setStatus(hasConflict ? "CONFLICT" : "AVAILABLE");
                sessions.add(session);

                if (hasConflict) {
                    conflictSessions++;
                } else {
                    availableSessions++;
                    totalAmount = totalAmount.add(sessionPrice);
                }
            }
            current = current.plusDays(1);
        }

        RecurringPreviewCacheEntry cacheEntry = new RecurringPreviewCacheEntry();
        cacheEntry.setFacilityId(request.getFacilityId());
        cacheEntry.setStartDate(startDate);
        cacheEntry.setEndDate(endDate);
        cacheEntry.setPatterns(request.getPatterns());
        cacheEntry.setSessions(sessions);
        cacheEntry.setTotalAmount(totalAmount);
        String token = RecurringPreviewStore.put(cacheEntry);

        RecurringPreviewResponseDTO response = new RecurringPreviewResponseDTO();
        response.setPreviewToken(token);
        response.setTotalSessions(sessions.size());
        response.setAvailableSessions(availableSessions);
        response.setConflictSessions(conflictSessions);
        response.setTotalAmount(totalAmount);
        response.setSessions(sessions);
        return response;
    }

    private void validateUniqueDays(List<RecurringPatternDTO> patterns) {
        Set<Integer> usedDays = new HashSet<>();
        for (RecurringPatternDTO pattern : patterns) {
            if (pattern.getDayOfWeek() == null || pattern.getDayOfWeek() < 1 || pattern.getDayOfWeek() > 7) {
                throw new RecurringValidationException("INVALID_DAY_OF_WEEK",
                        "dayOfWeek must be between 1 (Sun) and 7 (Sat).");
            }
            if (!usedDays.add(pattern.getDayOfWeek())) {
                throw new RecurringValidationException("DUPLICATE_DAY_PATTERN",
                        "Mỗi thứ trong tuần chỉ được cấu hình một khung giờ trong cùng recurring booking.");
            }
        }
    }

    private List<PatternPrepared> preparePatterns(List<RecurringPatternDTO> patterns,
                                                  Facility facility,
                                                  Map<Integer, Court> courtMap,
                                                  List<SingleBookingMatrixTimeSlotDTO> slots) {
        List<PatternPrepared> prepared = new ArrayList<>();
        for (RecurringPatternDTO pattern : patterns) {
            if (pattern.getCourtId() == null) {
                throw new RecurringValidationException("VALIDATION_ERROR", "courtId is required in patterns.");
            }
            if (pattern.getStartTime() == null || pattern.getEndTime() == null) {
                throw new RecurringValidationException("VALIDATION_ERROR", "startTime/endTime are required in patterns.");
            }

            Court court = courtMap.get(pattern.getCourtId());
            if (court == null) {
                throw new RecurringValidationException("COURT_NOT_FOUND",
                        "Court not found in this facility: " + pattern.getCourtId());
            }

            LocalTime start = parseTime(pattern.getStartTime(), "startTime");
            LocalTime end = parseTime(pattern.getEndTime(), "endTime");
            if (!end.isAfter(start)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "endTime must be after startTime.");
            }
            long minutes = ChronoUnit.MINUTES.between(start, end);
            if (minutes < 60) {
                throw new RecurringValidationException("MIN_DURATION", "Each session must be at least 60 minutes.");
            }
            if (start.isBefore(facility.getOpenTime()) || end.isAfter(facility.getCloseTime())) {
                throw new RecurringValidationException("OUT_OF_OPERATING_HOURS", "Pattern time is outside facility operating hours.");
            }

            List<Integer> slotIds = convertTimeRangeToSlots(start, end, slots);
            if (slotIds.isEmpty()) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "Time range does not match configured slots.");
            }

            PatternPrepared item = new PatternPrepared();
            item.pattern = pattern;
            item.dayOfWeek = pattern.getDayOfWeek();
            item.court = court;
            item.slotIds = slotIds;
            prepared.add(item);
        }
        return prepared;
    }

    private List<Integer> convertTimeRangeToSlots(LocalTime start,
                                                  LocalTime end,
                                                  List<SingleBookingMatrixTimeSlotDTO> slots) {
        List<Integer> matched = new ArrayList<>();
        LocalTime firstStart = null;
        LocalTime lastEnd = null;
        for (SingleBookingMatrixTimeSlotDTO slot : slots) {
            LocalTime slotStart = parseTime(slot.getStartTime(), "slot.startTime");
            LocalTime slotEnd = parseTime(slot.getEndTime(), "slot.endTime");
            if (!slotStart.isBefore(start) && !slotEnd.isAfter(end)) {
                matched.add(slot.getSlotId());
                if (firstStart == null) {
                    firstStart = slotStart;
                }
                lastEnd = slotEnd;
            }
        }

        if (matched.isEmpty()) {
            return matched;
        }
        if (!start.equals(firstStart) || !end.equals(lastEnd)) {
            throw new RecurringValidationException("INVALID_TIME_RANGE",
                    "Time range must align with 30-minute slot boundaries.");
        }

        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = new HashMap<>();
        for (SingleBookingMatrixTimeSlotDTO slot : slots) {
            slotMap.put(slot.getSlotId(), slot);
        }

        for (int i = 1; i < matched.size(); i++) {
            LocalTime prevEnd = parseTime(slotMap.get(matched.get(i - 1)).getEndTime(), "slot.endTime");
            LocalTime currStart = parseTime(slotMap.get(matched.get(i)).getStartTime(), "slot.startTime");
            if (!prevEnd.equals(currStart)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE",
                        "Time range must map to contiguous slots.");
            }
        }
        return matched;
    }

    private BigDecimal calculateSessionPrice(int facilityId,
                                             int courtTypeId,
                                             LocalDate bookingDate,
                                             List<Integer> slotIds,
                                             Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        List<FacilityPriceRule> rules =
                priceRuleRepo.findByFacilityAndCourtTypeAndDayType(facilityId, courtTypeId, dayType);

        BigDecimal total = BigDecimal.ZERO;
        for (Integer slotId : slotIds) {
            SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
            if (slot == null) {
                throw new RecurringValidationException("INVALID_SLOT", "Slot not found: " + slotId);
            }
            LocalTime slotStart = parseTime(slot.getStartTime(), "slot.startTime");
            LocalTime slotEnd = parseTime(slot.getEndTime(), "slot.endTime");

            List<FacilityPriceRule> matching = rules.stream()
                    .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                    .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                    .collect(Collectors.toList());

            if (matching.isEmpty()) {
                throw new RecurringValidationException("PRICE_RULE_MISSING",
                        "No price rule found for slot " + slot.getStartTime() + "-" + slot.getEndTime());
            }
            if (matching.size() > 1) {
                throw new RecurringValidationException("PRICE_RULE_OVERLAPPED",
                        "Multiple price rules match slot " + slot.getStartTime() + "-" + slot.getEndTime());
            }
            total = total.add(matching.get(0).getPrice());
        }
        return total;
    }

    private boolean hasConflict(Map<Integer, List<Integer>> bookedByCourt, int courtId, List<Integer> requestedSlots) {
        List<Integer> booked = bookedByCourt.get(courtId);
        if (booked == null || booked.isEmpty()) {
            return false;
        }
        Set<Integer> bookedSet = new HashSet<>(booked);
        for (Integer slotId : requestedSlots) {
            if (bookedSet.contains(slotId)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseDate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " is required.");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new RecurringValidationException("VALIDATION_ERROR",
                    field + " format invalid. Expected YYYY-MM-DD.");
        }
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

    private int toPlanDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7 + 1;
    }

    private static class PatternPrepared {
        private RecurringPatternDTO pattern;
        private int dayOfWeek;
        private Court court;
        private List<Integer> slotIds;
    }
}


