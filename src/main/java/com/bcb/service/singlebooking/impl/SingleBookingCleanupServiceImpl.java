package com.bcb.service.singlebooking.impl;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Booking;
import com.bcb.repository.booking.BookingRepository;
import com.bcb.repository.booking.BookingSlotRepository;
import com.bcb.repository.booking.CourtSlotBookingRepository;
import com.bcb.repository.booking.impl.BookingRepositoryImpl;
import com.bcb.repository.booking.impl.BookingSlotRepositoryImpl;
import com.bcb.repository.booking.impl.CourtSlotBookingRepositoryImpl;
import com.bcb.service.singlebooking.SingleBookingCleanupService;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cleans up expired PENDING bookings by setting status to EXPIRED
 * and releasing CourtSlotBooking locks.
 *
 * @author AnhTN
 */
public class SingleBookingCleanupServiceImpl implements SingleBookingCleanupService {

    private static final Logger LOG = Logger.getLogger(SingleBookingCleanupServiceImpl.class.getName());

    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;

    public SingleBookingCleanupServiceImpl() {
        this.bookingRepo = new BookingRepositoryImpl();
        this.bookingSlotRepo = new BookingSlotRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public void cleanupExpiredHolds() {
        List<Booking> expired = bookingRepo.findExpiredPendingBookings();
        if (expired.isEmpty()) {
            return;
        }
        LOG.info("Found " + expired.size() + " expired PENDING booking(s) to clean up.");

        for (Booking booking : expired) {
            Connection conn = null;
            try {
                conn = DBContext.getConnection();
                conn.setAutoCommit(false);

                // 1. Find booking_slot_ids
                List<Integer> bookingSlotIds =
                        bookingSlotRepo.findBookingSlotIdsByBookingId(conn, booking.getBookingId());

                // 2. Delete CourtSlotBooking locks
                courtSlotBookingRepo.deleteByBookingSlotIds(conn, bookingSlotIds);

                // 3. Set booking status to EXPIRED
                bookingRepo.updateStatus(conn, booking.getBookingId(), "EXPIRED");

                conn.commit();
                LOG.info("Expired booking_id=" + booking.getBookingId() + " cleaned up successfully.");
            } catch (SQLException | DataAccessException e) {
                rollbackQuietly(conn);
                LOG.log(Level.SEVERE, "Failed to cleanup booking_id=" + booking.getBookingId(), e);
            } finally {
                closeQuietly(conn);
            }
        }
    }

    /** Quietly rolls back a connection. */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) { /* ignored */ }
        }
    }

    /** Quietly closes a connection. */
    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { /* ignored */ }
        }
    }
}
