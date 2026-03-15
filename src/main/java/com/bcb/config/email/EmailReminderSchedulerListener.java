package com.bcb.config.email;

import com.bcb.service.email.EmailReminderService;
import com.bcb.service.email.impl.EmailReminderServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class EmailReminderSchedulerListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(EmailReminderSchedulerListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Starting email reminder scheduler (every 15 minutes)...");
        EmailReminderService reminderService = new EmailReminderServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "email-reminder-scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                reminderService.runUpcomingReminders();
                reminderService.runPaymentReminders();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in email reminder scheduler", e);
            }
        }, 15, 15, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down email reminder scheduler...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
