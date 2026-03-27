package com.bcb.service.impl;

import com.bcb.utils.staff.StaffAuthUtil;
import com.bcb.utils.staff.StaffBookingSnapshotTokenUtil;
import com.bcb.dto.staff.StaffBookingEditExistingSlotDTO;
import com.bcb.dto.staff.StaffBookingEditOutcomeDTO;
import com.bcb.dto.staff.StaffBookingEditSessionCellDTO;
import com.bcb.dto.staff.StaffBookingEditSlotStateDTO;
import com.bcb.dto.staff.StaffBookingEditStatusCountDTO;
import com.bcb.repository.impl.StaffBookingEditRepositoryImpl;
import com.bcb.repository.staff.StaffBookingEditRepository;
import com.bcb.service.staff.StaffBookingEditService;
import com.bcb.service.email.EmailQueueService;
import com.bcb.service.email.impl.EmailQueueServiceImpl;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaffBookingEditServiceImpl implements StaffBookingEditService {

    private final StaffBookingEditRepository repository = new StaffBookingEditRepositoryImpl();
    private final EmailQueueService emailQueueService = new EmailQueueServiceImpl();

    @Override
    public StaffBookingEditOutcomeDTO process(String servletPath, int facilityId, int staffId, String body) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            String json;
            if (servletPath.endsWith("/edit/preview")) {
                json = handlePreview(conn, facilityId, body);
                return out(200, json);
            } else if (servletPath.endsWith("/edit/save")) {
                json = handleSave(conn, facilityId, staffId, body);
                return out(200, json);
            } else if (servletPath.endsWith("/release-slot")) {
                json = handleReleaseSlot(conn, facilityId, staffId, body);
                return out(200, json);
            } else if (servletPath.endsWith("/cancel")) {
                json = handleCancel(conn, facilityId, staffId, body);
                return out(200, json);
            } else {
                return out(404, "{\"success\":false,\"message\":\"API không tồn tại\"}");
            }
        } catch (ApiException e) {
            return out(e.status, e.toJson());
        }
    }

    private String handlePreview(Connection conn, int facilityId, String body) throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        if (bookingId <= 0) {
            throw new ApiException(400, "Thiếu bookingId");
        }

        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String etag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        String totalAmount = snapshot.invoice != null ? snapshot.invoice.totalAmount : "0";
        String paidAmount = snapshot.invoice != null ? snapshot.invoice.paidAmount : "0";
        String paymentStatus = snapshot.invoice != null ? snapshot.invoice.paymentStatus : "UNPAID";
        String refundDue = snapshot.invoice != null ? snapshot.invoice.refundDue : "0";
        String refundStatus = snapshot.invoice != null ? snapshot.invoice.refundStatus : "NONE";

        StringBuilder json = new StringBuilder(320);
        json.append("{\"success\":true,\"data\":{");
        json.append("\"bookingId\":").append(snapshot.bookingId);
        json.append(",\"bookingStatus\":\"").append(snapshot.bookingStatus).append("\"");
        json.append(",\"slotCount\":").append(snapshot.slots.size());
        json.append(",\"totalAmount\":").append(totalAmount);
        json.append(",\"paidAmount\":").append(paidAmount);
        json.append(",\"paymentStatus\":\"").append(paymentStatus).append("\"");
        json.append(",\"refundDue\":").append(refundDue);
        json.append(",\"refundStatus\":\"").append(refundStatus).append("\"");
        json.append(",\"etag\":").append(StaffAuthUtil.escapeJson(etag));
        json.append("}}");
        return json.toString();
    }

    private String handleSave(Connection conn, int facilityId, int staffId, String body) throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));

        List<SlotPair> addSlots = parseSlotPairs(body, "addSlots");
        Set<Integer> removeSlotIds = parseIntSet(body, "removeBookingSlotIds");

        if (bookingId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId hoặc etag");
        }
        if (addSlots.isEmpty() && removeSlotIds.isEmpty()) {
            throw new ApiException(400, "Không có thay đổi để lưu");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertConfirmedSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            ensureEditableRemovals(conn, bookingId, removeSlotIds);
            ensureNoDuplicateAddPairs(addSlots);
            validateSessionRuleAfterEdit(conn, bookingId, removeSlotIds, addSlots);

            Map<Integer, BigDecimal> removedRentalBySlotId = new HashMap<>();
            for (Integer bookingSlotId : removeSlotIds) {
                BigDecimal rentalRemoved = cancelPendingSlot(conn, bookingId, bookingSlotId);
                if (rentalRemoved.compareTo(BigDecimal.ZERO) > 0) {
                    removedRentalBySlotId.put(bookingSlotId, rentalRemoved);
                }
            }

            LocalDate bookingDate = LocalDate.parse(before.bookingDate);
            ensureAddSlotsNotExpired(conn, bookingDate, addSlots);
            for (SlotPair slot : addSlots) {
                upsertPendingSlot(conn, bookingId, facilityId, bookingDate, slot);
            }

            StaffBookingSnapshotTokenUtil.Snapshot afterSlots =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            InvoiceUpdateResult invoice = recalcInvoice(conn, bookingId, reason, before, afterSlots, removedRentalBySlotId);
            String nextBookingStatus = recomputeBookingStatus(conn, bookingId);
            repository.updateBookingStatus(conn, bookingId, nextBookingStatus);

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "EDIT_SAVE", "CHANGE_ALL",
                    reason, beforeEtag, afterEtag, before, after, invoice.refundDue);

            conn.commit();

            try {
                String payload = buildUpdatePayload(before, after, reason);
                emailQueueService.enqueueBookingUpdated(bookingId, payload);
            } catch (Exception ignored) {
            }
            return "{\"success\":true,\"message\":\"Lưu thay đổi thành công\",\"data\":{"
                    + "\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"bookingStatus\":\"" + nextBookingStatus + "\""
                    + ",\"totalAmount\":" + invoice.totalAmount.toPlainString()
                    + ",\"paidAmount\":" + invoice.paidAmount.toPlainString()
                    + ",\"refundDue\":" + invoice.refundDue.toPlainString()
                    + ",\"refundStatus\":\"" + invoice.refundStatus + "\""
                    + "}}";

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private String handleReleaseSlot(Connection conn, int facilityId, int staffId, String body) throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        int bookingSlotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingSlotId"));
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));

        if (bookingId <= 0 || bookingSlotId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId, bookingSlotId hoặc etag");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertReleaseSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            StaffBookingEditSlotStateDTO slot = repository.findSlotState(conn, bookingId, bookingSlotId);
            if (slot == null) {
                throw new ApiException(404, "Không tìm thấy slot trong booking");
            }
            if (!"NO_SHOW".equals(slot.getSlotStatus())) {
                throw new ApiException(400, "Chỉ cho phép giải phóng slot NO_SHOW");
            }
            Map<Integer, String> beforeStatus = new HashMap<>();
            for (StaffBookingSnapshotTokenUtil.SlotSnapshot s : before.slots) {
                beforeStatus.put(s.bookingSlotId, s.slotStatus);
            }
            List<StaffBookingEditSessionCellDTO> cells = repository.findSessionCellsByBookingId(conn, bookingId);
            Map<Integer, LocalTime> sessionEndBySlotId = buildSessionEndBySlotId(cells, beforeStatus);
            LocalTime sessionEnd = sessionEndBySlotId.get(bookingSlotId);
            if (sessionEnd == null || before.bookingDate == null || before.bookingDate.isEmpty()) {
                throw new ApiException(400, "Không xác định được thời gian kết thúc phiên");
            }
            LocalDate bookingDate = LocalDate.parse(before.bookingDate);
            LocalDateTime sessionEndAt = LocalDateTime.of(bookingDate, sessionEnd);
            if (LocalDateTime.now().isAfter(sessionEndAt)) {
                throw new ApiException(400, "Phiên đã quá giờ, không thể giải phóng slot");
            }

            boolean changed = false;
            if (!slot.isReleased()) {
                repository.deleteCourtSlotBooking(conn, bookingSlotId);
                repository.markSlotReleased(conn, bookingSlotId);
                changed = true;
            }

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "RELEASE_SLOT", "CHANGE_SLOT",
                    reason, beforeEtag, afterEtag, before, after,
                    after.invoice != null ? parseMoney(after.invoice.refundDue) : BigDecimal.ZERO);

            conn.commit();

            return "{\"success\":true,\"message\":"
                    + StaffAuthUtil.escapeJson(changed ? "Giải phóng slot thành công" : "Slot đã được giải phóng trước đó")
                    + ",\"data\":{\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"changed\":" + changed + "}}";

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private String handleCancel(Connection conn, int facilityId, int staffId, String body) throws Exception {
        int bookingId = parseInt(StaffBookingSnapshotTokenUtil.extractString(body, "bookingId"));
        String etag = StaffBookingSnapshotTokenUtil.extractString(body, "etag");
        String reason = trimToNull(StaffBookingSnapshotTokenUtil.extractString(body, "reason"));
        boolean confirmAllRemaining = "true".equalsIgnoreCase(
                String.valueOf(StaffBookingSnapshotTokenUtil.extractString(body, "confirmAllRemaining")));

        if (bookingId <= 0 || etag == null || etag.isEmpty()) {
            throw new ApiException(400, "Thiếu bookingId hoặc etag");
        }
        if (!confirmAllRemaining) {
            throw new ApiException(400, "Bạn cần xác nhận hủy toàn bộ slot còn lại");
        }
        if (reason == null || reason.isEmpty()) {
            throw new ApiException(400, "Vui lòng nhập lý do hủy");
        }

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            StaffBookingSnapshotTokenUtil.Snapshot before = assertConfirmedSnapshot(conn, bookingId, facilityId, etag);
            String beforeEtag = StaffBookingSnapshotTokenUtil.computeEtag(before);

            List<Integer> pendingSlotIds = repository.findPendingSlotIds(conn, bookingId);
            if (pendingSlotIds.isEmpty()) {
                throw new ApiException(400, "Không còn slot PENDING để hủy");
            }
            Map<Integer, BigDecimal> removedRentalBySlotId = new HashMap<>();
            for (Integer bookingSlotId : pendingSlotIds) {
                BigDecimal rentalRemoved = cancelPendingSlot(conn, bookingId, bookingSlotId);
                if (rentalRemoved.compareTo(BigDecimal.ZERO) > 0) {
                    removedRentalBySlotId.put(bookingSlotId, rentalRemoved);
                }
            }

            StaffBookingSnapshotTokenUtil.Snapshot afterSlots =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            InvoiceUpdateResult invoice = recalcInvoice(conn, bookingId, reason, before, afterSlots, removedRentalBySlotId);
            String nextBookingStatus = recomputeBookingStatus(conn, bookingId);
            repository.updateBookingStatus(conn, bookingId, nextBookingStatus);

            StaffBookingSnapshotTokenUtil.Snapshot after =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
            String afterEtag = StaffBookingSnapshotTokenUtil.computeEtag(after);

            insertAuditLog(conn, bookingId, staffId, "CANCEL_BOOKING", "CHANGE_ALL",
                    reason, beforeEtag, afterEtag, before, after, invoice.refundDue);

            conn.commit();

            try {
                String payload = buildCancelPayload(reason);
                emailQueueService.enqueueBookingCancelled(bookingId, payload);
            } catch (Exception ignored) {
            }
            return "{\"success\":true,\"message\":\"Hủy các slot còn lại thành công\",\"data\":{"
                    + "\"etag\":" + StaffAuthUtil.escapeJson(afterEtag)
                    + ",\"bookingStatus\":\"" + nextBookingStatus + "\""
                    + ",\"totalAmount\":" + invoice.totalAmount.toPlainString()
                    + ",\"paidAmount\":" + invoice.paidAmount.toPlainString()
                    + ",\"refundDue\":" + invoice.refundDue.toPlainString()
                    + ",\"refundStatus\":\"" + invoice.refundStatus + "\""
                    + "}}";

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private StaffBookingSnapshotTokenUtil.Snapshot assertConfirmedSnapshot(
            Connection conn, int bookingId, int facilityId, String requestEtag) throws Exception {
        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String currentEtag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        if (!currentEtag.equals(requestEtag)) {
            throw ApiException.conflict(currentEtag);
        }

        if (!"CONFIRMED".equals(snapshot.bookingStatus)) {
            throw new ApiException(400, "Chỉ cho phép sửa booking ở trạng thái CONFIRMED");
        }

        return snapshot;
    }

    private StaffBookingSnapshotTokenUtil.Snapshot assertReleaseSnapshot(
            Connection conn, int bookingId, int facilityId, String requestEtag) throws Exception {
        StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, facilityId);
        if (snapshot == null) {
            throw new ApiException(404, "Không tìm thấy booking");
        }

        String currentEtag = StaffBookingSnapshotTokenUtil.computeEtag(snapshot);
        if (!currentEtag.equals(requestEtag)) {
            throw ApiException.conflict(currentEtag);
        }

        if (!"CONFIRMED".equals(snapshot.bookingStatus) && !"COMPLETED".equals(snapshot.bookingStatus)) {
            throw new ApiException(400, "Chỉ cho phép giải phóng slot khi booking ở trạng thái CONFIRMED hoặc COMPLETED");
        }

        return snapshot;
    }

    private void ensureEditableRemovals(Connection conn, int bookingId, Set<Integer> removeSlotIds) throws Exception {
        if (removeSlotIds.isEmpty()) return;

        Set<Integer> pending = new HashSet<>(repository.findPendingSlotIds(conn, bookingId));
        for (Integer slotId : removeSlotIds) {
            if (!pending.contains(slotId)) {
                throw new ApiException(400, "Chỉ được bỏ các slot PENDING");
            }
        }
    }

    private void ensureNoDuplicateAddPairs(List<SlotPair> addSlots) throws Exception {
        Set<String> keys = new HashSet<>();
        for (SlotPair slot : addSlots) {
            if (slot.courtId <= 0 || slot.slotId <= 0) {
                throw new ApiException(400, "addSlots không hợp lệ");
            }
            String key = slot.courtId + "-" + slot.slotId;
            if (!keys.add(key)) {
                throw new ApiException(400, "addSlots bị trùng courtId-slotId");
            }
        }
    }

    private void validateSessionRuleAfterEdit(Connection conn, int bookingId,
                                              Set<Integer> removeSlotIds, List<SlotPair> addSlots) throws Exception {
        List<StaffBookingEditSessionCellDTO> cells = new ArrayList<>();

        List<StaffBookingEditSessionCellDTO> existing = repository.findSessionCellsByBookingId(conn, bookingId);
        for (StaffBookingEditSessionCellDTO row : existing) {
            String status = row.getSlotStatus();
            int bookingSlotId = row.getBookingSlotId();
            if ("CANCELLED".equals(status)) continue;
            if ("PENDING".equals(status) && removeSlotIds.contains(bookingSlotId)) continue;
            cells.add(row);
        }

        for (SlotPair add : addSlots) {
            StaffBookingEditSessionCellDTO c = repository.findSessionCellBySlotId(conn, add.courtId, add.slotId);
            if (c == null) {
                throw new ApiException(400, "Slot thêm mới không hợp lệ");
            }
            cells.add(c);
        }

        Map<Integer, List<StaffBookingEditSessionCellDTO>> byCourt = new HashMap<>();
        for (StaffBookingEditSessionCellDTO c : cells) {
            byCourt.computeIfAbsent(c.getCourtId(), k -> new ArrayList<>()).add(c);
        }

        for (Map.Entry<Integer, List<StaffBookingEditSessionCellDTO>> entry : byCourt.entrySet()) {
            List<StaffBookingEditSessionCellDTO> list = entry.getValue();
            list.sort(Comparator.comparing(StaffBookingEditSessionCellDTO::getStart));
            int len = 1;
            for (int i = 1; i < list.size(); i++) {
                StaffBookingEditSessionCellDTO prev = list.get(i - 1);
                StaffBookingEditSessionCellDTO cur = list.get(i);
                if (prev.getEnd().equals(cur.getStart())) {
                    len++;
                } else {
                    if (len < 2) {
                        throw new ApiException(400, "Mỗi phiên phải có ít nhất 2 slot liên tiếp trên cùng 1 sân");
                    }
                    len = 1;
                }
            }
            if (!list.isEmpty() && len < 2) {
                throw new ApiException(400, "Mỗi phiên phải có ít nhất 2 slot liên tiếp trên cùng 1 sân");
            }
        }
    }

    private void ensureAddSlotsNotExpired(Connection conn, LocalDate bookingDate, List<SlotPair> addSlots)
            throws Exception {
        if (!LocalDate.now().equals(bookingDate) || addSlots == null || addSlots.isEmpty()) {
            return;
        }
        LocalTime now = LocalTime.now();
        for (SlotPair slot : addSlots) {
            StaffBookingEditSessionCellDTO cell = repository.findSessionCellBySlotId(conn, slot.courtId, slot.slotId);
            if (cell == null) {
                throw new ApiException(400, "Slot không tồn tại");
            }
            if (now.compareTo(cell.getEnd()) >= 0) {
                throw new ApiException(400, "Đã quá giờ kết thúc của một hoặc nhiều slot. Vui lòng chọn slot khác.");
            }
        }
    }

    private BigDecimal cancelPendingSlot(Connection conn, int bookingId, int bookingSlotId) throws Exception {
        int affected = repository.cancelPendingSlot(conn, bookingId, bookingSlotId);
        if (affected == 0) {
            throw new ApiException(400, "Không thể hủy slot không ở trạng thái PENDING");
        }
        BigDecimal rentalTotal = repository.sumRentalAmountByBookingSlotId(conn, bookingSlotId);
        repository.deleteInventoryRentalScheduleByBookingSlotId(conn, bookingSlotId);
        repository.deleteRacketRentalByBookingSlotId(conn, bookingSlotId);
        repository.deleteCourtSlotBooking(conn, bookingSlotId);
        return rentalTotal != null ? rentalTotal : BigDecimal.ZERO;
    }

    private void upsertPendingSlot(Connection conn, int bookingId, int facilityId,
                                   LocalDate bookingDate, SlotPair slot) throws Exception {
        StaffBookingEditExistingSlotDTO existing = repository.findExistingSlot(conn, bookingId, slot.courtId, slot.slotId);

        BigDecimal price = repository.lookupCurrentPrice(conn, facilityId, bookingDate, slot.courtId, slot.slotId);
        if (price == null) {
            throw new ApiException(400, "Không tìm thấy giá cho slot thêm mới");
        }

        int bookingSlotId;
        if (existing != null && existing.getBookingSlotId() != null) {
            if (!"CANCELLED".equals(existing.getSlotStatus())) {
                throw new ApiException(409, "Slot thêm mới đã tồn tại trong booking");
            }
            repository.reopenCancelledSlot(conn, existing.getBookingSlotId(), price);
            bookingSlotId = existing.getBookingSlotId();
        } else {
            bookingSlotId = repository.insertPendingSlot(conn, bookingId, slot.courtId, slot.slotId, price);
        }

        try {
            repository.insertCourtSlotBooking(conn, slot.courtId, bookingDate, slot.slotId, bookingSlotId);
        } catch (SQLException e) {
            throw new ApiException(409, "Slot thêm mới đã bị người khác đặt");
        }
    }

    private InvoiceUpdateResult recalcInvoice(Connection conn, int bookingId, String reason,
                                              StaffBookingSnapshotTokenUtil.Snapshot before,
                                              StaffBookingSnapshotTokenUtil.Snapshot afterSlots,
                                              Map<Integer, BigDecimal> removedRentalBySlotId) throws Exception {
        BigDecimal totalAmount = repository.sumActiveAmount(conn, bookingId)
                .add(repository.sumRentalAmount(conn, bookingId));
        BigDecimal paidAmount = repository.findPaidAmount(conn, bookingId);
        String previousPaymentStatus = repository.findPaymentStatus(conn, bookingId);

        BigDecimal overpaid = paidAmount.subtract(totalAmount);
        if (overpaid.compareTo(BigDecimal.ZERO) < 0) {
            overpaid = BigDecimal.ZERO;
        }

        BigDecimal refundDue = BigDecimal.ZERO;
        String refundStatus = "NONE";
        String refundNote = null;

        if (overpaid.compareTo(BigDecimal.ZERO) > 0) {
            boolean eligible = true;

            java.time.LocalDateTime createdAt = repository.findBookingCreatedAt(conn, bookingId);
            if (createdAt == null || java.time.LocalDateTime.now().isAfter(createdAt.plusHours(24))) {
                eligible = false;
            }

            if (eligible && hasIneligiblePlayStatus(before)) {
                eligible = false;
            }

            if (eligible) {
                BigDecimal eligibleDiff = computeEligibleDiffAmount(conn, before, afterSlots, removedRentalBySlotId);
                refundDue = overpaid.min(eligibleDiff);
                if (refundDue.compareTo(BigDecimal.ZERO) > 0) {
                    refundStatus = "PENDING_MANUAL";
                    refundNote = buildRefundNote("Manual refund pending", reason);
                } else {
                    refundStatus = "NONE";
                    refundNote = buildRefundNote("Refund not eligible", reason);
                }
            } else {
                refundStatus = "NONE";
                refundNote = buildRefundNote("Refund not eligible", reason);
            }
        }

        String paymentStatus;
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                paymentStatus = (previousPaymentStatus != null && !previousPaymentStatus.isEmpty())
                        ? previousPaymentStatus
                        : "UNPAID";
            } else {
                paymentStatus = "PAID";
            }
        } else if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "UNPAID";
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            paymentStatus = "PAID";
        } else {
            paymentStatus = "PARTIAL";
        }

        repository.updateInvoiceAfterRecalc(conn, bookingId, totalAmount, refundDue, refundStatus, refundNote, paymentStatus);

        InvoiceUpdateResult r = new InvoiceUpdateResult();
        r.totalAmount = totalAmount;
        r.paidAmount = paidAmount;
        r.refundDue = refundDue;
        r.refundStatus = refundStatus;
        r.paymentStatus = paymentStatus;
        return r;
    }

    private boolean hasIneligiblePlayStatus(StaffBookingSnapshotTokenUtil.Snapshot before) {
        if (before == null || before.slots == null) return false;
        for (StaffBookingSnapshotTokenUtil.SlotSnapshot slot : before.slots) {
            String status = slot.slotStatus;
            if ("CHECKED_IN".equals(status) || "CHECK_OUT".equals(status) || "NO_SHOW".equals(status)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal computeEligibleDiffAmount(Connection conn,
                                                 StaffBookingSnapshotTokenUtil.Snapshot before,
                                                 StaffBookingSnapshotTokenUtil.Snapshot afterSlots,
                                                 Map<Integer, BigDecimal> removedRentalBySlotId) throws Exception {
        if (before == null || afterSlots == null) return BigDecimal.ZERO;

        java.time.LocalDate bookingDate;
        try {
            bookingDate = java.time.LocalDate.parse(before.bookingDate);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

        Map<Integer, String> beforeStatus = new HashMap<>();
        Map<Integer, BigDecimal> beforePrice = new HashMap<>();
        for (StaffBookingSnapshotTokenUtil.SlotSnapshot slot : before.slots) {
            beforeStatus.put(slot.bookingSlotId, slot.slotStatus);
            beforePrice.put(slot.bookingSlotId, parseMoney(slot.price));
        }

        Map<Integer, StaffBookingSnapshotTokenUtil.SlotSnapshot> afterMap = new HashMap<>();
        for (StaffBookingSnapshotTokenUtil.SlotSnapshot slot : afterSlots.slots) {
            afterMap.put(slot.bookingSlotId, slot);
        }

        List<StaffBookingEditSessionCellDTO> cells = repository.findSessionCellsByBookingId(conn, before.bookingId);
        Map<Integer, java.time.LocalTime> sessionStartBySlotId = buildSessionStartBySlotId(cells, beforeStatus);

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nowPlus24 = now.plusHours(24);

        BigDecimal eligibleDiff = BigDecimal.ZERO;
        for (StaffBookingSnapshotTokenUtil.SlotSnapshot slot : before.slots) {
            String status = slot.slotStatus;
            if ("CANCELLED".equals(status)) continue;

            BigDecimal beforeAmt = beforePrice.getOrDefault(slot.bookingSlotId, BigDecimal.ZERO);
            StaffBookingSnapshotTokenUtil.SlotSnapshot after = afterMap.get(slot.bookingSlotId);
            BigDecimal afterAmt = BigDecimal.ZERO;
            if (after != null && !"CANCELLED".equals(after.slotStatus)) {
                afterAmt = parseMoney(after.price);
            }

            BigDecimal rentalDelta = BigDecimal.ZERO;
            if (removedRentalBySlotId != null && (after == null || "CANCELLED".equals(after.slotStatus))) {
                rentalDelta = removedRentalBySlotId.getOrDefault(slot.bookingSlotId, BigDecimal.ZERO);
            }

            BigDecimal delta = beforeAmt.subtract(afterAmt).add(rentalDelta);
            if (delta.compareTo(BigDecimal.ZERO) <= 0) continue;

            java.time.LocalTime sessionStart = sessionStartBySlotId.get(slot.bookingSlotId);
            if (sessionStart == null) continue;

            java.time.LocalDateTime sessionStartAt = java.time.LocalDateTime.of(bookingDate, sessionStart);
            if (!sessionStartAt.isAfter(nowPlus24)) {
                continue;
            }

            eligibleDiff = eligibleDiff.add(delta);
        }

        return eligibleDiff;
    }

    private Map<Integer, java.time.LocalTime> buildSessionStartBySlotId(List<StaffBookingEditSessionCellDTO> cells,
                                                                        Map<Integer, String> beforeStatus) {
        Map<Integer, List<StaffBookingEditSessionCellDTO>> byCourt = new HashMap<>();
        for (StaffBookingEditSessionCellDTO cell : cells) {
            String status = beforeStatus.get(cell.getBookingSlotId());
            if (status == null || "CANCELLED".equals(status)) continue;
            byCourt.computeIfAbsent(cell.getCourtId(), k -> new ArrayList<>()).add(cell);
        }

        Map<Integer, java.time.LocalTime> sessionStartBySlot = new HashMap<>();
        for (List<StaffBookingEditSessionCellDTO> list : byCourt.values()) {
            list.sort(Comparator.comparing(StaffBookingEditSessionCellDTO::getStart));
            java.time.LocalTime sessionStart = null;
            java.time.LocalTime prevEnd = null;
            for (StaffBookingEditSessionCellDTO cell : list) {
                if (sessionStart == null || (prevEnd != null && !prevEnd.equals(cell.getStart()))) {
                    sessionStart = cell.getStart();
                }
                sessionStartBySlot.put(cell.getBookingSlotId(), sessionStart);
                prevEnd = cell.getEnd();
            }
        }

        return sessionStartBySlot;
    }
    private Map<Integer, java.time.LocalTime> buildSessionEndBySlotId(List<StaffBookingEditSessionCellDTO> cells,
                                                                      Map<Integer, String> beforeStatus) {
        Map<Integer, List<StaffBookingEditSessionCellDTO>> byCourt = new HashMap<>();
        for (StaffBookingEditSessionCellDTO cell : cells) {
            String status = beforeStatus.get(cell.getBookingSlotId());
            if (status == null || "CANCELLED".equals(status)) continue;
            byCourt.computeIfAbsent(cell.getCourtId(), k -> new ArrayList<>()).add(cell);
        }

        Map<Integer, java.time.LocalTime> sessionEndBySlot = new HashMap<>();
        for (List<StaffBookingEditSessionCellDTO> list : byCourt.values()) {
            list.sort(Comparator.comparing(StaffBookingEditSessionCellDTO::getStart));
            List<StaffBookingEditSessionCellDTO> current = new ArrayList<>();
            java.time.LocalTime prevEnd = null;

            for (StaffBookingEditSessionCellDTO cell : list) {
                if (current.isEmpty()) {
                    current.add(cell);
                    prevEnd = cell.getEnd();
                    continue;
                }
                if (prevEnd != null && prevEnd.equals(cell.getStart())) {
                    current.add(cell);
                    prevEnd = cell.getEnd();
                    continue;
                }
                if (prevEnd != null) {
                    for (StaffBookingEditSessionCellDTO c : current) {
                        sessionEndBySlot.put(c.getBookingSlotId(), prevEnd);
                    }
                }
                current = new ArrayList<>();
                current.add(cell);
                prevEnd = cell.getEnd();
            }
            if (!current.isEmpty() && prevEnd != null) {
                for (StaffBookingEditSessionCellDTO c : current) {
                    sessionEndBySlot.put(c.getBookingSlotId(), prevEnd);
                }
            }
        }

        return sessionEndBySlot;
    }

    private String buildRefundNote(String prefix, String reason) {
        if (reason == null || reason.isEmpty()) return prefix;
        return prefix + ": " + reason;
    }

    private String recomputeBookingStatus(Connection conn, int bookingId) throws Exception {
        int activeCount = 0;   // slots still in progress
        int finishedCount = 0; // slots already ended by play/no-show

        for (StaffBookingEditStatusCountDTO row : repository.findSlotStatusCounts(conn, bookingId)) {
            String status = row.getSlotStatus();
            int cnt = row.getCount();

            if ("PENDING".equals(status) || "CHECKED_IN".equals(status)) {
                activeCount += cnt;
            }
            if ("CHECK_OUT".equals(status) || "NO_SHOW".equals(status)) {
                finishedCount += cnt;
            }
        }

        if (activeCount > 0) {
            return "CONFIRMED";
        }
        return finishedCount > 0 ? "COMPLETED" : "CANCELLED";
    }

    private void insertAuditLog(Connection conn, int bookingId, int staffId,
                                String changeAction, String changeType,
                                String reason, String beforeEtag, String afterEtag,
                                StaffBookingSnapshotTokenUtil.Snapshot before,
                                StaffBookingSnapshotTokenUtil.Snapshot after,
                                BigDecimal refundDue) throws Exception {
        String beforeJson = buildSnapshotJson(before);
        String afterJson = buildSnapshotJson(after);
        repository.insertAuditLog(conn, bookingId, staffId, changeAction, changeType,
                reason, beforeEtag, afterEtag, beforeJson, afterJson, refundDue);
    }

    private String buildSnapshotJson(StaffBookingSnapshotTokenUtil.Snapshot s) {
        StringBuilder json = new StringBuilder(1024);
        json.append('{');
        json.append("\"bookingStatus\":\"").append(s.bookingStatus).append("\"");

        json.append(",\"slots\":[");
        for (int i = 0; i < s.slots.size(); i++) {
            StaffBookingSnapshotTokenUtil.SlotSnapshot slot = s.slots.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                    .append("\"bookingSlotId\":").append(slot.bookingSlotId)
                    .append(",\"courtId\":").append(slot.courtId)
                    .append(",\"slotId\":").append(slot.slotId)
                    .append(",\"slotStatus\":\"").append(slot.slotStatus).append("\"")
                    .append(",\"price\":").append(slot.price)
                    .append(",\"released\":").append(slot.released)
                    .append('}');
        }
        json.append(']');

        if (s.invoice != null) {
            json.append(",\"invoice\":{")
                    .append("\"totalAmount\":").append(s.invoice.totalAmount)
                    .append(",\"paidAmount\":").append(s.invoice.paidAmount)
                    .append(",\"paymentStatus\":\"").append(s.invoice.paymentStatus).append("\"")
                    .append(",\"refundDue\":").append(s.invoice.refundDue)
                    .append(",\"refundStatus\":\"").append(s.invoice.refundStatus).append("\"")
                    .append('}');
        } else {
            json.append(",\"invoice\":null");
        }

        json.append('}');
        return json.toString();
    }

    private String buildUpdatePayload(StaffBookingSnapshotTokenUtil.Snapshot before,
                                      StaffBookingSnapshotTokenUtil.Snapshot after,
                                      String reason) {
        StringBuilder json = new StringBuilder(2048);
        json.append('{');
        json.append("\"before\":").append(buildSnapshotJson(before));
        json.append(",\"after\":").append(buildSnapshotJson(after));
        if (reason != null && !reason.isEmpty()) {
            json.append(",\"reason\":").append(StaffAuthUtil.escapeJson(reason));
        }
        json.append('}');
        return json.toString();
    }

    private String buildCancelPayload(String reason) {
        if (reason == null || reason.isEmpty()) {
            return "{}";
        }
        return "{\"reason\":" + StaffAuthUtil.escapeJson(reason) + "}";
    }

    private List<SlotPair> parseSlotPairs(String json, String key) {
        String arr = extractArray(json, key);
        List<SlotPair> out = new ArrayList<>();
        if (arr == null || arr.length() < 2) return out;

        int pos = 0;
        while (pos < arr.length()) {
            int objStart = arr.indexOf('{', pos);
            if (objStart < 0) break;
            int objEnd = arr.indexOf('}', objStart);
            if (objEnd < 0) break;
            String obj = arr.substring(objStart, objEnd + 1);

            SlotPair p = new SlotPair();
            p.courtId = parseInt(StaffBookingSnapshotTokenUtil.extractString(obj, "courtId"));
            p.slotId = parseInt(StaffBookingSnapshotTokenUtil.extractString(obj, "slotId"));
            if (p.courtId > 0 && p.slotId > 0) {
                out.add(p);
            }
            pos = objEnd + 1;
        }

        return out;
    }

    private Set<Integer> parseIntSet(String json, String key) {
        String arr = extractArray(json, key);
        Set<Integer> out = new LinkedHashSet<>();
        if (arr == null || arr.length() < 2) return out;

        String body = arr.substring(1, arr.length() - 1).trim();
        if (body.isEmpty()) return out;

        String[] parts = body.split(",");
        for (String part : parts) {
            int val = parseInt(part.trim());
            if (val > 0) out.add(val);
        }
        return out;
    }

    private String extractArray(String json, String key) {
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

    private int parseInt(String value) {
        if (value == null) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private StaffBookingEditOutcomeDTO out(int status, String json) {
        StaffBookingEditOutcomeDTO result = new StaffBookingEditOutcomeDTO();
        result.setStatus(status);
        result.setJson(json);
        return result;
    }

    private static class SlotPair {
        int courtId;
        int slotId;
    }

    private static class InvoiceUpdateResult {
        BigDecimal totalAmount;
        BigDecimal paidAmount;
        BigDecimal refundDue;
        String refundStatus;        String paymentStatus;
    }

    private static class ApiException extends Exception {
        final int status;
        final String message;
        final String currentEtag;

        ApiException(int status, String message) {
            this(status, message, null);
        }

        ApiException(int status, String message, String currentEtag) {
            super(message);
            this.status = status;
            this.message = message;
            this.currentEtag = currentEtag;
        }

        static ApiException conflict(String currentEtag) {
            return new ApiException(409, "Booking data changed, please reload.", currentEtag);
        }

        String toJson() {
            StringBuilder json = new StringBuilder(160);
            json.append("{\"success\":false,\"message\":")
                    .append(StaffAuthUtil.escapeJson(message));
            if (currentEtag != null) {
                json.append(",\"data\":{\"currentEtag\":")
                        .append(StaffAuthUtil.escapeJson(currentEtag))
                        .append("}");
            }
            json.append("}");
            return json.toString();
        }
    }
}


















