package com.bcb.service.mybooking.impl;

import com.bcb.dto.mybooking.BookingSlotDetailDTO;
import com.bcb.dto.mybooking.MergedSlotDTO;
import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Notification;
import com.bcb.repository.mybooking.MyBookingRepository;
import com.bcb.repository.mybooking.NotificationRepository;
import com.bcb.repository.mybooking.impl.MyBookingRepositoryImpl;
import com.bcb.repository.mybooking.impl.NotificationRepositoryImpl;
import com.bcb.service.mybooking.MyBookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of {@link MyBookingService}.
 * Handles booking list (with consecutive slot merging), detail view, and cancellation.
 *
 * @author AnhTN
 */
public class MyBookingServiceImpl implements MyBookingService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DTF_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MyBookingRepository bookingRepo;
    private final NotificationRepository notificationRepo;

    public MyBookingServiceImpl() {
        this.bookingRepo = new MyBookingRepositoryImpl();
        this.notificationRepo = new NotificationRepositoryImpl();
    }

//    /** {@inheritDoc} */
//    @Override
//    public List<MyBookingListDTO> getMyBookings(int accountId, String status,
//                                                 LocalDate dateFrom, LocalDate dateTo) {
//        return getMyBookings(accountId, status, dateFrom, dateTo, 0, Integer.MAX_VALUE);
//    }

    /** {@inheritDoc} */
    @Override
    public List<MyBookingListDTO> getMyBookings(int accountId, String status,
                                                 LocalDate dateFrom, LocalDate dateTo,
                                                 int offset, int limit) {
        List<MyBookingListDTO> bookings = bookingRepo.findMyBookings(accountId, status, dateFrom, dateTo,
                Math.max(0, offset), Math.max(1, limit));
        // Post-process: single booking keeps old slot merge, recurring shows weekly pattern summary.
        for (MyBookingListDTO b : bookings) {
            if ("RECURRING".equals(b.getBookingType())) {
                String patternText = mergeRecurringPatternSlots(b.getRecurringPatternDetails());
                b.setRecurringPatternDetails(patternText);
                b.setSlotDetails(patternText);
            } else {
                b.setSlotDetails(mergeConsecutiveSlots(b.getSlotDetails()));
            }
        }
        return bookings;
    }

