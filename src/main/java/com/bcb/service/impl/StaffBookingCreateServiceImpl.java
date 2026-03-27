package com.bcb.service.impl;


import com.bcb.dto.staff.RentalItemDTO;
import com.bcb.dto.staff.StaffBookingCreateOutcomeDTO;
import com.bcb.dto.staff.StaffBookingCreateSlotDTO;
import com.bcb.dto.staff.StaffCustomerAccountDTO;
import com.bcb.repository.impl.StaffBookingCreateRepositoryImpl;
import com.bcb.repository.staff.StaffBookingCreateRepository;
import com.bcb.service.email.EmailQueueService;
import com.bcb.service.email.impl.EmailQueueServiceImpl;
import com.bcb.service.staff.StaffBookingCreateService;
import com.bcb.utils.DBContext;
import com.bcb.utils.staff.StaffAuthUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffBookingCreateServiceImpl implements StaffBookingCreateService {

    private final StaffBookingCreateRepository repository = new StaffBookingCreateRepositoryImpl();
    private final EmailQueueService emailQueueService = new EmailQueueServiceImpl();

    /**
     * Validates input and orchestrates creation of a staff proxy booking.
     */
    @Override
    public StaffBookingCreateOutcomeDTO createBooking(String body, int facilityId, Integer staffId) throws Exception {
        if (staffId == null) {
            return out(403, jsonError("Staff chưa được gán"));
        }

        String dateStr = extractString(body, "date");
        String customerType = extractString(body, "customerType");
        String accountIdStr = extractString(body, "accountId");
        String guestName = extractString(body, "guestName");
        String guestPhone = normalizePhone(extractString(body, "guestPhone"));
        String guestEmail = normalizeEmail(extractString(body, "guestEmail"));

        List<StaffBookingCreateSlotDTO> slots = parseSlots(body);
        List<RentalItemDTO> rentals = parseRentals(body);

        if (dateStr == null || dateStr.isEmpty()) {
            return out(400, jsonError("Thiếu ngày đặt sân"));
        }

        LocalDate bookingDate;
        try {
            bookingDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return out(400, jsonError("Ngày không hợp lệ"));
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            return out(400, jsonError("Không thể đặt cho ngày trong quá khứ"));
        }

        if (slots.isEmpty()) {
            return out(400, jsonError("Chưa chọn slot nào"));
        }

        if (!"ACCOUNT".equals(customerType) && !"GUEST".equals(customerType)) {
            return out(400, jsonError("Loại khách không hợp lệ"));
        }

        Integer accountId = null;
        if ("ACCOUNT".equals(customerType)) {
            if (accountIdStr == null || accountIdStr.isEmpty()) {
                return out(400, jsonError("Chưa chọn khách hàng"));
            }
            try {
                accountId = Integer.parseInt(accountIdStr);
            } catch (NumberFormatException e) {
                return out(400, jsonError("Account ID không hợp lệ"));
            }
        } else {
            if (guestName == null || guestName.trim().isEmpty()) {
                return out(400, jsonError("Vui lòng nhập họ tên khách"));
            }
            if (guestPhone == null || guestPhone.trim().isEmpty()) {
                return out(400, jsonError("Vui lòng nhập số điện thoại"));
            }
        }

        // Guest phone matches an existing account -> prompt client to switch to ACCOUNT.
        if ("GUEST".equals(customerType)) {
            StaffCustomerAccountDTO matchedAccount = repository.findActiveCustomerByPhone(guestPhone);
            if (matchedAccount != null) {
                return out(409, guestPhoneMatchedJson(matchedAccount));
            }
        }

        // Business rule: each session must contain >= 2 consecutive slots on the same court.
        if (!validateSlotGroups(slots)) {
            return out(400, jsonError("Mỗi phiên chơi phải có ít nhất 2 slot liên tiếp trên cùng 1 sân"));
        }

        if (hasExpiredSlotForToday(bookingDate, slots)) {
            return out(400, jsonError("Đã quá giờ kết thúc của một hoặc nhiều slot. Vui lòng chọn slot khác."));
        }

        if (!validateRentals(rentals)) {
            return out(400, jsonError("Dữ liệu đồ thuê không hợp lệ"));
        }

        try {
            int bookingId = createBookingTransaction(
                    facilityId,
                    bookingDate,
                    customerType,
                    accountId,
                    guestName,
                    guestPhone,
                    guestEmail,
                    staffId,
                    slots,
                    rentals
            );
            // Send booking confirmation email after successful creation.
            EmailQueueService.EmailEnqueueResult emailResult = emailQueueService.enqueueBookingCreated(bookingId);
            return out(200, buildSuccessJson(bookingId, emailResult));
        } catch (SlotConflictException e) {
            return out(409, jsonError("Slot đã được đặt bởi người khác. Vui lòng chọn lại."));
        } catch (Exception e) {
            return out(500, jsonError("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Backward-compatible overload for legacy callers without rentals.
     */
    private int createBookingTransaction(int facilityId, LocalDate bookingDate, String customerType,
                                         Integer accountId, String guestName, String guestPhone, String guestEmail,
                                         int staffId, List<StaffBookingCreateSlotDTO> slots) throws Exception {
        return createBookingTransaction(
                facilityId,
                bookingDate,
                customerType,
                accountId,
                guestName,
                guestPhone,
                guestEmail,
                staffId,
                slots,
                new ArrayList<>()
        );
    }

    /**
     * Multi-step booking transaction: create booking, slots, rentals, and invoice atomically.
     */
    private int createBookingTransaction(int facilityId, LocalDate bookingDate, String customerType,
                                         Integer accountId, String guestName, String guestPhone, String guestEmail,
                                         int staffId, List<StaffBookingCreateSlotDTO> slots,
                                         List<RentalItemDTO> rentals) throws Exception {

        DayOfWeek dow = bookingDate.getDayOfWeek();
        String dayType = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer guestId = null;
                if ("GUEST".equals(customerType)) {
                    guestId = repository.insertGuest(conn, guestName, guestPhone, guestEmail);
                }

                int bookingId = "ACCOUNT".equals(customerType)
                        ? repository.insertBookingForAccount(conn, facilityId, bookingDate, accountId, staffId)
                        : repository.insertBookingForGuest(conn, facilityId, bookingDate, guestId, staffId);

                Map<String, BigDecimal> priceCache = repository.loadPrices(conn, facilityId, dayType);
                Map<Integer, Integer> courtTypeMap = repository.loadCourtTypes(conn, facilityId);
                Map<Integer, LocalTime[]> slotTimeMap = repository.loadSlotTimes(conn);

                BigDecimal totalAmount = BigDecimal.ZERO;

                /* Map newly created booking slots for linking rentals (key: courtId_slotId). */
                Map<String, Integer> bookingSlotIdByCourtAndSlot = new HashMap<>();

                for (StaffBookingCreateSlotDTO slot : slots) {
                    BigDecimal price = lookupPrice(slot, priceCache, courtTypeMap, slotTimeMap);
                    totalAmount = totalAmount.add(price);

                    int bookingSlotId = repository.insertBookingSlot(conn, bookingId, bookingDate, slot, price);
                    try {
                        repository.insertCourtSlotBooking(conn, slot, bookingDate, bookingSlotId);
                    } catch (SQLException e) {
                        conn.rollback();
                        throw new SlotConflictException();
                    }

                    bookingSlotIdByCourtAndSlot.put(buildCourtSlotKey(slot.getCourtId(), slot.getSlotId()), bookingSlotId);
                }

                /* Persist rental items after booking slots are created (safe to skip when empty). */
                BigDecimal rentalTotal = BigDecimal.ZERO;

                for (RentalItemDTO rental : rentals) {
                    if (rental == null) continue;

                    int matchedSlotCount = 0;
                    for (Integer slotId : rental.getSlotIds()) {
                        Integer bookingSlotId = bookingSlotIdByCourtAndSlot.get(
                                buildCourtSlotKey(rental.getCourtId(), slotId)
                        );
                        if (bookingSlotId == null) continue;

                        repository.insertBookingRental(
                                conn,
                                bookingSlotId,
                                rental.getInventoryId(),
                                rental.getQuantity(),
                                rental.getUnitPrice(),
                                "STAFF"
                        );
                        repository.updateInventoryRentalScheduleStatus(
                                conn,
                                facilityId,
                                bookingDate,
                                rental.getCourtId(),
                                slotId,
                                rental.getInventoryId(),
                                "RENTED"
                        );
                        matchedSlotCount++;
                    }

                    if (matchedSlotCount == 0) continue;

                    BigDecimal lineTotal = rental.getUnitPrice()
                            .multiply(BigDecimal.valueOf(rental.getQuantity()))
                            .multiply(BigDecimal.valueOf(matchedSlotCount));

                    rentalTotal = rentalTotal.add(lineTotal);
                }

                totalAmount = totalAmount.add(rentalTotal);

                repository.insertInvoice(conn, bookingId, totalAmount);
                conn.commit();
                return bookingId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private BigDecimal lookupPrice(StaffBookingCreateSlotDTO slot, Map<String, BigDecimal> priceCache,
                                   Map<Integer, Integer> courtTypeMap, Map<Integer, LocalTime[]> slotTimeMap) {
        Integer courtTypeId = courtTypeMap.get(slot.getCourtId());
        LocalTime[] times = slotTimeMap.get(slot.getSlotId());
        if (courtTypeId == null || times == null) {
            return BigDecimal.ZERO;
        }

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

    private boolean hasExpiredSlotForToday(LocalDate bookingDate, List<StaffBookingCreateSlotDTO> slots) throws Exception {
        if (!LocalDate.now().equals(bookingDate) || slots == null || slots.isEmpty()) {
            return false;
        }

        LocalTime now = LocalTime.now();
        try (Connection conn = DBContext.getConnection()) {
            Map<Integer, LocalTime[]> slotTimeMap = repository.loadSlotTimes(conn);
            for (StaffBookingCreateSlotDTO slot : slots) {
                LocalTime[] times = slotTimeMap.get(slot.getSlotId());
                if (times == null) continue;
                if (now.compareTo(times[1]) >= 0) return true;
            }
        }

        return false;
    }

    private boolean validateSlotGroups(List<StaffBookingCreateSlotDTO> slots) throws Exception {
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (StaffBookingCreateSlotDTO slot : slots) {
            groups.computeIfAbsent(slot.getCourtId(), k -> new ArrayList<>()).add(slot.getSlotId());
        }

        Map<Integer, Integer> slotOrder;
        try (Connection conn = DBContext.getConnection()) {
            slotOrder = repository.loadSlotOrder(conn);
        }

        for (Map.Entry<Integer, List<Integer>> entry : groups.entrySet()) {
            List<Integer> slotIds = entry.getValue();
            slotIds.sort((a, b) -> Integer.compare(slotOrder.getOrDefault(a, 0), slotOrder.getOrDefault(b, 0)));

            List<Integer> currentSession = new ArrayList<>();
            currentSession.add(slotIds.get(0));

            for (int i = 1; i < slotIds.size(); i++) {
                int prevOrder = slotOrder.getOrDefault(slotIds.get(i - 1), -1);
                int curOrder = slotOrder.getOrDefault(slotIds.get(i), -1);

                if (curOrder == prevOrder + 1) {
                    currentSession.add(slotIds.get(i));
                } else {
                    if (currentSession.size() < 2) return false;
                    currentSession = new ArrayList<>();
                    currentSession.add(slotIds.get(i));
                }
            }

            if (currentSession.size() < 2) return false;
        }

        return true;
    }

    private boolean validateRentals(List<RentalItemDTO> rentals) {
        if (rentals == null || rentals.isEmpty()) return true;

        for (RentalItemDTO item : rentals) {
            if (item == null) return false;
            if (item.getCourtId() == null || item.getCourtId() <= 0) return false;
            if (item.getInventoryId() == null || item.getInventoryId() <= 0) return false;
            if (item.getQuantity() == null || item.getQuantity() <= 0) return false;
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) return false;
            if (item.getSlotIds() == null || item.getSlotIds().isEmpty()) return false;
        }

        return true;
    }

    private List<StaffBookingCreateSlotDTO> parseSlots(String json) {
        List<StaffBookingCreateSlotDTO> result = new ArrayList<>();

        String arrStr = extractArray(json, "slots");
        if (arrStr == null || arrStr.isEmpty()) return result;

        List<String> objects = splitTopLevelObjects(arrStr);
        for (String obj : objects) {
            String courtIdStr = extractString(obj, "courtId");
            String slotIdStr = extractString(obj, "slotId");

            if (courtIdStr != null && slotIdStr != null) {
                try {
                    result.add(new StaffBookingCreateSlotDTO(
                            Integer.parseInt(courtIdStr),
                            Integer.parseInt(slotIdStr)
                    ));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return result;
    }

    private List<RentalItemDTO> parseRentals(String json) {
        List<RentalItemDTO> result = new ArrayList<>();

        String arrStr = extractArray(json, "rentals");
        if (arrStr == null || arrStr.isEmpty()) return result;

        List<String> objects = splitTopLevelObjects(arrStr);
        for (String obj : objects) {
            RentalItemDTO item = new RentalItemDTO();

            String courtIdStr = extractString(obj, "courtId");
            String inventoryIdStr = extractString(obj, "inventoryId");
            String quantityStr = extractString(obj, "quantity");
            String unitPriceStr = extractString(obj, "unitPrice");
            String name = extractString(obj, "name");

            try {
                item.setCourtId(courtIdStr == null ? null : Integer.parseInt(courtIdStr));
                item.setInventoryId(inventoryIdStr == null ? null : Integer.parseInt(inventoryIdStr));
                item.setQuantity(quantityStr == null ? null : Integer.parseInt(quantityStr));
                item.setUnitPrice(unitPriceStr == null ? null : new BigDecimal(unitPriceStr));
                item.setName(name);
                item.setGroupKey(extractString(obj, "groupKey"));
                item.setStartTime(extractString(obj, "startTime"));
                item.setEndTime(extractString(obj, "endTime"));
                item.setSlotIds(parseIntArray(obj, "slotIds"));

                result.add(item);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private List<Integer> parseIntArray(String json, String key) {
        List<Integer> result = new ArrayList<>();

        String arr = extractArray(json, key);
        if (arr == null || arr.length() < 2) return result;

        String content = arr.substring(1, arr.length() - 1).trim();
        if (content.isEmpty()) return result;

        String[] parts = content.split(",");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        return result;
    }

    private String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;

        int arrStart = json.indexOf("[", idx + search.length());
        if (arrStart < 0) return null;

        int arrEnd = findMatchingBracket(json, arrStart);
        if (arrEnd < 0) return null;

        return json.substring(arrStart, arrEnd + 1);
    }

    private int findMatchingBracket(String text, int openIndex) {
        int level = 0;
        for (int i = openIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                level++;
            } else if (c == ']') {
                level--;
                if (level == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private List<String> splitTopLevelObjects(String arrayText) {
        List<String> result = new ArrayList<>();
        if (arrayText == null || arrayText.length() < 2) return result;

        int level = 0;
        int objStart = -1;

        for (int i = 0; i < arrayText.length(); i++) {
            char c = arrayText.charAt(i);

            if (c == '{') {
                if (level == 0) objStart = i;
                level++;
            } else if (c == '}') {
                level--;
                if (level == 0 && objStart >= 0) {
                    result.add(arrayText.substring(objStart, i + 1));
                    objStart = -1;
                }
            }
        }

        return result;
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;

        idx = json.indexOf(":", idx + search.length());
        if (idx < 0) return null;

        idx++;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        if (idx >= json.length()) return null;

        if (json.startsWith("null", idx)) return null;

        if (json.charAt(idx) == '"') {
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;

            for (int i = idx + 1; i < json.length(); i++) {
                char c = json.charAt(i);

                if (escaped) {
                    sb.append(c);
                    escaped = false;
                    continue;
                }

                if (c == '\\') {
                    escaped = true;
                    continue;
                }

                if (c == '"') {
                    return sb.toString();
                }

                sb.append(c);
            }
            return null;
        }

        int end = idx;
        while (end < json.length()
                && json.charAt(end) != ','
                && json.charAt(end) != '}'
                && json.charAt(end) != ']'
                && !Character.isWhitespace(json.charAt(end))) {
            end++;
        }

        return json.substring(idx, end);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\s+", "").trim();
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String value = email.trim();
        return value.isEmpty() ? null : value;
    }

    private String buildCourtSlotKey(Integer courtId, Integer slotId) {
        return courtId + "_" + slotId;
    }

    private String buildSuccessJson(int bookingId, EmailQueueService.EmailEnqueueResult emailResult) {
        StringBuilder data = new StringBuilder(96);
        data.append("\"bookingId\":").append(bookingId);
        if (emailResult != null) {
            data.append(",\"emailQueued\":").append(emailResult.queued);
            if (emailResult.warning != null && !emailResult.warning.isEmpty()) {
                data.append(",\"emailWarning\":").append(StaffAuthUtil.escapeJson(emailResult.warning));
            }
        }
        return "{\"success\":true,\"message\":\"Đặt sân thành công\",\"data\":{" + data + "}}";
    }

    private String guestPhoneMatchedJson(StaffCustomerAccountDTO account) {
        return "{\"success\":false,\"code\":\"GUEST_PHONE_MATCHED_ACCOUNT\",\"message\":\"Số điện thoại đã tồn tại tài khoản khách hàng\",\"data\":{" +
                "\"accountId\":" + account.getAccountId() + "," +
                "\"fullName\":" + StaffAuthUtil.escapeJson(account.getFullName()) + "," +
                "\"phone\":" + StaffAuthUtil.escapeJson(account.getPhone()) + "," +
                "\"email\":" + StaffAuthUtil.escapeJson(account.getEmail()) +
                "}}";
    }

    private String jsonError(String message) {
        return "{\"success\":false,\"message\":" + StaffAuthUtil.escapeJson(message) + "}";
    }

    private StaffBookingCreateOutcomeDTO out(int status, String json) {
        StaffBookingCreateOutcomeDTO outcome = new StaffBookingCreateOutcomeDTO();
        outcome.setStatus(status);
        outcome.setJson(json);
        return outcome;
    }

    private static class SlotConflictException extends Exception {
        public SlotConflictException() {
            super("Slot conflict");
        }
    }

}

