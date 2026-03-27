package com.bcb.service.email.impl;

import com.bcb.dto.email.BookingEmailHeaderDTO;
import com.bcb.dto.email.BookingEmailSlotDTO;
import com.bcb.dto.email.BookingRecipientDTO;
import com.bcb.dto.email.BookingRecurringHeaderDTO;
import com.bcb.dto.email.BookingRecurringPatternDTO;
import com.bcb.dto.email.EmailQueueItemDTO;
import com.bcb.repository.email.BookingEmailRepository;
import com.bcb.repository.email.EmailQueueRepository;
import com.bcb.repository.email.impl.BookingEmailRepositoryImpl;
import com.bcb.repository.email.impl.EmailQueueRepositoryImpl;
import com.bcb.service.SendEmailService;
import com.bcb.service.email.EmailQueueService;
import com.bcb.service.impl.SendEmailServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmailQueueServiceImpl implements EmailQueueService {

    private static final int MAX_RETRY = 5;
    private static final int[] BACKOFF_MINUTES = new int[]{1, 5, 30, 60, 60};

    private final EmailQueueRepository emailQueueRepository = new EmailQueueRepositoryImpl();
    private final BookingEmailRepository bookingEmailRepository = new BookingEmailRepositoryImpl();
    private final SendEmailService sendEmailService = new SendEmailServiceImpl();

    /**
     * Enqueues a booking created email (staff-created only).
     */
    @Override
    public EmailEnqueueResult enqueueBookingCreated(int bookingId) {
        return enqueueIfPossible("CREATE", bookingId, null);
    }

    /**
     * Enqueues a recurring booking created email (staff-created only).
     */
    @Override
    public EmailEnqueueResult enqueueRecurringBookingCreated(int bookingId) {
        return enqueueIfPossible("CREATE_RECURRING", bookingId, null);
    }

    /**
     * Enqueues a booking update email (staff-created only).
     */
    @Override
    public void enqueueBookingUpdated(int bookingId, String payloadJson) {
        enqueueIfPossible("UPDATE", bookingId, payloadJson);
    }

    /**
     * Enqueues a booking cancel email (staff-created only).
     */
    @Override
    public void enqueueBookingCancelled(int bookingId, String payloadJson) {
        enqueueIfPossible("CANCEL", bookingId, payloadJson);
    }

    @Override
    public EmailEnqueueResult enqueueCustomerPaymentSuccess(int bookingId, String paymentType) {
        String normalizedType = paymentType == null ? "" : paymentType.trim().toUpperCase();
        String emailType = "REMAINING".equals(normalizedType) ? "PAY_REMAINING" : "PAY_SUCCESS";
        String payload = "{\"paymentType\":\"" + normalizedType + "\"}";
        return enqueueForCustomer(emailType, bookingId, payload);
    }

    @Override
    public EmailEnqueueResult enqueueCustomerRemainingPaid(int bookingId) {
        return enqueueForCustomer("PAY_REMAINING", bookingId, "{\"paymentType\":\"REMAINING\"}");
    }

    /**
     * Processes pending emails and updates queue status with retry/backoff.
     */
    @Override
    public void processPendingEmails() {
        /*
         * Queue processing loop:
         * - Fetch pending rows and mark as SENDING.
         * - Build email content and send.
         * - Mark SENT or FAILED with backoff.
         */
        List<EmailQueueItemDTO> items;
        try {
            items = emailQueueRepository.findAndMarkPending(20);
        } catch (Exception e) {
            return;
        }

        for (EmailQueueItemDTO item : items) {
            try {
                EmailContent content = buildEmailContent(item);
                if (content == null) {
                    markFailed(item, "Không có nội dung", true);
                    continue;
                }
                sendEmailService.send(item.getToEmail(), content.subject, content.body);
                emailQueueRepository.markSent(item.getEmailId());
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.length() > 500) {
                    msg = msg.substring(0, 500);
                }
                markFailed(item, msg, false);
            }
        }
    }

    private EmailEnqueueResult enqueueIfPossible(String emailType, int bookingId, String payloadJson) {
        try {
            BookingRecipientDTO recipient = bookingEmailRepository.findRecipient(bookingId);
            // Only enqueue emails for staff-created bookings.
            if (recipient == null || recipient.getStaffId() == null) {
                return new EmailEnqueueResult(false, "Không phải booking do staff tạo. Không xếp email.");
            }
            String toEmail = recipient.getEmail();
            // Skip enqueue when recipient email is missing.
            if (toEmail == null || toEmail.trim().isEmpty()) {
                return new EmailEnqueueResult(false, "Không có email. Không xếp thông báo.");
            }
            emailQueueRepository.enqueue(emailType, bookingId, toEmail.trim(), payloadJson, null);
            return new EmailEnqueueResult(true, null);
        } catch (Exception e) {
            return new EmailEnqueueResult(false, "Không thể xếp email vào hàng đợi.");
        }
    }

    private void markFailed(EmailQueueItemDTO item, String lastError, boolean forceFailed) {
        int nextRetry = item.getRetryCount() + 1;
        boolean exhausted = nextRetry >= MAX_RETRY;
        String status = (forceFailed || exhausted) ? "FAILED" : "PENDING";
        // Update status to SENT/FAILED with retry backoff.
        int backoffIndex = Math.min(nextRetry - 1, BACKOFF_MINUTES.length - 1);
        LocalDateTime nextAttempt = LocalDateTime.now().plusMinutes(BACKOFF_MINUTES[backoffIndex]);
        try {
            emailQueueRepository.markFailed(item.getEmailId(), nextRetry, nextAttempt, lastError, status);
        } catch (Exception ignored) {
        }
    }

    private EmailContent buildEmailContent(EmailQueueItemDTO item) throws Exception {
        if (item == null) return null;
        String type = item.getEmailType();
        if (type == null) return null;

        switch (type) {
            case "CREATE":
                return buildCreateEmail(item.getBookingId());
            case "UPDATE":
                return buildUpdateEmail(item.getBookingId(), item.getPayloadJson());
            case "CANCEL":
                return buildCancelEmail(item.getBookingId(), item.getPayloadJson());
            case "CREATE_RECURRING":
                return buildRecurringCreateEmail(item.getBookingId());
            case "REMINDER_UPCOMING_24H":
                return buildUpcomingReminderEmail(item.getBookingId(), 24);
            case "REMINDER_UPCOMING_2H":
                return buildUpcomingReminderEmail(item.getBookingId(), 2);
            case "REMINDER_PAYMENT_12H":
                return buildPaymentReminderEmail(item.getBookingId(), 12);
            case "PAY_SUCCESS":
                return buildCustomerPaymentSuccessEmail(item.getBookingId(), item.getPayloadJson());
            case "PAY_REMAINING":
                return buildCustomerRemainingPaidEmail(item.getBookingId(), item.getPayloadJson());
            case "REMINDER_CUS_24H":
                return buildUpcomingReminderEmail(item.getBookingId(), 24);
            default:
                return null;
        }
    }

    private EmailEnqueueResult enqueueForCustomer(String emailType, int bookingId, String payloadJson) {
        try {
            BookingRecipientDTO recipient = bookingEmailRepository.findRecipient(bookingId);
            // Only enqueue emails for staff-created bookings.
            if (recipient == null) {
                return new EmailEnqueueResult(false, "Không tìm thấy người nhận.");
            }
            if (recipient.getStaffId() != null) {
                return new EmailEnqueueResult(false, "Booking do staff tạo. Bỏ qua email customer.");
            }
            String toEmail = recipient.getEmail();
            // Skip enqueue when recipient email is missing.
            if (toEmail == null || toEmail.trim().isEmpty()) {
                return new EmailEnqueueResult(false, "Không có email. Không xếp thông báo.");
            }
            emailQueueRepository.enqueue(emailType, bookingId, toEmail.trim(), payloadJson, null);
            return new EmailEnqueueResult(true, null);
        } catch (Exception e) {
            return new EmailEnqueueResult(false, "Không thể xếp email vào hàng đợi.");
        }
    }

    private EmailContent buildCreateEmail(int bookingId) throws Exception {
        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;
        List<BookingEmailSlotDTO> slots = bookingEmailRepository.findSlots(bookingId);
        List<EmailSession> sessions = buildSessionsFromSlots(slots);

        String subject = "Xác nhận đặt sân";
        StringBuilder body = new StringBuilder(1024);
        body.append("<h2>Xác nhận đặt sân</h2>");
        appendHeader(body, header);
        body.append("<h3>Chi tiết phiên</h3>");
        body.append(renderSessions(sessions));
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

    private EmailContent buildCancelEmail(int bookingId, String payloadJson) throws Exception {
        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;
        List<BookingEmailSlotDTO> slots = bookingEmailRepository.findSlots(bookingId);
        List<EmailSession> sessions = buildSessionsFromSlots(slots);
        String reason = SimpleJson.extractString(payloadJson, "reason");

        String subject = "Hủy đặt sân";
        StringBuilder body = new StringBuilder(1024);
        body.append("<h2>Hủy đặt sân</h2>");
        appendHeader(body, header);
        if (reason != null && !reason.isEmpty()) {
            body.append("<p>Lý do: ").append(escapeHtml(reason)).append("</p>");
        }
        body.append("<h3>Chi tiết phiên</h3>");
        body.append(renderSessions(sessions));
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

        private EmailContent buildRecurringCreateEmail(int bookingId) throws Exception {
        BookingRecurringHeaderDTO header = bookingEmailRepository.findRecurringHeader(bookingId);
        if (header == null) return null;
        List<BookingRecurringPatternDTO> patterns = bookingEmailRepository.findRecurringPatterns(bookingId);

        String subject = "Xác nhận đặt sân định kỳ";
        StringBuilder body = new StringBuilder(1200);
        body.append("<h2>").append(subject).append("</h2>");
        body.append("<p>Mã booking: ").append(header.getBookingId()).append("</p>");
        body.append("<p>Sân: ").append(escapeHtml(safe(header.getFacilityName()))).append("</p>");
        body.append("<p>Khách hàng: ").append(escapeHtml(safe(header.getCustomerName()))).append("</p>");
        if (header.getCustomerPhone() != null && !header.getCustomerPhone().isEmpty()) {
            body.append("<p>Điện thoại: ").append(escapeHtml(header.getCustomerPhone())).append("</p>");
        }
        if (header.getStartDate() != null && header.getEndDate() != null) {
            body.append("<p>Lịch định kỳ: ")
                    .append(escapeHtml(header.getStartDate()))
                    .append(" → ")
                    .append(escapeHtml(header.getEndDate()))
                    .append("</p>");
        }

        body.append("<h3>Lịch định kỳ</h3>");
        body.append(renderRecurringPatterns(patterns));

        if (header.getTotalAmount() != null) {
            body.append("<p>Tổng tiền: ").append(escapeHtml(formatMoney(header.getTotalAmount()))).append("</p>");
        }
        if (header.getPaidAmount() != null) {
            body.append("<p>Đã thanh toán: ").append(escapeHtml(formatMoney(header.getPaidAmount()))).append("</p>");
        }
        if (header.getPaymentStatus() != null) {
            body.append("<p>Trạng thái thanh toán: ").append(escapeHtml(header.getPaymentStatus())).append("</p>");
        }
        return new EmailContent(subject, body.toString());
    }

    private String renderRecurringPatterns(List<BookingRecurringPatternDTO> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return "<p>Không có lịch định kỳ.</p>";
        }
        StringBuilder sb = new StringBuilder(512);
        sb.append("<ul>");
        for (BookingRecurringPatternDTO p : patterns) {
            sb.append("<li>")
                    .append(escapeHtml(dayOfWeekLabel(p.getDayOfWeek())))
                    .append(" - ")
                    .append(escapeHtml(safe(p.getCourtName())))
                    .append(" - ")
                    .append(escapeHtml(safe(p.getStartTime())))
                    .append("-")
                    .append(escapeHtml(safe(p.getEndTime())))
                    .append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String dayOfWeekLabel(int dow) {
        switch (dow) {
            case 1: return "Chủ nhật";
            case 2: return "Thứ 2";
            case 3: return "Thứ 3";
            case 4: return "Thứ 4";
            case 5: return "Thứ 5";
            case 6: return "Thứ 6";
            case 7: return "Thứ 7";
            default: return "Không rõ";
        }
    }
private EmailContent buildUpdateEmail(int bookingId, String payloadJson) throws Exception {
        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;

        String beforeJson = SimpleJson.extractObject(payloadJson, "before");
        String afterJson = SimpleJson.extractObject(payloadJson, "after");
        String reason = SimpleJson.extractString(payloadJson, "reason");

        List<SlotSnapshot> beforeSlots = parseSnapshotSlots(beforeJson);
        List<SlotSnapshot> afterSlots = parseSnapshotSlots(afterJson);

        Set<Integer> courtIds = new HashSet<>();
        Set<Integer> slotIds = new HashSet<>();
        collectIds(beforeSlots, courtIds, slotIds);
        collectIds(afterSlots, courtIds, slotIds);

        Map<Integer, String> courtNames = bookingEmailRepository.findCourtNames(courtIds);
        Map<Integer, LocalTime[]> slotTimes = bookingEmailRepository.findSlotTimes(slotIds);

        List<EmailSession> beforeSessions = buildSessionsFromSnapshots(beforeSlots, courtNames, slotTimes);
        List<EmailSession> afterSessions = buildSessionsFromSnapshots(afterSlots, courtNames, slotTimes);

        String subject = "Cập nhật đặt sân";
        StringBuilder body = new StringBuilder(1200);
        body.append("<h2>Cập nhật đặt sân</h2>");
        appendHeader(body, header);
        if (reason != null && !reason.isEmpty()) {
            body.append("<p>Lý do: ").append(escapeHtml(reason)).append("</p>");
        }

        body.append("<h3>Trước khi thay đổi</h3>");
        body.append(renderSessions(beforeSessions));
        body.append("<h3>Sau khi thay đổi</h3>");
        body.append(renderSessions(afterSessions));
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

        private EmailContent buildUpcomingReminderEmail(int bookingId, int leadHours) throws Exception {
        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;
        List<BookingEmailSlotDTO> slots = bookingEmailRepository.findSlots(bookingId);
        List<EmailSession> sessions = buildSessionsFromSlots(slots);

        String subject = leadHours == 24
                ? "Nhắc lịch chơi sắp tới (còn 24 giờ)"
                : "Nhắc lịch chơi sắp tới (còn 2 giờ)";

        StringBuilder body = new StringBuilder(1024);
        body.append("<h2>").append(subject).append("</h2>");
        appendHeader(body, header);
        String startInfo = buildStartInfo(header, sessions);
        if (!startInfo.isEmpty()) {
            body.append("<p>").append(startInfo).append("</p>");
        }
        body.append("<h3>Chi tiết phiên</h3>");
        body.append(renderSessions(sessions));
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

    private EmailContent buildPaymentReminderEmail(int bookingId, int leadHours) throws Exception {
        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;
        List<BookingEmailSlotDTO> slots = bookingEmailRepository.findSlots(bookingId);
        List<EmailSession> sessions = buildSessionsFromSlots(slots);

        String subject = "Nhắc thanh toán (còn " + leadHours + " giờ)";
        StringBuilder body = new StringBuilder(1024);
        body.append("<h2>").append(subject).append("</h2>");
        appendHeader(body, header);
        String startInfo = buildStartInfo(header, sessions);
        if (!startInfo.isEmpty()) {
            body.append("<p>").append(startInfo).append("</p>");
        }
        body.append("<h3>Chi tiết phiên</h3>");
        body.append(renderSessions(sessions));

        BigDecimal total = header.getTotalAmount() != null ? header.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = header.getPaidAmount() != null ? header.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remain = total.subtract(paid);
        if (remain.compareTo(BigDecimal.ZERO) < 0) remain = BigDecimal.ZERO;
        body.append("<p>Còn phải thanh toán: ").append(escapeHtml(formatMoney(remain))).append("</p>");
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

    private EmailContent buildCustomerPaymentSuccessEmail(int bookingId, String payloadJson) throws Exception {
        String paymentType = SimpleJson.extractString(payloadJson, "paymentType");
        return buildCustomerPaymentEmail(bookingId, paymentType, false);
    }

    private EmailContent buildCustomerRemainingPaidEmail(int bookingId, String payloadJson) throws Exception {
        String paymentType = SimpleJson.extractString(payloadJson, "paymentType");
        return buildCustomerPaymentEmail(bookingId, paymentType, true);
    }

    private EmailContent buildCustomerPaymentEmail(int bookingId, String paymentType, boolean remainingOnly) throws Exception {
        BookingRecurringHeaderDTO recurringHeader = bookingEmailRepository.findRecurringHeader(bookingId);
        if (recurringHeader != null) {
            List<BookingRecurringPatternDTO> patterns = bookingEmailRepository.findRecurringPatterns(bookingId);
            String subject = remainingOnly
                    ? "Thanh toán phần còn lại thành công"
                    : buildPaymentSuccessSubject(paymentType);
            StringBuilder body = new StringBuilder(1200);
            body.append("<h2>").append(subject).append("</h2>");
            body.append("<p>Mã booking: ").append(recurringHeader.getBookingId()).append("</p>");
            body.append("<p>Sân: ").append(escapeHtml(safe(recurringHeader.getFacilityName()))).append("</p>");
            body.append("<p>Khách hàng: ").append(escapeHtml(safe(recurringHeader.getCustomerName()))).append("</p>");
            if (recurringHeader.getCustomerPhone() != null && !recurringHeader.getCustomerPhone().isEmpty()) {
                body.append("<p>Điện thoại: ").append(escapeHtml(recurringHeader.getCustomerPhone())).append("</p>");
            }
            if (recurringHeader.getStartDate() != null && recurringHeader.getEndDate() != null) {
                body.append("<p>Lịch định kỳ: ")
                        .append(escapeHtml(recurringHeader.getStartDate()))
                        .append(" → ")
                        .append(escapeHtml(recurringHeader.getEndDate()))
                        .append("</p>");
            }
            body.append("<h3>Lịch định kỳ</h3>");
            body.append(renderRecurringPatterns(patterns));
            appendRecurringPayment(body, recurringHeader, remainingOnly);
            return new EmailContent(subject, body.toString());
        }

        BookingEmailHeaderDTO header = bookingEmailRepository.findHeader(bookingId);
        if (header == null) return null;
        List<BookingEmailSlotDTO> slots = bookingEmailRepository.findSlots(bookingId);
        List<EmailSession> sessions = buildSessionsFromSlots(slots);

        String subject = remainingOnly
                ? "Thanh toán phần còn lại thành công"
                : buildPaymentSuccessSubject(paymentType);
        StringBuilder body = new StringBuilder(1100);
        body.append("<h2>").append(subject).append("</h2>");
        appendHeader(body, header);
        body.append("<h3>Chi tiết phiên</h3>");
        body.append(renderSessions(sessions));

        if (remainingOnly) {
            body.append("<p>Bạn đã thanh toán xong phần còn lại cho booking này.</p>");
        } else {
            body.append("<p>Giao dịch thanh toán của bạn đã được ghi nhận thành công.</p>");
        }
        appendPayment(body, header);
        return new EmailContent(subject, body.toString());
    }

    private void appendRecurringPayment(StringBuilder body, BookingRecurringHeaderDTO header, boolean remainingOnly) {
        if (header.getTotalAmount() != null) {
            body.append("<p>Tổng tiền: ").append(escapeHtml(formatMoney(header.getTotalAmount()))).append("</p>");
        }
        if (header.getPaidAmount() != null) {
            body.append("<p>Đã thanh toán: ").append(escapeHtml(formatMoney(header.getPaidAmount()))).append("</p>");
        }
        if (remainingOnly) {
            body.append("<p>Bạn đã thanh toán xong phần còn lại cho booking định kỳ này.</p>");
        }
        if (header.getPaymentStatus() != null) {
            body.append("<p>Trạng thái thanh toán: ").append(escapeHtml(header.getPaymentStatus())).append("</p>");
        }
    }

    private String buildPaymentSuccessSubject(String paymentType) {
        String normalizedType = paymentType == null ? "" : paymentType.trim().toUpperCase();
        switch (normalizedType) {
            case "DEPOSIT":
                return "Thanh toán đặt cọc thành công";
            case "FULL":
                return "Thanh toán toàn phần thành công";
            case "REMAINING":
                return "Thanh toán phần còn lại thành công";
            default:
                return "Thanh toán thành công";
        }
    }

    private String buildStartInfo(BookingEmailHeaderDTO header, List<EmailSession> sessions) {
        if (header == null || header.getBookingDate() == null) return "";
        if (sessions == null || sessions.isEmpty()) return "";

        LocalTime earliest = null;
        for (EmailSession s : sessions) {
            if (s == null || s.start == null) continue;
            if (earliest == null || s.start.isBefore(earliest)) {
                earliest = s.start;
            }
        }
        if (earliest == null) return "";
        String time = fmtTime(earliest);
        if (time.isEmpty()) return "";
        return "Lịch chơi bắt đầu lúc " + time + " ngày " + header.getBookingDate();
    }
private void appendHeader(StringBuilder body, BookingEmailHeaderDTO header) {
        body.append("<p>Mã booking: ").append(header.getBookingId()).append("</p>");
        body.append("<p>Sân: ").append(escapeHtml(safe(header.getFacilityName()))).append("</p>");
        body.append("<p>Ngày: ").append(escapeHtml(safe(header.getBookingDate()))).append("</p>");
        body.append("<p>Khách hàng: ").append(escapeHtml(safe(header.getCustomerName()))).append("</p>");
        if (header.getCustomerPhone() != null && !header.getCustomerPhone().isEmpty()) {
            body.append("<p>Điện thoại: ").append(escapeHtml(header.getCustomerPhone())).append("</p>");
        }
    }

    private void appendPayment(StringBuilder body, BookingEmailHeaderDTO header) {
        if (header.getTotalAmount() != null) {
            body.append("<p>Tổng tiền: ").append(escapeHtml(formatMoney(header.getTotalAmount()))).append("</p>");
        }
        if (header.getPaidAmount() != null) {
            body.append("<p>Đã thanh toán: ").append(escapeHtml(formatMoney(header.getPaidAmount()))).append("</p>");
        }
        if (header.getPaymentStatus() != null) {
            body.append("<p>Trạng thái thanh toán: ").append(escapeHtml(header.getPaymentStatus())).append("</p>");
        }
    }

    private List<EmailSession> buildSessionsFromSlots(List<BookingEmailSlotDTO> slots) {
        if (slots == null || slots.isEmpty()) return Collections.emptyList();

        Map<Integer, List<SlotWithTime>> byCourt = new HashMap<>();
        for (BookingEmailSlotDTO slot : slots) {
            if (slot.getSlotStatus() != null && "CANCELLED".equalsIgnoreCase(slot.getSlotStatus())) {
                continue;
            }
            LocalTime start = parseTime(slot.getStartTime());
            LocalTime end = parseTime(slot.getEndTime());
            if (start == null || end == null) continue;

            SlotWithTime s = new SlotWithTime();
            s.courtId = slot.getCourtId();
            s.courtName = slot.getCourtName();
            s.start = start;
            s.end = end;
            s.price = slot.getPrice();
            byCourt.computeIfAbsent(slot.getCourtId(), k -> new ArrayList<>()).add(s);
        }
        return groupSessions(byCourt);
    }

    private List<EmailSession> buildSessionsFromSnapshots(List<SlotSnapshot> slots,
                                                         Map<Integer, String> courtNames,
                                                         Map<Integer, LocalTime[]> slotTimes) {
        if (slots == null || slots.isEmpty()) return Collections.emptyList();

        Map<Integer, List<SlotWithTime>> byCourt = new HashMap<>();
        for (SlotSnapshot slot : slots) {
            if (slot.slotStatus != null && "CANCELLED".equalsIgnoreCase(slot.slotStatus)) {
                continue;
            }
            LocalTime[] times = slotTimes.get(slot.slotId);
            if (times == null || times[0] == null || times[1] == null) continue;

            SlotWithTime s = new SlotWithTime();
            s.courtId = slot.courtId;
            s.courtName = courtNames.getOrDefault(slot.courtId, "Sân " + slot.courtId);
            s.start = times[0];
            s.end = times[1];
            s.price = slot.price;
            byCourt.computeIfAbsent(slot.courtId, k -> new ArrayList<>()).add(s);
        }
        return groupSessions(byCourt);
    }

    private List<EmailSession> groupSessions(Map<Integer, List<SlotWithTime>> byCourt) {
        List<EmailSession> sessions = new ArrayList<>();
        for (List<SlotWithTime> list : byCourt.values()) {
            list.sort((a, b) -> a.start.compareTo(b.start));

            EmailSession current = null;
            for (SlotWithTime slot : list) {
                if (current == null) {
                    current = new EmailSession(slot.courtName, slot.start, slot.end, slot.price, 1);
                    continue;
                }
                if (current.end.equals(slot.start)) {
                    current.end = slot.end;
                    current.slotCount += 1;
                    current.totalPrice = add(current.totalPrice, slot.price);
                } else {
                    sessions.add(current);
                    current = new EmailSession(slot.courtName, slot.start, slot.end, slot.price, 1);
                }
            }
            if (current != null) sessions.add(current);
        }
        sessions.sort((a, b) -> {
            int cmp = a.courtName.compareToIgnoreCase(b.courtName);
            if (cmp != 0) return cmp;
            return a.start.compareTo(b.start);
        });
        return sessions;
    }

    private String renderSessions(List<EmailSession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return "<p>Không có phiên.</p>";
        }
        StringBuilder sb = new StringBuilder(512);
        sb.append("<ul>");
        for (EmailSession s : sessions) {
            sb.append("<li>")
                    .append(escapeHtml(s.courtName))
                    .append(" - ")
                    .append(escapeHtml(fmtTime(s.start)))
                    .append("-")
                    .append(escapeHtml(fmtTime(s.end)))
                    .append(" (số slot: ")
                    .append(s.slotCount)
                    .append(", tổng: ")
                    .append(escapeHtml(formatMoney(s.totalPrice)))
                    .append(")</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private List<SlotSnapshot> parseSnapshotSlots(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isEmpty()) return Collections.emptyList();
        String arr = SimpleJson.extractArray(snapshotJson, "slots");
        if (arr == null || arr.length() < 2) return Collections.emptyList();

        List<SlotSnapshot> out = new ArrayList<>();
        int pos = 0;
        while (pos < arr.length()) {
            int objStart = arr.indexOf('{', pos);
            if (objStart < 0) break;
            int objEnd = arr.indexOf('}', objStart);
            if (objEnd < 0) break;
            String obj = arr.substring(objStart, objEnd + 1);

            SlotSnapshot slot = new SlotSnapshot();
            slot.courtId = parseInt(SimpleJson.extractString(obj, "courtId"));
            slot.slotId = parseInt(SimpleJson.extractString(obj, "slotId"));
            slot.slotStatus = SimpleJson.extractString(obj, "slotStatus");
            slot.price = parseMoney(SimpleJson.extractString(obj, "price"));
            if (slot.courtId > 0 && slot.slotId > 0) {
                out.add(slot);
            }
            pos = objEnd + 1;
        }
        return out;
    }

    private void collectIds(List<SlotSnapshot> slots, Set<Integer> courtIds, Set<Integer> slotIds) {
        if (slots == null) return;
        for (SlotSnapshot s : slots) {
            if (s.courtId > 0) courtIds.add(s.courtId);
            if (s.slotId > 0) slotIds.add(s.slotId);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String formatMoney(BigDecimal v) {
        if (v == null) return "0";
        return v.stripTrailingZeros().toPlainString();
    }

    private BigDecimal add(BigDecimal a, BigDecimal b) {
        if (a == null) return b == null ? BigDecimal.ZERO : b;
        if (b == null) return a;
        return a.add(b);
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String fmtTime(LocalTime t) {
        if (t == null) return "";
        String v = t.toString();
        return v.length() >= 5 ? v.substring(0, 5) : v;
    }

    private int parseInt(String s) {
        if (s == null) return -1;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal parseMoney(String s) {
        if (s == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static class EmailContent {
        final String subject;
        final String body;

        EmailContent(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }
    }

    private static class SlotSnapshot {
        int courtId;
        int slotId;
        String slotStatus;
        BigDecimal price;
    }

    private static class SlotWithTime {
        int courtId;
        String courtName;
        LocalTime start;
        LocalTime end;
        BigDecimal price;
    }

    private static class EmailSession {
        String courtName;
        LocalTime start;
        LocalTime end;
        BigDecimal totalPrice;
        int slotCount;

        EmailSession(String courtName, LocalTime start, LocalTime end, BigDecimal totalPrice, int slotCount) {
            this.courtName = courtName;
            this.start = start;
            this.end = end;
            this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO;
            this.slotCount = slotCount;
        }
    }

    private static class SimpleJson {
        static String extractString(String json, String key) {
            if (json == null) return null;
            String search = '"' + key + '"';
            int idx = json.indexOf(search);
            if (idx < 0) return null;

            idx = json.indexOf(':', idx + search.length());
            if (idx < 0) return null;

            idx++;
            while (idx < json.length() && json.charAt(idx) == ' ') idx++;
            if (idx >= json.length()) return null;

            if (json.charAt(idx) == 'n') return null;

            if (json.charAt(idx) == '"') {
                int end = idx + 1;
                while (end < json.length()) {
                    if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                        return json.substring(idx + 1, end);
                    }
                    end++;
                }
                return null;
            }

            int end = idx;
            while (end < json.length() && ",} ]".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(idx, end);
        }

        static String extractArray(String json, String key) {
            if (json == null) return null;
            String search = '"' + key + '"';
            int idx = json.indexOf(search);
            if (idx < 0) return null;

            int start = json.indexOf('[', idx + search.length());
            if (start < 0) return null;

            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
            return null;
        }

        static String extractObject(String json, String key) {
            if (json == null) return null;
            String search = '"' + key + '"';
            int idx = json.indexOf(search);
            if (idx < 0) return null;

            int start = json.indexOf('{', idx + search.length());
            if (start < 0) return null;

            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
            return null;
        }
    }
}




















