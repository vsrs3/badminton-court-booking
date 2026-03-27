package com.bcb.config.email;

import com.bcb.service.email.EmailQueueService;
import com.bcb.service.email.impl.EmailQueueServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class EmailQueueWorkerListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(EmailQueueWorkerListener.class.getName());
    private ScheduledExecutorService scheduler;

    /**
     * Starts the email queue worker to process pending emails periodically.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Starting email queue worker (every 60 seconds)...");
        EmailQueueService emailQueueService = new EmailQueueServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "email-queue-worker");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                emailQueueService.processPendingEmails();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in email queue worker", e);
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down email queue worker...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
