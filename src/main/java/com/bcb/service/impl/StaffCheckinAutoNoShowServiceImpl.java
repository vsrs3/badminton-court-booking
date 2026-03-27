package com.bcb.service.impl;

import com.bcb.dto.staff.StaffCheckinSessionDTO;
import com.bcb.repository.impl.StaffCheckinRepositoryImpl;
import com.bcb.repository.staff.StaffCheckinRepository;
import com.bcb.service.staff.StaffCheckinAutoNoShowService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatically marks PENDING sessions as NO_SHOW after end time.
 */
public class StaffCheckinAutoNoShowServiceImpl implements StaffCheckinAutoNoShowService {

    private static final Logger LOG = Logger.getLogger(StaffCheckinAutoNoShowServiceImpl.class.getName());
    private final StaffCheckinRepository repository = new StaffCheckinRepositoryImpl();

    /**
     * Scans for overdue PENDING sessions and marks them as NO_SHOW.
     */
    @Override
    public void autoNoShowExpiredSessions() {
        List<Integer> bookingIds = new ArrayList<>();
        try (Connection conn = DBContext.getConnection()) {
            // Only target CONFIRMED bookings with pending slots for today.
            bookingIds = repository.findConfirmedBookingIdsWithPendingSlots(conn, LocalDate.now());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load bookings for auto no-show", e);
            return;
        }

        if (bookingIds.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (int bookingId : bookingIds) {
            Connection conn = null;
            try {
                conn = DBContext.getConnection();
                conn.setAutoCommit(false);

                /* Auto no-show transaction flow per booking. */
                List<StaffCheckinSessionDTO> sessions =
                        StaffCheckinSessionBuilder.buildSessionsWithTime(repository, conn, bookingId);
                if (sessions.isEmpty()) {
                    conn.commit();
                    continue;
                }

                int autoNoShowCount = 0;
                for (StaffCheckinSessionDTO session : sessions) {
                    String status = getSessionStatus(conn, session.getSlotIds());
                    LocalDate sessionDate = session.getSessionDate();
                    if (sessionDate == null) {
                        continue;
                    }
                    boolean expired = sessionDate.isBefore(today)
                            || (sessionDate.isEqual(today) && now.isAfter(session.getEndTime()));
                    if ("PENDING".equals(status) && expired) {
                        repository.updateSlotsNoShow(conn, session.getSlotIds());
                        autoNoShowCount++;
                    }
                }

                if (autoNoShowCount > 0) {
                    // Mark booking COMPLETED when all sessions finished.
                    boolean allFinished = checkAllSessionsFinished(conn, sessions);
                    if (allFinished) {
                        repository.updateBookingStatus(conn, bookingId, "COMPLETED");
                    }
                    conn.commit();
                    LOG.info("Auto no-show updated booking_id=" + bookingId + ", sessions=" + autoNoShowCount);
                } else {
                    conn.commit();
                }
            } catch (Exception e) {
                rollbackQuietly(conn);
                LOG.log(Level.SEVERE, "Failed to auto no-show booking_id=" + bookingId, e);
            } finally {
                closeQuietly(conn);
            }
        }
    }

    private boolean checkAllSessionsFinished(Connection conn, List<StaffCheckinSessionDTO> sessions) throws Exception {
        for (StaffCheckinSessionDTO session : sessions) {
            String status = getSessionStatus(conn, session.getSlotIds());
            if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status) && !"CANCELLED".equals(status)) {
                return false;
            }
        }
        return true;
    }

    private String getSessionStatus(Connection conn, List<Integer> slotIds) throws Exception {
        if (slotIds.isEmpty()) return "PENDING";

        List<String> statuses = repository.findSlotStatuses(conn, slotIds);

        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean allCancelled = true;
        boolean anyCheckedIn = false;

        for (String s : statuses) {
            if (!"CHECK_OUT".equals(s)) allCheckout = false;
            if (!"NO_SHOW".equals(s)) allNoShow = false;
            if (!"CANCELLED".equals(s)) allCancelled = false;
            if ("CHECKED_IN".equals(s)) anyCheckedIn = true;
        }

        if (allCancelled) return "CANCELLED";
        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // ignored
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // ignored
            }
        }
    }
}
