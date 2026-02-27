package com.bcb.config.singlebooking;

import com.bcb.service.singlebooking.SingleBookingCleanupService;
import com.bcb.service.singlebooking.impl.SingleBookingCleanupServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ServletContextListener that starts a cron job (every 1 minute) to expire
 * PENDING bookings whose hold has expired, releasing CourtSlotBooking locks.
 *
 * @author AnhTN
 */
@WebListener
public class SingleBookingHoldCleanupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(SingleBookingHoldCleanupListener.class.getName());
    private ScheduledExecutorService scheduler;

    /** Starts the cleanup cron on application startup. */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Starting SingleBooking hold cleanup cron (every 1 minute)...");
        SingleBookingCleanupService cleanupService = new SingleBookingCleanupServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sb-hold-cleanup");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupService.cleanupExpiredHolds();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in hold cleanup cron", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /** Shuts down the cleanup cron on application shutdown. */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down SingleBooking hold cleanup cron...");
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
