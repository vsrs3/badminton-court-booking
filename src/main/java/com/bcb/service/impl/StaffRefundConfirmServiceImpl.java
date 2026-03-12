package com.bcb.service.impl;

import com.bcb.dto.staff.StaffRefundConfirmResultDTO;
import com.bcb.repository.impl.StaffRefundRepositoryImpl;
import com.bcb.repository.staff.StaffRefundRepository;
import com.bcb.service.staff.StaffRefundConfirmService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StaffRefundConfirmServiceImpl implements StaffRefundConfirmService {

    private static final DateTimeFormatter NOTE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final StaffRefundRepository repository = new StaffRefundRepositoryImpl();

    @Override
    public StaffRefundConfirmResultDTO confirmRefund(int bookingId, int facilityId, int staffId, String note)
            throws Exception {
        StaffRefundConfirmResultDTO result = new StaffRefundConfirmResultDTO();

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String existingNote = repository.findRefundNote(conn, bookingId, facilityId);
                if (existingNote == null) {
                    result.setSuccess(false);
                    result.setMessage("Không tìm thấy yêu cầu hoàn tiền hoặc yêu cầu không ở trạng thái chờ xử lý");
                    return result;
                }

                String confirmNote = buildConfirmNote(staffId, note);
                String mergedNote = mergeNotes(existingNote, confirmNote);

                int updated = repository.markRefunded(conn, bookingId, facilityId, mergedNote);
                if (updated == 0) {
                    conn.rollback();
                    result.setSuccess(false);
                    result.setMessage("Không thể xác nhận hoàn tiền");
                    return result;
                }

                conn.commit();
                result.setSuccess(true);
                result.setMessage("Đã xác nhận hoàn tiền");
                result.setRefundStatus("REFUNDED");
                result.setRefundNote(mergedNote);
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String buildConfirmNote(int staffId, String note) {
        String base = "Đã hoàn tiền bởi nhân viên " + staffId + " lúc " + NOTE_TIME_FORMAT.format(LocalDateTime.now());
        if (note == null || note.trim().isEmpty()) return base;
        return base + " - " + note.trim();
    }

    private String mergeNotes(String existing, String append) {
        if (existing == null || existing.trim().isEmpty()) return append;
        return existing.trim() + " | " + append;
    }
}