//    /** {@inheritDoc} */
//    @Override
//    public MyBookingDetailDTO getBookingDetail(int bookingId, int accountId) throws BusinessException {
//        return getBookingDetail(bookingId, accountId, 5, false, 0, LocalDate.now());
//    }

    /** {@inheritDoc} */
    @Override
    public MyBookingDetailDTO getBookingDetail(int bookingId, int accountId,
                                               int futureDayLimit,
                                               boolean includePastDays,
                                               int pastDayLimit,
                                               LocalDate pivotDate) throws BusinessException {
        MyBookingDetailDTO detail = bookingRepo.findBookingDetail(bookingId, accountId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "Booking not found or you don't have permission to view it."));

        List<BookingSlotDetailDTO> slots;
        List<BookingSlotDetailDTO> pastSlots = new ArrayList<>();
        if (detail.getCreatedAt() != null) {
            detail.setCreatedAtDisplay(detail.getCreatedAt().format(DTF_UI));
        }

        // Build merged slots for display (same algorithm as list view)
        if ("RECURRING".equals(detail.getBookingType())) {
            int safeFutureDayLimit = Math.max(1, futureDayLimit);
            int safePastDayLimit = Math.max(0, pastDayLimit);
            LocalDate baseDate = pivotDate != null ? pivotDate : LocalDate.now();

            List<LocalDate> futureDates = bookingRepo.findBookingDates(
                    bookingId, baseDate, false, safeFutureDayLimit + 1);
            boolean hasMoreFuture = futureDates.size() > safeFutureDayLimit;
            if (hasMoreFuture) {
                futureDates = new ArrayList<>(futureDates.subList(0, safeFutureDayLimit));
            }
            slots = bookingRepo.findSlotsByBookingIdAndDates(bookingId, futureDates, true);

            boolean hasMorePast = false;
            if (includePastDays && safePastDayLimit > 0) {
                List<LocalDate> pastDates = bookingRepo.findBookingDates(
                        bookingId, baseDate, true, safePastDayLimit + 1);
                hasMorePast = pastDates.size() > safePastDayLimit;
                if (hasMorePast) {
                    pastDates = new ArrayList<>(pastDates.subList(0, safePastDayLimit));
                }
                pastSlots = bookingRepo.findSlotsByBookingIdAndDates(bookingId, pastDates, false);
            }

            List<BookingSlotDetailDTO> visibleSlots = new ArrayList<>(slots);
            visibleSlots.addAll(pastSlots);
            detail.setSlots(visibleSlots);
            detail.setRecurringPatternDetails(mergeRecurringPatternSlots(detail.getRecurringPatternDetails()));
            detail.setRecurringSessions(mergeSlotsByDate(slots, true));
            detail.setPastRecurringSessions(mergeSlotsByDate(pastSlots, false));
            detail.setHasMoreFutureSessions(hasMoreFuture);
            detail.setHasMorePastSessions(hasMorePast);
            detail.setMergedSlots(new ArrayList<>());
        } else {
            slots = bookingRepo.findSlotsByBookingId(bookingId);
            detail.setSlots(slots);
            detail.setMergedSlots(mergeSlots(slots));
            detail.setRecurringSessions(new ArrayList<>());
            detail.setPastRecurringSessions(new ArrayList<>());
            detail.setHasMoreFutureSessions(false);
            detail.setHasMorePastSessions(false);
        }

        // Calculate total hours from slots
        double totalHours = 0;
        List<BookingSlotDetailDTO> hoursSource = detail.getSlots() != null ? detail.getSlots() : slots;
        for (BookingSlotDetailDTO slot : hoursSource) {
            try {
                LocalTime start = LocalTime.parse(slot.getStartTime(), TF);
                LocalTime end = LocalTime.parse(slot.getEndTime(), TF);
                long minutes = java.time.Duration.between(start, end).toMinutes();
                totalHours += minutes / 60.0;
            } catch (Exception ignored) {
                // skip invalid time parsing
            }
        }
        detail.setTotalHours(totalHours);

        return detail;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelBooking(int bookingId, int accountId) throws BusinessException {
        // Validate cancellable
        if (!bookingRepo.isCancellable(bookingId, accountId)) {
            throw new BusinessException("CANCEL_NOT_ALLOWED",
                    "Booking cannot be cancelled. It may already be paid, cancelled, or does not belong to you.");
        }

        // Perform cancellation (transactional)
        bookingRepo.cancelBooking(bookingId);

        // Send in-app notification
        Notification notification = new Notification();
        notification.setAccountId(accountId);
        notification.setTitle("Đặt sân đã bị hủy");
        notification.setContent("Booking #" + bookingId + " đã được hủy thành công.");
        notification.setType("SYSTEM");
        notification.setIsSent(false);
        notificationRepo.insertNotification(notification);
    }

    /* ════════════════════════════════════════════════════════════════════
       Private helpers
       ════════════════════════════════════════════════════════════════════ */

    /**
     * Merges a list of {@link BookingSlotDetailDTO} into grouped {@link MergedSlotDTO} blocks.
     * Consecutive slots (prevEnd == curStart) on the same court are combined into one block.
     * Price is summed; slotCount reflects how many raw slots were merged.
     * Result is sorted by startTime then courtName — same ordering as the list view.
     *
     * @param slots raw slot list (sorted by startTime, courtName from DB)
     * @return list of merged slot blocks ready for display
     * @author AnhTN
     */
    private List<MergedSlotDTO> mergeSlots(List<BookingSlotDetailDTO> slots) {
        if (slots == null || slots.isEmpty()) return new ArrayList<>();

        // Group by courtName preserving insertion order
        LinkedHashMap<String, List<BookingSlotDetailDTO>> grouped = new LinkedHashMap<>();
        for (BookingSlotDetailDTO s : slots) {
            grouped.computeIfAbsent(s.getCourtName(), k -> new ArrayList<>()).add(s);
        }

        // Collect merged segments: [segStart, segEnd, courtName, totalPrice, slotCount]
        List<Object[]> segments = new ArrayList<>();

        for (Map.Entry<String, List<BookingSlotDetailDTO>> entry : grouped.entrySet()) {
            String court = entry.getKey();
            List<BookingSlotDetailDTO> list = entry.getValue();
            // Sort by startTime within court
            list.sort(Comparator.comparing(s -> LocalTime.parse(s.getStartTime(), TF)));

            BookingSlotDetailDTO first = list.get(0);
            LocalTime segStart = LocalTime.parse(first.getStartTime(), TF);
            LocalTime segEnd   = LocalTime.parse(first.getEndTime(), TF);
            BigDecimal price   = first.getPrice() != null ? first.getPrice() : BigDecimal.ZERO;
            int count = 1;

            for (int i = 1; i < list.size(); i++) {
                BookingSlotDetailDTO cur = list.get(i);
                LocalTime curStart = LocalTime.parse(cur.getStartTime(), TF);
                LocalTime curEnd   = LocalTime.parse(cur.getEndTime(), TF);
                BigDecimal curPrice = cur.getPrice() != null ? cur.getPrice() : BigDecimal.ZERO;

                if (curStart.equals(segEnd)) {
                    // Consecutive — extend block
                    segEnd  = curEnd;
                    price   = price.add(curPrice);
                    count++;
                } else {
                    segments.add(new Object[]{segStart, segEnd, court, price, count});
                    segStart = curStart;
                    segEnd   = curEnd;
                    price    = curPrice;
                    count    = 1;
                }
            }
            segments.add(new Object[]{segStart, segEnd, court, price, count});
        }

        // Sort globally by startTime then courtName
        segments.sort(Comparator
                .comparing((Object[] o) -> (LocalTime) o[0])
                .thenComparing(o -> (String) o[2]));

        // Map to DTOs
        List<MergedSlotDTO> result = new ArrayList<>();
        for (Object[] seg : segments) {
            result.add(new MergedSlotDTO(
                    (String)    seg[2],
                    ((LocalTime) seg[0]).format(TF),
                    ((LocalTime) seg[1]).format(TF),
                    (BigDecimal) seg[3],
                    (int)        seg[4]
            ));
        }
        return result;
    }

    /**
     * Merges consecutive time slots for the same court into a single time range.
     *
     * <p>Input (raw from DB):  "CourtA|08:00|08:30;;CourtA|08:30|09:00;;CourtB|09:00|09:30"
     * <p>Output (display):     "CourtA: 08:00-09:00, CourtB: 09:00-09:30"
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Parse each token into (courtName, startTime, endTime).</li>
     *   <li>Group by courtName (insertion order preserved via LinkedHashMap).</li>
     *   <li>Sort per-court slots by startTime; merge adjacent where prevEnd == curStart.</li>
     *   <li>Sort merged segments globally by startTime then courtName.</li>
     * </ol>
     *
     * @param slotRaw raw slot string from DB (row separator ";;", field separator "|")
     * @return human-readable merged slot description, or empty string if null/blank
     * @author AnhTN
     */
    private String mergeConsecutiveSlots(String slotRaw) {
        if (slotRaw == null || slotRaw.isBlank()) return "";

        // Step 1: Parse tokens
        String[] tokens = slotRaw.split(";;");
        // Map: courtName → list of [startTime, endTime]
        LinkedHashMap<String, List<LocalTime[]>> courtSlots = new LinkedHashMap<>();

        for (String token : tokens) {
            String[] parts = token.split("\\|");
            if (parts.length < 3) continue;
            String courtName = parts[0].trim();
            try {
                LocalTime start = LocalTime.parse(parts[1].trim(), TF);
                LocalTime end   = LocalTime.parse(parts[2].trim(), TF);
                courtSlots.computeIfAbsent(courtName, k -> new ArrayList<>())
                          .add(new LocalTime[]{start, end});
            } catch (Exception ignored) { /* skip malformed */ }
        }

        if (courtSlots.isEmpty()) return slotRaw; // fallback

        // Step 2: Merge consecutive slots per court; collect merged segments for sorting
        // Each entry: [firstStart, lastEnd, courtName]
        List<Object[]> mergedSegments = new ArrayList<>();

        for (Map.Entry<String, List<LocalTime[]>> entry : courtSlots.entrySet()) {
            String court = entry.getKey();
            List<LocalTime[]> timeList = entry.getValue();
            // Sort by start time
            timeList.sort(Comparator.comparing(a -> a[0]));

            LocalTime segStart = timeList.get(0)[0];
            LocalTime segEnd   = timeList.get(0)[1];

            for (int i = 1; i < timeList.size(); i++) {
                LocalTime curStart = timeList.get(i)[0];
                LocalTime curEnd   = timeList.get(i)[1];
                if (curStart.equals(segEnd)) {
                    // Consecutive — extend current segment
                    segEnd = curEnd;
                } else {
                    // Gap found — save current segment and start new one
                    mergedSegments.add(new Object[]{segStart, segEnd, court});
                    segStart = curStart;
                    segEnd   = curEnd;
                }
            }
            mergedSegments.add(new Object[]{segStart, segEnd, court});
        }

        // Step 3: Sort merged segments by startTime, then courtName
        mergedSegments.sort(Comparator
                .comparing((Object[] o) -> (LocalTime) o[0])
                .thenComparing(o -> (String) o[2]));

        // Step 4: Build display string
        StringBuilder sb = new StringBuilder();
        for (Object[] seg : mergedSegments) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(seg[2])  // court name
              .append(": ")
              .append(((LocalTime) seg[0]).format(TF))
              .append("-")
              .append(((LocalTime) seg[1]).format(TF));
        }
        return sb.toString();
    }

    /**
     * Merges recurring weekly pattern raw tokens into readable blocks grouped by day-of-week + court.
     * Input token format: dayOfWeek|courtName|start|end
     */
    private String mergeRecurringPatternSlots(String patternRaw) {
        if (patternRaw == null || patternRaw.isBlank()) return "";

        String[] tokens = patternRaw.split(";;");
        LinkedHashMap<String, List<LocalTime[]>> grouped = new LinkedHashMap<>();

        for (String token : tokens) {
            String[] parts = token.split("\\|");
            if (parts.length < 4) continue;
            try {
                int dayOfWeek = Integer.parseInt(parts[0].trim());
                String courtName = parts[1].trim();
                LocalTime start = LocalTime.parse(parts[2].trim(), TF);
                LocalTime end = LocalTime.parse(parts[3].trim(), TF);
                String key = dayOfWeek + "|" + courtName;
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(new LocalTime[]{start, end});
            } catch (Exception ignored) {
                // skip malformed token
            }
        }

        List<String> chunks = new ArrayList<>();
        for (Map.Entry<String, List<LocalTime[]>> entry : grouped.entrySet()) {
            String[] keyParts = entry.getKey().split("\\|", 2);
            int day = Integer.parseInt(keyParts[0]);
            String court = keyParts[1];
            List<LocalTime[]> ranges = entry.getValue();
            ranges.sort(Comparator.comparing(a -> a[0]));

            LocalTime segStart = ranges.get(0)[0];
            LocalTime segEnd = ranges.get(0)[1];
            for (int i = 1; i < ranges.size(); i++) {
                LocalTime curStart = ranges.get(i)[0];
                LocalTime curEnd = ranges.get(i)[1];
                if (curStart.equals(segEnd)) {
                    segEnd = curEnd;
                } else {
                    chunks.add(dayOfWeekLabel(day) + " - " + court + ": "
                            + segStart.format(TF) + "-" + segEnd.format(TF));
                    segStart = curStart;
                    segEnd = curEnd;
                }
            }
            chunks.add(dayOfWeekLabel(day) + " - " + court + ": "
                    + segStart.format(TF) + "-" + segEnd.format(TF));
        }

        return String.join("\n", chunks);
    }

    /**
     * Builds recurring sessions similar to preview screen: grouped by booking date + court + contiguous time.
     */
    private List<MergedSlotDTO> mergeSlotsByDate(List<BookingSlotDetailDTO> slots, boolean ascendingDate) {
        if (slots == null || slots.isEmpty()) return new ArrayList<>();

        LinkedHashMap<String, List<BookingSlotDetailDTO>> grouped = new LinkedHashMap<>();
        for (BookingSlotDetailDTO s : slots) {
            if (s.getBookingDate() == null || s.getStartTime() == null || s.getEndTime() == null) {
                continue;
            }
            String key = s.getBookingDate() + "|" + s.getCourtName();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        List<MergedSlotDTO> merged = new ArrayList<>();
        for (Map.Entry<String, List<BookingSlotDetailDTO>> entry : grouped.entrySet()) {
            String[] keyParts = entry.getKey().split("\\|", 2);
            LocalDate bookingDate = LocalDate.parse(keyParts[0]);
            String courtName = keyParts[1];

            List<BookingSlotDetailDTO> list = entry.getValue();
            list.sort(Comparator.comparing(s -> LocalTime.parse(s.getStartTime(), TF)));

            BookingSlotDetailDTO first = list.get(0);
            LocalTime segStart = LocalTime.parse(first.getStartTime(), TF);
            LocalTime segEnd = LocalTime.parse(first.getEndTime(), TF);
            BigDecimal price = first.getPrice() != null ? first.getPrice() : BigDecimal.ZERO;
            int count = 1;

            for (int i = 1; i < list.size(); i++) {
                BookingSlotDetailDTO cur = list.get(i);
                LocalTime curStart = LocalTime.parse(cur.getStartTime(), TF);
                LocalTime curEnd = LocalTime.parse(cur.getEndTime(), TF);
                BigDecimal curPrice = cur.getPrice() != null ? cur.getPrice() : BigDecimal.ZERO;

                if (curStart.equals(segEnd)) {
                    segEnd = curEnd;
                    price = price.add(curPrice);
                    count++;
                } else {
                    merged.add(new MergedSlotDTO(courtName, segStart.format(TF), segEnd.format(TF), price, count, bookingDate));
                    segStart = curStart;
                    segEnd = curEnd;
                    price = curPrice;
                    count = 1;
                }
            }
            merged.add(new MergedSlotDTO(courtName, segStart.format(TF), segEnd.format(TF), price, count, bookingDate));
        }

        Comparator<MergedSlotDTO> comparator = Comparator
                .comparing(MergedSlotDTO::getBookingDate,
                        ascendingDate ? Comparator.naturalOrder() : Comparator.reverseOrder())
                .thenComparing(MergedSlotDTO::getStartTime)
                .thenComparing(MergedSlotDTO::getCourtName);
        merged.sort(comparator);
        return merged;
    }

    private String dayOfWeekLabel(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Chủ nhật";
            case 2 -> "Thứ hai";
            case 3 -> "Thứ ba";
            case 4 -> "Thứ tư";
            case 5 -> "Thứ năm";
            case 6 -> "Thứ sáu";
            case 7 -> "Thứ bảy";
            default -> "Thứ " + dayOfWeek;
        };
    }
}

