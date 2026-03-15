package com.bcb.service.impl;

import com.bcb.dto.staff.StaffCustomerAccountDTO;
import com.bcb.dto.staff.StaffRecurringBookingOutcomeDTO;
import com.bcb.dto.staff.StaffRecurringBookingRequestDTO;
import com.bcb.repository.impl.StaffConfirmPaymentRepositoryImpl;
import com.bcb.repository.impl.StaffRecurringBookingRepositoryImpl;
import com.bcb.repository.staff.StaffConfirmPaymentRepository;
import com.bcb.repository.staff.StaffRecurringBookingRepository;
import com.bcb.service.staff.StaffRecurringBookingService;
import com.bcb.service.email.EmailQueueService;
import com.bcb.service.email.impl.EmailQueueServiceImpl;
import com.bcb.utils.DBContext;
import com.bcb.utils.staff.StaffAuthUtil;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StaffRecurringBookingServiceImpl implements StaffRecurringBookingService {

    private static final Gson GSON = new Gson();
    private static final int MAX_SUGGESTIONS = 10;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final StaffRecurringBookingRepository repository = new StaffRecurringBookingRepositoryImpl();
    private final StaffConfirmPaymentRepository paymentRepository = new StaffConfirmPaymentRepositoryImpl();
    private final EmailQueueService emailQueueService = new EmailQueueServiceImpl();

    @Override
    public StaffRecurringBookingOutcomeDTO preview(String body, int facilityId, Integer staffId) throws Exception {
        if (staffId == null) {
            return out(403, error("Staff chưa được gán"));
        }

        StaffRecurringBookingRequestDTO req = parseRequest(body);
        if (req == null) {
            return out(400, error("Dữ liệu không hợp lệ"));
        }

        ValidationResult validation = validateRequest(req);
        if (!validation.valid) {
            return out(validation.status, validation.json);
        }

        try (Connection conn = DBContext.getConnection()) {
            PlanningContext ctx = buildContext(conn, facilityId);
            Map<Integer, PatternPlan> patternMap;
            try {
                patternMap = buildPatternMap(req.getPatterns(), ctx.slotOrder);
            } catch (Exception ex) {
                return out(400, error(ex.getMessage()));
            }

            List<DatePlan> datePlans = generateDatePlans(req, patternMap);
            if (datePlans.isEmpty()) {
                return out(400, error("Không có ngày nào trong lịch định kỳ"));
            }
            List<SessionView> sessions = new ArrayList<>();
            List<ConflictView> conflicts = new ArrayList<>();
            List<String> skippedDates = new ArrayList<>();

            BigDecimal totalAmount = BigDecimal.ZERO;
            String policy = normalizePolicy(req.getConflictPolicy());

            for (DatePlan plan : datePlans) {
                if (isPastSession(plan.date, plan.slotIds, ctx)) {
                    return out(400, error(buildPastSessionMessage(plan.date)));
                }
                Map<Integer, List<Integer>> booked = repository.findBookedSlots(conn, facilityId, plan.date);
                boolean conflict = isConflict(booked, plan.courtId, plan.slotIds);

                BigDecimal sessionAmount = calcSessionAmount(plan.courtId, plan.slotIds, plan.date, ctx);

                if (!conflict) {
                    totalAmount = totalAmount.add(sessionAmount);
                    sessions.add(SessionView.ok(plan.date, plan.courtId, plan.slotIds, sessionAmount));
                    continue;
                }

                if ("SKIP".equals(policy)) {
                    skippedDates.add(plan.date.toString());
                    sessions.add(SessionView.skipped(plan.date, plan.courtId, plan.slotIds, sessionAmount));
                    conflicts.add(new ConflictView(plan, Collections.emptyList()));
                    continue;
                }

                List<SuggestionView> suggestions = buildSuggestions(conn, facilityId, plan, booked, ctx);
                sessions.add(SessionView.conflict(plan.date, plan.courtId, plan.slotIds, sessionAmount));
                conflicts.add(new ConflictView(plan, suggestions));
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("policy", policy);
            data.put("totalAmount", totalAmount);
            data.put("sessions", sessions);
            data.put("conflicts", conflicts);
            data.put("skippedDates", skippedDates);

            return out(200, success("Xem trước đặt sân định kỳ thành công", data));
        } catch (Exception e) {
            return out(500, error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @Override
    public StaffRecurringBookingOutcomeDTO confirm(String body, int facilityId, Integer staffId) throws Exception {
        if (staffId == null) {
            return out(403, error("Staff chưa được gán"));
        }

        StaffRecurringBookingRequestDTO req = parseRequest(body);
        if (req == null) {
            return out(400, error("Dữ liệu không hợp lệ"));
        }

        ValidationResult validation = validateRequest(req);
        if (!validation.valid) {
            return out(validation.status, validation.json);
        }

        String policy = normalizePolicy(req.getConflictPolicy());
        String paymentMethod = normalizePaymentMethod(req.getPaymentMethod());
        if (paymentMethod == null) {
            return out(400, error("Phương thức thanh toán không hợp lệ"));
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try {
                PlanningContext ctx = buildContext(conn, facilityId);
                Map<Integer, PatternPlan> patternMap;
                try {
                    patternMap = buildPatternMap(req.getPatterns(), ctx.slotOrder);
                } catch (Exception ex) {
                    conn.rollback();
                    return out(400, error(ex.getMessage()));
                }

                Integer guestId = null;
                if ("GUEST".equals(req.getCustomerType())) {
                    guestId = repository.insertGuest(conn, req.getGuestName(),
                            normalizePhone(req.getGuestPhone()), normalizeEmail(req.getGuestEmail()));
                }

                int bookingId = repository.insertBookingRoot(conn, facilityId, req.getAccountId(), guestId, staffId);
                int recurringId = repository.insertRecurringBooking(
                        conn, facilityId,
                        LocalDate.parse(req.getStartDate()),
                        LocalDate.parse(req.getEndDate()));
                repository.updateBookingRecurringId(conn, bookingId, recurringId);

                insertRecurringPatterns(conn, recurringId, req.getPatterns());

                List<DatePlan> datePlans = generateDatePlans(req, patternMap);
                if (datePlans.isEmpty()) {
                    conn.rollback();
                    return out(400, error("Không có ngày nào trong lịch định kỳ"));
                }
                Map<LocalDate, SelectedSession> overrides;
                try {
                    overrides = buildOverrides(req, datePlans, ctx.slotOrder, policy);
                } catch (Exception ex) {
                    conn.rollback();
                    return out(400, error(ex.getMessage()));
                }

                List<String> skippedDates = new ArrayList<>();
                List<String> bookedDates = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (DatePlan plan : datePlans) {
                    Map<Integer, List<Integer>> booked = repository.findBookedSlots(conn, facilityId, plan.date);

                    SelectedSession override = overrides.get(plan.date);
                    int courtId = override != null ? override.courtId : plan.courtId;
                    List<Integer> slotIds = override != null ? override.slotIds : plan.slotIds;

                    if (isPastSession(plan.date, slotIds, ctx)) {
                        conn.rollback();
                        return out(400, error(buildPastSessionMessage(plan.date)));
                    }

                    boolean conflict = isConflict(booked, courtId, slotIds);
                    if (conflict) {
                        if ("SKIP".equals(policy)) {
                            skippedDates.add(plan.date.toString());
                            repository.insertBookingSkip(conn, recurringId, plan.date, "Trùng lịch");
                            continue;
                        }
                        if (override == null) {
                            conn.rollback();
                            return out(409, error("Ngày " + plan.date + " bị trùng lịch. Vui lòng chọn phiên thay thế."));
                        }
                        conn.rollback();
                        return out(409, error("Phiên thay thế ngày " + plan.date + " vẫn bị trùng lịch."));
                    }

                    BigDecimal sessionAmount = calcSessionAmount(courtId, slotIds, plan.date, ctx);
                    for (Integer slotId : slotIds) {
                        BigDecimal slotAmount = sessionAmountForSlot(courtId, slotId, plan.date, ctx);
                        int bookingSlotId = repository.insertBookingSlot(conn, bookingId, courtId, plan.date, slotId, slotAmount);
                        try {
                            repository.insertCourtSlotBooking(conn, courtId, plan.date, slotId, bookingSlotId);
                        } catch (SQLException e) {
                            conn.rollback();
                            return out(409, error("Slot bị trùng lịch. Vui lòng thử lại."));
                        }
                    }

                    totalAmount = totalAmount.add(sessionAmount);
                    bookedDates.add(plan.date.toString());
                }

                if (bookedDates.isEmpty()) {
                    conn.rollback();
                    return out(409, error("Không còn ngày hợp lệ để đặt"));
                }

                int invoiceId = repository.insertInvoice(conn, bookingId, totalAmount);
                paymentRepository.insertPayment(conn, invoiceId, totalAmount, "FULL", paymentMethod, staffId);
                paymentRepository.updateInvoiceAsPaid(conn, bookingId, totalAmount);
                repository.updateBookingStatus(conn, bookingId, "CONFIRMED");

                conn.commit();

                EmailQueueService.EmailEnqueueResult emailResult =
                        emailQueueService.enqueueRecurringBookingCreated(bookingId);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("bookingId", bookingId);
                data.put("recurringId", recurringId);
                data.put("totalAmount", totalAmount);
                data.put("skippedDates", skippedDates);
                data.put("bookedDates", bookedDates);
                if (emailResult != null) {
                    data.put("emailQueued", emailResult.queued);
                    if (emailResult.warning != null && !emailResult.warning.isEmpty()) {
                        data.put("emailWarning", emailResult.warning);
                    }
                }

                return out(200, success("Tạo đặt sân định kỳ thành công", data));
            } catch (Exception e) {
                conn.rollback();
                return out(500, error("Lỗi hệ thống: " + e.getMessage()));
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private StaffRecurringBookingRequestDTO parseRequest(String body) {
        try {
            return GSON.fromJson(body, StaffRecurringBookingRequestDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private ValidationResult validateRequest(StaffRecurringBookingRequestDTO req) throws Exception {
        if (req.getStartDate() == null || req.getEndDate() == null) {
            return ValidationResult.fail(400, error("Thiếu ngày bắt đầu hoặc ngày kết thúc"));
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(req.getStartDate());
            endDate = LocalDate.parse(req.getEndDate());
        } catch (DateTimeParseException e) {
            return ValidationResult.fail(400, error("Ngày không hợp lệ"));
        }

        if (endDate.isBefore(startDate)) {
            return ValidationResult.fail(400, error("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu"));
        }

        if (endDate.isBefore(startDate.plusWeeks(4))) {
            return ValidationResult.fail(400, error("Thời gian định kỳ phải tối thiểu 4 tuần"));
        }

        if (startDate.isBefore(LocalDate.now())) {
            return ValidationResult.fail(400, error("Không thể đặt cho ngày trong quá khứ"));
        }

        if (!"ACCOUNT".equals(req.getCustomerType()) && !"GUEST".equals(req.getCustomerType())) {
            return ValidationResult.fail(400, error("Loại khách không hợp lệ"));
        }

        if ("ACCOUNT".equals(req.getCustomerType())) {
            if (req.getAccountId() == null || req.getAccountId() <= 0) {
                return ValidationResult.fail(400, error("Chưa chọn khách hàng"));
            }
        } else {
            if (req.getGuestName() == null || req.getGuestName().trim().isEmpty()) {
                return ValidationResult.fail(400, error("Vui lòng nhập họ tên khách"));
            }
            if (req.getGuestPhone() == null || req.getGuestPhone().trim().isEmpty()) {
                return ValidationResult.fail(400, error("Vui lòng nhập số điện thoại"));
            }
            if (req.getGuestEmail() != null && !req.getGuestEmail().trim().isEmpty()
                    && !isValidEmail(req.getGuestEmail())) {
                return ValidationResult.fail(400, error("Email không đúng định dạng"));
            }
            StaffCustomerAccountDTO matched = repository.findActiveCustomerByPhone(normalizePhone(req.getGuestPhone()));
            if (matched != null) {
                return ValidationResult.fail(409, guestPhoneMatchedJson(matched));
            }
        }

        if (req.getPatterns() == null || req.getPatterns().isEmpty()) {
            return ValidationResult.fail(400, error("Chưa chọn lịch đặt định kỳ"));
        }

        String policy = normalizePolicy(req.getConflictPolicy());
        if (!"SKIP".equals(policy) && !"SUGGEST".equals(policy)) {
            return ValidationResult.fail(400, error("Chính sách xử lý trùng lịch không hợp lệ"));
        }

        return ValidationResult.ok();
    }

    private PlanningContext buildContext(Connection conn, int facilityId) throws Exception {
        Map<Integer, Integer> slotOrder = repository.loadSlotOrder(conn);
        Map<Integer, LocalTime[]> slotTimes = repository.loadSlotTimes(conn);
        Map<Integer, Integer> courtTypes = repository.loadCourtTypes(conn, facilityId);
        Map<String, BigDecimal> pricesWeekday = repository.loadPrices(conn, facilityId, "WEEKDAY");
        Map<String, BigDecimal> pricesWeekend = repository.loadPrices(conn, facilityId, "WEEKEND");
        List<Integer> slotIdsByOrder = slotOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return new PlanningContext(slotOrder, slotTimes, courtTypes, pricesWeekday, pricesWeekend, slotIdsByOrder);
    }

    private Map<Integer, PatternPlan> buildPatternMap(List<StaffRecurringBookingRequestDTO.PatternDTO> patterns,
                                                      Map<Integer, Integer> slotOrder) throws Exception {
        Map<Integer, PatternPlan> map = new HashMap<>();
        for (StaffRecurringBookingRequestDTO.PatternDTO p : patterns) {
            if (p.getDayOfWeek() == null || p.getDayOfWeek() < 1 || p.getDayOfWeek() > 7) {
                throw new Exception("Ngày trong tuần không hợp lệ");
            }
            if (p.getCourtId() == null || p.getCourtId() <= 0) {
                throw new Exception("Thiếu sân cho lịch định kỳ");
            }
            if (p.getSlotIds() == null || p.getSlotIds().size() < 2) {
                throw new Exception("Mỗi phiên phải có ít nhất 2 slot liên tiếp");
            }

            List<Integer> slotIds = new ArrayList<>(p.getSlotIds());
            Set<Integer> unique = new HashSet<>(slotIds);
            if (unique.size() != slotIds.size()) {
                throw new Exception("Slot bị trùng trong cùng một phiên");
            }

            slotIds.sort(Comparator.comparingInt(s -> slotOrder.getOrDefault(s, -1)));
            if (!areConsecutive(slotIds, slotOrder)) {
                throw new Exception("Các slot phải liên tiếp trong cùng một phiên");
            }

            PatternPlan existing = map.get(p.getDayOfWeek());
            if (existing != null) {
                throw new Exception("Mỗi ngày chỉ được đặt 1 phiên");
            }

            map.put(p.getDayOfWeek(), new PatternPlan(p.getDayOfWeek(), p.getCourtId(), slotIds));
        }
        return map;
    }

    private List<DatePlan> generateDatePlans(StaffRecurringBookingRequestDTO req, Map<Integer, PatternPlan> patternMap) {
        LocalDate start = LocalDate.parse(req.getStartDate());
        LocalDate end = LocalDate.parse(req.getEndDate());
        List<DatePlan> plans = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int dow = toPatternDayOfWeek(d.getDayOfWeek());
            PatternPlan pattern = patternMap.get(dow);
            if (pattern != null) {
                plans.add(new DatePlan(d, pattern.courtId, pattern.slotIds));
            }
        }
        return plans;
    }

    private Map<LocalDate, SelectedSession> buildOverrides(StaffRecurringBookingRequestDTO req,
                                                           List<DatePlan> plans,
                                                           Map<Integer, Integer> slotOrder,
                                                           String policy) throws Exception {
        if (req.getSelectedSessions() == null || req.getSelectedSessions().isEmpty()) {
            return new HashMap<>();
        }
        if ("SKIP".equals(policy)) {
            throw new Exception("Không dùng phiên thay thế khi chọn chế độ bỏ qua");
        }

        Set<LocalDate> allowedDates = plans.stream().map(p -> p.date).collect(Collectors.toSet());
        Map<LocalDate, SelectedSession> map = new HashMap<>();

        for (StaffRecurringBookingRequestDTO.SelectedSessionDTO s : req.getSelectedSessions()) {
            if (s.getDate() == null || s.getCourtId() == null || s.getCourtId() <= 0) {
                throw new Exception("Phiên thay thế không hợp lệ");
            }
            if (s.getSlotIds() == null || s.getSlotIds().size() < 2) {
                throw new Exception("Phiên thay thế phải có ít nhất 2 slot liên tiếp");
            }
            LocalDate date;
            try {
                date = LocalDate.parse(s.getDate());
            } catch (DateTimeParseException e) {
                throw new Exception("Ngày thay thế không hợp lệ");
            }
            if (!allowedDates.contains(date)) {
                throw new Exception("Ngày " + date + " không nằm trong lịch định kỳ");
            }
            List<Integer> slotIds = new ArrayList<>(s.getSlotIds());
            slotIds.sort(Comparator.comparingInt(id -> slotOrder.getOrDefault(id, -1)));
            if (!areConsecutive(slotIds, slotOrder)) {
                throw new Exception("Các slot trong phiên thay thế phải liên tiếp");
            }
            map.put(date, new SelectedSession(date, s.getCourtId(), slotIds));
        }
        return map;
    }

    private void insertRecurringPatterns(Connection conn, int recurringId, List<StaffRecurringBookingRequestDTO.PatternDTO> patterns)
            throws Exception {
        for (StaffRecurringBookingRequestDTO.PatternDTO p : patterns) {
            List<Integer> slots = p.getSlotIds();
            if (slots == null) continue;
            for (Integer slotId : slots) {
                repository.insertRecurringPattern(conn, recurringId, p.getCourtId(), p.getDayOfWeek(), slotId);
            }
        }
    }

    private boolean areConsecutive(List<Integer> slotIds, Map<Integer, Integer> slotOrder) {
        if (slotIds.size() < 2) return false;
        for (int i = 1; i < slotIds.size(); i++) {
            int prev = slotOrder.getOrDefault(slotIds.get(i - 1), -1);
            int cur = slotOrder.getOrDefault(slotIds.get(i), -1);
            if (prev < 0 || cur < 0 || cur != prev + 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isConflict(Map<Integer, List<Integer>> booked, int courtId, List<Integer> slotIds) {
        List<Integer> bookedSlots = booked.get(courtId);
        if (bookedSlots == null || bookedSlots.isEmpty()) return false;
        for (Integer slotId : slotIds) {
            if (bookedSlots.contains(slotId)) return true;
        }
        return false;
    }

    private List<SuggestionView> buildSuggestions(Connection conn, int facilityId, DatePlan plan,
                                                  Map<Integer, List<Integer>> booked, PlanningContext ctx) throws Exception {
        List<SuggestionView> suggestions = new ArrayList<>();
        Integer courtTypeId = ctx.courtTypes.get(plan.courtId);
        if (courtTypeId == null) return suggestions;

        List<Integer> sameTypeCourts = repository.loadCourtsByType(conn, facilityId, courtTypeId);
        for (Integer courtId : sameTypeCourts) {
            if (courtId == plan.courtId) continue;
            if (!isConflict(booked, courtId, plan.slotIds)) {
                suggestions.add(buildSuggestion(courtId, plan.slotIds, plan.date, ctx));
                if (suggestions.size() >= MAX_SUGGESTIONS) return suggestions;
            }
        }

        if (!suggestions.isEmpty()) {
            return suggestions;
        }

        List<TimeBlock> blocks = findAvailableBlocks(plan, booked, ctx);
        for (TimeBlock block : blocks) {
            suggestions.add(buildSuggestion(plan.courtId, block.slotIds, plan.date, ctx));
            if (suggestions.size() >= MAX_SUGGESTIONS) break;
        }

        return suggestions;
    }

    private List<TimeBlock> findAvailableBlocks(DatePlan plan, Map<Integer, List<Integer>> booked, PlanningContext ctx) {
        List<Integer> orderedSlots = ctx.slotIdsByOrder;
        int len = plan.slotIds.size();
        int desiredStart = ctx.slotOrder.getOrDefault(plan.slotIds.get(0), -1);

        List<Integer> bookedSlots = booked.getOrDefault(plan.courtId, Collections.emptyList());
        Set<Integer> bookedSet = new HashSet<>(bookedSlots);

        List<TimeBlock> candidates = new ArrayList<>();
        for (int start = 0; start <= orderedSlots.size() - len; start++) {
            List<Integer> block = orderedSlots.subList(start, start + len);
            if (block.equals(plan.slotIds)) continue;
            boolean free = true;
            for (Integer slotId : block) {
                if (bookedSet.contains(slotId)) {
                    free = false;
                    break;
                }
            }
            if (free) {
                int distance = desiredStart >= 0 ? Math.abs(start - desiredStart) : start;
                candidates.add(new TimeBlock(distance, new ArrayList<>(block)));
            }
        }

        candidates.sort(Comparator
                .comparingInt((TimeBlock b) -> b.distance)
                .thenComparingInt(b -> ctx.slotOrder.getOrDefault(b.slotIds.get(0), 0)));

        return candidates;
    }

    private SuggestionView buildSuggestion(int courtId, List<Integer> slotIds, LocalDate date, PlanningContext ctx) {
        BigDecimal amount = calcSessionAmount(courtId, slotIds, date, ctx);
        LocalTime start = ctx.slotTimes.get(slotIds.get(0))[0];
        LocalTime end = ctx.slotTimes.get(slotIds.get(slotIds.size() - 1))[1];
        return new SuggestionView(courtId, slotIds, amount, start.toString(), end.toString());
    }

    private BigDecimal calcSessionAmount(int courtId, List<Integer> slotIds, LocalDate date, PlanningContext ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (Integer slotId : slotIds) {
            total = total.add(sessionAmountForSlot(courtId, slotId, date, ctx));
        }
        return total;
    }

    private BigDecimal sessionAmountForSlot(int courtId, int slotId, LocalDate date, PlanningContext ctx) {
        String dayType = isWeekend(date) ? "WEEKEND" : "WEEKDAY";
        Map<String, BigDecimal> priceCache = "WEEKEND".equals(dayType) ? ctx.pricesWeekend : ctx.pricesWeekday;

        Integer courtTypeId = ctx.courtTypes.get(courtId);
        LocalTime[] times = ctx.slotTimes.get(slotId);
        if (courtTypeId == null || times == null) return BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : priceCache.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            int ruleTypeId = Integer.parseInt(parts[0]);
            LocalTime ruleStart = LocalTime.parse(parts[1]);
            LocalTime ruleEnd = LocalTime.parse(parts[2]);
            if (ruleTypeId == courtTypeId && !times[0].isBefore(ruleStart) && !times[1].isAfter(ruleEnd)) {
                return entry.getValue();
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    private String buildPastSessionMessage(LocalDate date) {
        String time = LocalTime.now().format(TIME_FMT);
        return "Bạn đang đặt lịch cho ngày " + date + ". Vui lòng chọn các khung giờ từ " + time + " trở đi.";
    }

    private boolean isPastSession(LocalDate date, List<Integer> slotIds, PlanningContext ctx) {
        if (date == null || slotIds == null || slotIds.isEmpty()) return false;
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) return true;
        if (!date.isEqual(today)) return false;

        LocalTime sessionEnd = getSessionEndTime(slotIds, ctx);
        if (sessionEnd == null) return false;
        return !LocalTime.now().isBefore(sessionEnd);
    }

    private LocalTime getSessionStartTime(List<Integer> slotIds, PlanningContext ctx) {
        LocalTime start = null;
        for (Integer slotId : slotIds) {
            LocalTime[] times = ctx.slotTimes.get(slotId);
            if (times == null || times.length < 1 || times[0] == null) continue;
            if (start == null || times[0].isBefore(start)) {
                start = times[0];
            }
        }
        return start;
    }

    private LocalTime getSessionEndTime(List<Integer> slotIds, PlanningContext ctx) {
        LocalTime end = null;
        for (Integer slotId : slotIds) {
            LocalTime[] times = ctx.slotTimes.get(slotId);
            if (times == null || times.length < 2 || times[1] == null) continue;
            if (end == null || times[1].isAfter(end)) {
                end = times[1];
            }
        }
        return end;
    }

    private int toPatternDayOfWeek(DayOfWeek dow) {
        return dow == DayOfWeek.SUNDAY ? 1 : dow.getValue() + 1;
    }

    private String normalizePolicy(String policy) {
        if (policy == null) return "SKIP";
        return policy.trim().toUpperCase();
    }

    private String normalizePaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) return "CASH";
        String out = method.trim().toUpperCase();
        if (!"CASH".equals(out) && !"BANK_TRANSFER".equals(out) && !"VNPAY".equals(out)) {
            return null;
        }
        return out;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\s+", "").trim();
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String cleaned = email.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private boolean isValidEmail(String email) {
        String cleaned = email == null ? "" : email.trim();
        if (cleaned.isEmpty()) return true;
        return cleaned.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private String guestPhoneMatchedJson(StaffCustomerAccountDTO account) {
        return "{\"success\":false,\"code\":\"GUEST_PHONE_MATCHED_ACCOUNT\",\"message\":\"Số điện thoại đã tồn tại tài khoản khách hàng\",\"data\":{" +
                "\"accountId\":" + account.getAccountId() + "," +
                "\"fullName\":" + StaffAuthUtil.escapeJson(account.getFullName()) + "," +
                "\"phone\":" + StaffAuthUtil.escapeJson(account.getPhone()) + "," +
                "\"email\":" + StaffAuthUtil.escapeJson(account.getEmail()) +
                "}}";
    }

    private String error(String message) {
        return "{\"success\":false,\"message\":" + StaffAuthUtil.escapeJson(message) + "}";
    }

    private String success(String message, Object data) {
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("success", true);
        wrap.put("message", message);
        wrap.put("data", data);
        return GSON.toJson(wrap);
    }

    private StaffRecurringBookingOutcomeDTO out(int status, String json) {
        StaffRecurringBookingOutcomeDTO outcome = new StaffRecurringBookingOutcomeDTO();
        outcome.setStatus(status);
        outcome.setJson(json);
        return outcome;
    }

    private static class ValidationResult {
        final boolean valid;
        final int status;
        final String json;

        private ValidationResult(boolean valid, int status, String json) {
            this.valid = valid;
            this.status = status;
            this.json = json;
        }

        static ValidationResult ok() { return new ValidationResult(true, 200, null); }
        static ValidationResult fail(int status, String json) { return new ValidationResult(false, status, json); }
    }

    private static class PatternPlan {
        final int dayOfWeek;
        final int courtId;
        final List<Integer> slotIds;

        PatternPlan(int dayOfWeek, int courtId, List<Integer> slotIds) {
            this.dayOfWeek = dayOfWeek;
            this.courtId = courtId;
            this.slotIds = slotIds;
        }
    }

    private static class DatePlan {
        final LocalDate date;
        final int courtId;
        final List<Integer> slotIds;

        DatePlan(LocalDate date, int courtId, List<Integer> slotIds) {
            this.date = date;
            this.courtId = courtId;
            this.slotIds = slotIds;
        }
    }

    private static class SelectedSession {
        final LocalDate date;
        final int courtId;
        final List<Integer> slotIds;

        SelectedSession(LocalDate date, int courtId, List<Integer> slotIds) {
            this.date = date;
            this.courtId = courtId;
            this.slotIds = slotIds;
        }
    }

    private static class PlanningContext {
        final Map<Integer, Integer> slotOrder;
        final Map<Integer, LocalTime[]> slotTimes;
        final Map<Integer, Integer> courtTypes;
        final Map<String, BigDecimal> pricesWeekday;
        final Map<String, BigDecimal> pricesWeekend;
        final List<Integer> slotIdsByOrder;

        PlanningContext(Map<Integer, Integer> slotOrder,
                        Map<Integer, LocalTime[]> slotTimes,
                        Map<Integer, Integer> courtTypes,
                        Map<String, BigDecimal> pricesWeekday,
                        Map<String, BigDecimal> pricesWeekend,
                        List<Integer> slotIdsByOrder) {
            this.slotOrder = slotOrder;
            this.slotTimes = slotTimes;
            this.courtTypes = courtTypes;
            this.pricesWeekday = pricesWeekday;
            this.pricesWeekend = pricesWeekend;
            this.slotIdsByOrder = slotIdsByOrder;
        }
    }

    private static class SessionView {
        String date;
        int courtId;
        List<Integer> slotIds;
        String status;
        BigDecimal amount;

        static SessionView ok(LocalDate date, int courtId, List<Integer> slotIds, BigDecimal amount) {
            SessionView v = new SessionView();
            v.date = date.toString();
            v.courtId = courtId;
            v.slotIds = slotIds;
            v.status = "OK";
            v.amount = amount;
            return v;
        }

        static SessionView conflict(LocalDate date, int courtId, List<Integer> slotIds, BigDecimal amount) {
            SessionView v = new SessionView();
            v.date = date.toString();
            v.courtId = courtId;
            v.slotIds = slotIds;
            v.status = "CONFLICT";
            v.amount = amount;
            return v;
        }

        static SessionView skipped(LocalDate date, int courtId, List<Integer> slotIds, BigDecimal amount) {
            SessionView v = new SessionView();
            v.date = date.toString();
            v.courtId = courtId;
            v.slotIds = slotIds;
            v.status = "SKIPPED";
            v.amount = amount;
            return v;
        }
    }

    private static class ConflictView {
        String date;
        int courtId;
        List<Integer> slotIds;
        List<SuggestionView> suggestions;

        ConflictView(DatePlan plan, List<SuggestionView> suggestions) {
            this.date = plan.date.toString();
            this.courtId = plan.courtId;
            this.slotIds = plan.slotIds;
            this.suggestions = suggestions;
        }
    }

    private static class SuggestionView {
        int courtId;
        List<Integer> slotIds;
        BigDecimal amount;
        String startTime;
        String endTime;

        SuggestionView(int courtId, List<Integer> slotIds, BigDecimal amount, String startTime, String endTime) {
            this.courtId = courtId;
            this.slotIds = slotIds;
            this.amount = amount;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private static class TimeBlock {
        final int distance;
        final List<Integer> slotIds;

        TimeBlock(int distance, List<Integer> slotIds) {
            this.distance = distance;
            this.slotIds = slotIds;
        }
    }
}





