package com.bcb.config.checkin;

import com.bcb.service.impl.StaffCheckinServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class CheckinAutoCheckoutSchedulerListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(CheckinAutoCheckoutSchedulerListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Run every 15 minutes to check out overdue CHECKED_IN sessions.
        LOG.info("Starting auto-checkout scheduler (every 15 minutes)...");
        StaffCheckinServiceImpl service = new StaffCheckinServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "checkin-auto-checkout-scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                service.runAutoCheckoutOverdueSessions();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in auto-checkout scheduler", e);
            }
        }, 15, 15, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down auto-checkout scheduler...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
