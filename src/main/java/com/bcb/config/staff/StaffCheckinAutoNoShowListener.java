package com.bcb.config.staff;

import com.bcb.service.impl.StaffCheckinAutoNoShowServiceImpl;
import com.bcb.service.staff.StaffCheckinAutoNoShowService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ServletContextListener that starts a cron job (every 1 minute) to
 * auto mark PENDING sessions as NO_SHOW after end time.
 */
@WebListener
public class StaffCheckinAutoNoShowListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(StaffCheckinAutoNoShowListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Run every minute to mark overdue PENDING sessions as NO_SHOW.
        LOG.info("Starting staff auto no-show cron (every 1 minute)...");
        StaffCheckinAutoNoShowService service = new StaffCheckinAutoNoShowServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "staff-auto-noshow-cron");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                service.autoNoShowExpiredSessions();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in staff auto no-show cron", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down staff auto no-show cron...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
