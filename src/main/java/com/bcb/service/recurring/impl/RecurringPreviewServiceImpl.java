package com.bcb.service.recurring.impl;

import com.bcb.dto.recurring.RecurringPatternDTO;
import com.bcb.dto.recurring.RecurringPreviewRequestDTO;
import com.bcb.dto.recurring.RecurringPreviewResponseDTO;
import com.bcb.dto.recurring.RecurringSessionSuggestionDTO;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builds recurring preview from weekly patterns.
 *
 * @author AnhTN
 */
public class RecurringPreviewServiceImpl implements RecurringPreviewService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_REQUIRED_SESSIONS = 4;
    private static final int MAX_SUGGESTIONS_PER_CONFLICT = 6;

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
            throw new RecurringValidationException("VALIDATION_ERROR", "Yêu cầu dữ liệu request body là bắt buộc.");
        }

        LocalDate startDate = parseDate(request.getStartDate(), "startDate");
        LocalDate endDate = parseDate(request.getEndDate(), "endDate");

        if (request.getFacilityId() == null) {
            throw new RecurringValidationException("VALIDATION_ERROR", "facilityId là bắt buộc.");
        }
        if (startDate.isAfter(endDate)) {
            throw new RecurringValidationException("INVALID_DATE_RANGE", "startDate phải trước hoặc bằng endDate.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new RecurringValidationException("PAST_DATE", "startDate không được ở trong quá khứ.");
        }
        if (ChronoUnit.WEEKS.between(startDate, endDate) > 26) {
            throw new RecurringValidationException("DURATION_TOO_LONG", "Đặt lịch cố định không được vượt quá 6 tháng.");
        }

        if (request.getPatterns() == null || request.getPatterns().isEmpty()) {
            throw new RecurringValidationException("PATTERNS_REQUIRED", "Cần ít nhất một mẫu lặp lại.");
        }

        validateUniqueDays(request.getPatterns());

        Facility facility = facilityRepo.findActiveById(request.getFacilityId())
                .orElseThrow(() -> new RecurringNotFoundException("NOT_FOUND", "Không tìm thấy cơ sở với id=" + request.getFacilityId()));

        List<Court> courts = courtRepo.findActiveByFacilityId(request.getFacilityId());
        if (courts.isEmpty()) {
            throw new RecurringValidationException("COURTS_EMPTY", "Không tìm thấy sân hoạt động nào cho cơ sở này.");
        }
        Map<Integer, Court> courtMap = courts.stream().collect(Collectors.toMap(Court::getCourtId, c -> c));

        List<SingleBookingMatrixTimeSlotDTO> slots =
                timeSlotRepo.findByTimeRange(facility.getOpenTime(), facility.getCloseTime());
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));
        Map<Integer, Integer> slotIndexMap = buildSlotIndexMap(slots);

        // Request-scope caches to reduce repeated price-rule reads and suggestion price recompute.
        Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache = new HashMap<>();
        Map<String, BigDecimal> suggestionPriceCache = new HashMap<>();

        List<PatternPrepared> preparedPatterns = preparePatterns(request.getPatterns(), facility, courtMap, slots);
        validateNoPastTimeWhenStartDateIsToday(startDate, preparedPatterns, slotMap);

        List<RecurringPreviewSessionDTO> sessions = new ArrayList<>();
        int conflictSessions = 0;
        int availableSessions = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = toPlanDayOfWeek(current);
            Map<Integer, List<Integer>> unavailableByCourt = courtSlotBookingRepo.findUnavailableSlots(request.getFacilityId(), current);

            for (PatternPrepared prepared : preparedPatterns) {
                if (prepared.dayOfWeek != dayOfWeek) {
                    continue;
                }

                BigDecimal sessionPrice = calculateSessionPrice(
                        request.getFacilityId(),
                        prepared.court.getCourtTypeId(),
                        current,
                        prepared.slotIds,
                        slotMap,
                        priceRuleCache
                );

                List<Integer> conflictSlots = extractConflictSlots(unavailableByCourt, prepared.court.getCourtId(), prepared.slotIds);
                boolean hasConflict = !conflictSlots.isEmpty();

                RecurringPreviewSessionDTO session = new RecurringPreviewSessionDTO();
                session.setSessionId("temp-" + UUID.randomUUID());
                session.setDate(current.toString());
                session.setDayOfWeek(dayOfWeek);
                session.setCourtId(prepared.court.getCourtId());
                session.setCourtName(prepared.court.getCourtName());
                session.setStartTime(prepared.pattern.getStartTime());
                session.setEndTime(prepared.pattern.getEndTime());
                session.setSlots(prepared.slotIds);
                session.setConflictSlots(conflictSlots);
                session.setPrice(sessionPrice);
                session.setStatus(hasConflict ? "CONFLICT" : "AVAILABLE");
                if (hasConflict) {
                    session.setSuggestions(buildSuggestions(
                            request.getFacilityId(),
                            current,
                            prepared,
                            courts,
                            unavailableByCourt,
                            slots,
                            slotMap,
                            slotIndexMap,
                            priceRuleCache,
                            suggestionPriceCache
                    ));
                } else {
                    session.setSuggestions(Collections.emptyList());
                }
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
        response.setMinRequiredSessions(MIN_REQUIRED_SESSIONS);
        response.setTotalAmount(totalAmount);
        response.setSessions(sessions);
        return response;
    }

    private void validateUniqueDays(List<RecurringPatternDTO> patterns) {
        Set<Integer> usedDays = new HashSet<>();
        for (RecurringPatternDTO pattern : patterns) {
            if (pattern.getDayOfWeek() == null || pattern.getDayOfWeek() < 1 || pattern.getDayOfWeek() > 7) {
                throw new RecurringValidationException("INVALID_DAY_OF_WEEK", "dayOfWeek phải từ 1 (Chủ nhật) đến 7 (Thứ bảy).");
            }
            if (!usedDays.add(pattern.getDayOfWeek())) {
                throw new RecurringValidationException("DUPLICATE_DAY_PATTERN", "Mỗi thứ trong tuần chỉ được cấu hình một khung giờ trong cùng recurring booking.");
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
                throw new RecurringValidationException("VALIDATION_ERROR", "courtId là bắt buộc trong patterns.");
            }
            if (pattern.getStartTime() == null || pattern.getEndTime() == null) {
                throw new RecurringValidationException("VALIDATION_ERROR", "startTime/endTime là bắt buộc trong patterns.");
            }

            Court court = courtMap.get(pattern.getCourtId());
            if (court == null) {
                throw new RecurringValidationException("COURT_NOT_FOUND", "Không tìm thấy sân trong cơ sở này: " + pattern.getCourtId());
            }

            LocalTime start = parseTime(pattern.getStartTime(), "startTime");
            LocalTime end = parseTime(pattern.getEndTime(), "endTime");
            if (!end.isAfter(start)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "endTime phải sau startTime.");
            }
            long minutes = ChronoUnit.MINUTES.between(start, end);
            if (minutes < 60) {
                throw new RecurringValidationException("MIN_DURATION", "Mỗi buổi phải tối thiểu 60 phút.");
            }
            if (start.isBefore(facility.getOpenTime()) || end.isAfter(facility.getCloseTime())) {
                throw new RecurringValidationException("OUT_OF_OPERATING_HOURS", "Khung giờ mẫu nằm ngoài giờ hoạt động của cơ sở.");
            }

            List<Integer> slotIds = convertTimeRangeToSlots(start, end, slots);
            if (slotIds.isEmpty()) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian không khớp với các slot đã cấu hình.");
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
            throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian phải khớp với ranh giới slot 30 phút.");
        }

        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = new HashMap<>();
        for (SingleBookingMatrixTimeSlotDTO slot : slots) {
            slotMap.put(slot.getSlotId(), slot);
        }

        for (int i = 1; i < matched.size(); i++) {
            LocalTime prevEnd = parseTime(slotMap.get(matched.get(i - 1)).getEndTime(), "slot.endTime");
            LocalTime currStart = parseTime(slotMap.get(matched.get(i)).getStartTime(), "slot.startTime");
            if (!prevEnd.equals(currStart)) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian phải là các slot liên tiếp.");
            }
        }
        return matched;
    }

    private BigDecimal calculateSessionPrice(int facilityId,
                                             int courtTypeId,
                                             LocalDate bookingDate,
                                             List<Integer> slotIds,
                                             Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap,
                                             Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache) {
        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        PriceRuleCacheKey cacheKey = new PriceRuleCacheKey(facilityId, courtTypeId, dayType);
        List<FacilityPriceRule> rules = priceRuleCache.computeIfAbsent(
                cacheKey,
                k -> priceRuleRepo.findByFacilityAndCourtTypeAndDayType(k.facilityId, k.courtTypeId, k.dayType)
        );

        BigDecimal total = BigDecimal.ZERO;
        for (Integer slotId : slotIds) {
            SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
            if (slot == null) {
                throw new RecurringValidationException("INVALID_SLOT", "Không tìm thấy slot: " + slotId);
            }
            LocalTime slotStart = parseTime(slot.getStartTime(), "slot.startTime");
            LocalTime slotEnd = parseTime(slot.getEndTime(), "slot.endTime");

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
            total = total.add(matching.get(0).getPrice());
        }
        return total;
    }

    private List<Integer> extractConflictSlots(Map<Integer, List<Integer>> bookedByCourt,
                                               int courtId,
                                               List<Integer> requestedSlots) {
        List<Integer> booked = bookedByCourt.get(courtId);
        if (booked == null || booked.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> bookedSet = new HashSet<>(booked);
        List<Integer> conflict = new ArrayList<>();
        for (Integer slotId : requestedSlots) {
            if (bookedSet.contains(slotId)) {
                conflict.add(slotId);
            }
        }
        return conflict;
    }

    private List<RecurringSessionSuggestionDTO> buildSuggestions(int facilityId,
                                                                 LocalDate bookingDate,
                                                                 PatternPrepared prepared,
                                                                 List<Court> courts,
                                                                 Map<Integer, List<Integer>> bookedByCourt,
                                                                 List<SingleBookingMatrixTimeSlotDTO> allSlots,
                                                                 Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap,
                                                                 Map<Integer, Integer> slotIndexMap,
                                                                 Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache,
                                                                 Map<String, BigDecimal> suggestionPriceCache) {
        if (prepared.slotIds == null || prepared.slotIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecurringSessionSuggestionDTO> suggestions = new ArrayList<>();
        Set<String> uniqueKeys = new LinkedHashSet<>();
        int slotCount = prepared.slotIds.size();

        Integer firstIndex = slotIndexMap.get(prepared.slotIds.get(0));
        if (firstIndex == null) {
            return suggestions;
        }

        // 1) Preferred: same time block on other courts.
        for (Court court : courts) {
            if (suggestions.size() >= MAX_SUGGESTIONS_PER_CONFLICT) {
                break;
            }
            if (court.getCourtId().equals(prepared.court.getCourtId())) {
                continue;
            }
            addSuggestionIfAvailable(uniqueKeys, suggestions, "OTHER_COURT_SAME_TIME",
                    court, bookingDate, prepared.slotIds, bookedByCourt, facilityId, slotMap,
                    priceRuleCache, suggestionPriceCache);
        }

        // 2) Then: same court, nearest valid time blocks with same duration.
        for (int shift = 1; suggestions.size() < MAX_SUGGESTIONS_PER_CONFLICT; shift++) {
            boolean hasCandidate = false;
            int plusStart = firstIndex + shift;
            if (plusStart + slotCount <= allSlots.size()) {
                hasCandidate = true;
                List<Integer> plusSlots = sliceSlotIds(allSlots, plusStart, slotCount);
                addSuggestionIfAvailable(uniqueKeys, suggestions, "SAME_COURT_NEAREST_TIME",
                        prepared.court, bookingDate, plusSlots, bookedByCourt, facilityId, slotMap,
                        priceRuleCache, suggestionPriceCache);
            }

            int minusStart = firstIndex - shift;
            if (minusStart >= 0) {
                hasCandidate = true;
                List<Integer> minusSlots = sliceSlotIds(allSlots, minusStart, slotCount);
                addSuggestionIfAvailable(uniqueKeys, suggestions, "SAME_COURT_NEAREST_TIME",
                        prepared.court, bookingDate, minusSlots, bookedByCourt, facilityId, slotMap,
                        priceRuleCache, suggestionPriceCache);
            }

            if (!hasCandidate) {
                break;
            }
        }

        // 3) Fallback: nearest-time blocks on other courts.
        for (Court court : courts) {
            if (suggestions.size() >= MAX_SUGGESTIONS_PER_CONFLICT) {
                break;
            }
            if (court.getCourtId().equals(prepared.court.getCourtId())) {
                continue;
            }

            for (int shift = 1; suggestions.size() < MAX_SUGGESTIONS_PER_CONFLICT; shift++) {
                boolean hasCandidate = false;
                int plusStart = firstIndex + shift;
                if (plusStart + slotCount <= allSlots.size()) {
                    hasCandidate = true;
                    List<Integer> plusSlots = sliceSlotIds(allSlots, plusStart, slotCount);
                    addSuggestionIfAvailable(uniqueKeys, suggestions, "OTHER_COURT_NEAREST_TIME",
                            court, bookingDate, plusSlots, bookedByCourt, facilityId, slotMap,
                            priceRuleCache, suggestionPriceCache);
                }

                int minusStart = firstIndex - shift;
                if (minusStart >= 0) {
                    hasCandidate = true;
                    List<Integer> minusSlots = sliceSlotIds(allSlots, minusStart, slotCount);
                    addSuggestionIfAvailable(uniqueKeys, suggestions, "OTHER_COURT_NEAREST_TIME",
                            court, bookingDate, minusSlots, bookedByCourt, facilityId, slotMap,
                            priceRuleCache, suggestionPriceCache);
                }

                if (!hasCandidate) {
                    break;
                }
            }
        }

        return suggestions;
    }

    private void addSuggestionIfAvailable(Set<String> uniqueKeys,
                                          List<RecurringSessionSuggestionDTO> suggestions,
                                          String type,
                                          Court court,
                                          LocalDate bookingDate,
                                          List<Integer> slotIds,
                                          Map<Integer, List<Integer>> bookedByCourt,
                                          int facilityId,
                                          Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap,
                                          Map<PriceRuleCacheKey, List<FacilityPriceRule>> priceRuleCache,
                                          Map<String, BigDecimal> suggestionPriceCache) {
        if (slotIds == null || slotIds.isEmpty()) {
            return;
        }
        if (!isSlotRangeAvailable(bookedByCourt, court.getCourtId(), slotIds)) {
            return;
        }
        String key = court.getCourtId() + "|" + slotIds.get(0) + "|" + slotIds.get(slotIds.size() - 1);
        if (!uniqueKeys.add(key)) {
            return;
        }

        String priceKey = buildSuggestionPriceKey(court.getCourtId(), bookingDate, slotIds);
        BigDecimal price = suggestionPriceCache.computeIfAbsent(priceKey, k -> calculateSessionPrice(
                facilityId,
                court.getCourtTypeId(),
                bookingDate,
                slotIds,
                slotMap,
                priceRuleCache
        ));

        RecurringSessionSuggestionDTO suggestion = new RecurringSessionSuggestionDTO();
        suggestion.setType(type);
        suggestion.setCourtId(court.getCourtId());
        suggestion.setCourtName(court.getCourtName());
        suggestion.setSlots(new ArrayList<>(slotIds));
        suggestion.setStartTime(slotMap.get(slotIds.get(0)).getStartTime());
        suggestion.setEndTime(slotMap.get(slotIds.get(slotIds.size() - 1)).getEndTime());
        suggestion.setPrice(price);
        suggestions.add(suggestion);
    }

    private boolean isSlotRangeAvailable(Map<Integer, List<Integer>> bookedByCourt, int courtId, List<Integer> slotIds) {
        List<Integer> booked = bookedByCourt.get(courtId);
        if (booked == null || booked.isEmpty()) {
            return true;
        }
        Set<Integer> bookedSet = new HashSet<>(booked);
        for (Integer slotId : slotIds) {
            if (bookedSet.contains(slotId)) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> sliceSlotIds(List<SingleBookingMatrixTimeSlotDTO> slots, int startIndex, int count) {
        List<Integer> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(slots.get(startIndex + i).getSlotId());
        }
        return ids;
    }

    private String buildSuggestionPriceKey(Integer courtId, LocalDate bookingDate, List<Integer> slotIds) {
        return courtId + "|" + bookingDate + "|" + slotIds;
    }

    private Map<Integer, Integer> buildSlotIndexMap(List<SingleBookingMatrixTimeSlotDTO> slots) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < slots.size(); i++) {
            map.put(slots.get(i).getSlotId(), i);
        }
        return map;
    }

    private void validateNoPastTimeWhenStartDateIsToday(LocalDate startDate,
                                                        List<PatternPrepared> preparedPatterns,
                                                        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        if (startDate == null || preparedPatterns == null || preparedPatterns.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (!startDate.equals(today)) {
            return;
        }

        int todayPlanDay = toPlanDayOfWeek(today);
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (PatternPrepared prepared : preparedPatterns) {
            if (prepared.dayOfWeek != todayPlanDay) {
                continue;
            }

            if (prepared.slotIds == null || prepared.slotIds.isEmpty()) {
                throw new RecurringValidationException("INVALID_TIME_RANGE", "Khoảng thời gian không khớp với slot đã cấu hình.");
            }

            Integer firstSlotId = prepared.slotIds.get(0);
            SingleBookingMatrixTimeSlotDTO firstSlot = slotMap.get(firstSlotId);
            if (firstSlot == null) {
                throw new RecurringValidationException("INVALID_SLOT", "Không tìm thấy slot: " + firstSlotId);
            }

            // Reject when current time has passed the end of the first slot in today's pattern.
            LocalTime firstSlotEnd = parseTime(firstSlot.getEndTime(), "slot.endTime");
            if (now.isAfter(firstSlotEnd)) {
                String message = "Khung giờ " + prepared.pattern.getStartTime() + "-" + prepared.pattern.getEndTime()
                        + " của " + toVietnameseDayName(todayPlanDay)
                        + " đã ở quá khứ so với hiện tại (" + now.format(TF) + "). Vui lòng chọn giờ bắt đầu muộn hơn.";
                throw new RecurringValidationException("PAST_TIME_SLOT", message);
            }
        }
    }

    private LocalDate parseDate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " là bắt buộc.");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new RecurringValidationException("VALIDATION_ERROR", field + " định dạng không hợp lệ. Định dạng mong đợi YYYY-MM-DD.");
        }
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

    private int toPlanDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7 + 1;
    }

    private String toVietnameseDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "Chủ nhật";
            case 2:
                return "Thứ 2";
            case 3:
                return "Thứ 3";
            case 4:
                return "Thứ 4";
            case 5:
                return "Thứ 5";
            case 6:
                return "Thứ 6";
            case 7:
                return "Thứ 7";
            default:
                return "ngày đã chọn";
        }
    }

    private static class PatternPrepared {
        private RecurringPatternDTO pattern;
        private int dayOfWeek;
        private Court court;
        private List<Integer> slotIds;
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
