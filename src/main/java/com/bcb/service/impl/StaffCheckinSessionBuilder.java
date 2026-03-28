package com.bcb.service.impl;

import com.bcb.dto.staff.StaffCheckinSessionDTO;
import com.bcb.dto.staff.StaffCheckinSessionSlotRowDTO;
import com.bcb.repository.staff.StaffCheckinRepository;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

final class StaffCheckinSessionBuilder {

    private StaffCheckinSessionBuilder() {
    }

    static List<StaffCheckinSessionDTO> buildSessionsWithTime(StaffCheckinRepository repository,
                                                             Connection conn,
                                                             int bookingId) throws Exception {
        List<StaffCheckinSessionSlotRowDTO> rows = repository.findSessionSlotRows(conn, bookingId);

        if (rows.isEmpty()) return new ArrayList<>();

        List<StaffCheckinSessionDTO> sessions = new ArrayList<>();

        StaffCheckinSessionSlotRowDTO first = rows.get(0);
        StaffCheckinSessionDTO current = new StaffCheckinSessionDTO();
        current.getSlotIds().add(first.getBookingSlotId());
        current.setSessionDate(first.getSessionDate());
        current.setStartTime(first.getStartTime());
        current.setEndTime(first.getEndTime());

        for (int i = 1; i < rows.size(); i++) {
            StaffCheckinSessionSlotRowDTO prev = rows.get(i - 1);
            StaffCheckinSessionSlotRowDTO curr = rows.get(i);

            boolean sameDate = (prev.getSessionDate() != null && prev.getSessionDate().equals(curr.getSessionDate()));
            boolean sameCourt = prev.getCourtId() == curr.getCourtId();
            boolean contiguous = prev.getEndTime().equals(curr.getStartTime());
            boolean splitByStatus = shouldSplitByStatus(prev.getSlotStatus(), curr.getSlotStatus());

            if (sameDate && sameCourt && contiguous && !splitByStatus) {
                current.getSlotIds().add(curr.getBookingSlotId());
                current.setEndTime(curr.getEndTime());
            } else {
                sessions.add(current);
                current = new StaffCheckinSessionDTO();
                current.getSlotIds().add(curr.getBookingSlotId());
                current.setSessionDate(curr.getSessionDate());
                current.setStartTime(curr.getStartTime());
                current.setEndTime(curr.getEndTime());
            }
        }
        sessions.add(current);

        sessions.sort((a, b) -> {
            if (a.getSessionDate() == null && b.getSessionDate() == null) {
                return a.getStartTime().compareTo(b.getStartTime());
            }
            if (a.getSessionDate() == null) return 1;
            if (b.getSessionDate() == null) return -1;
            int cmp = a.getSessionDate().compareTo(b.getSessionDate());
            if (cmp != 0) return cmp;
            return a.getStartTime().compareTo(b.getStartTime());
        });
        return sessions;
    }

    private static boolean shouldSplitByStatus(String prevStatus, String currStatus) {
        boolean prevFinished = isFinished(prevStatus);
        boolean currFinished = isFinished(currStatus);
        boolean prevPending = isPending(prevStatus);
        boolean currPending = isPending(currStatus);

        return (prevFinished && currPending) || (prevPending && currFinished);
    }

    private static boolean isFinished(String status) {
        return "CHECK_OUT".equals(status) || "NO_SHOW".equals(status);
    }

    private static boolean isPending(String status) {
        return "PENDING".equals(status);
    }
}
