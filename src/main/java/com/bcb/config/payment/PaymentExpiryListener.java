package com.bcb.config.payment;

import com.bcb.service.payment.PaymentService;
import com.bcb.service.payment.impl.PaymentServiceImpl;
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
 * PENDING payments whose expire_at has passed, and release associated bookings.
 * Reusable: handles all payment types, not just single-booking.
 *
 * @author AnhTN
 */
@WebListener
public class PaymentExpiryListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(PaymentExpiryListener.class.getName());
    private ScheduledExecutorService scheduler;

    /** Starts the payment expiry cron on application startup. */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Starting payment expiry cron (every 1 minute)...");
        PaymentService paymentService = new PaymentServiceImpl();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "payment-expiry-cron");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                paymentService.expireOverduePayments();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in payment expiry cron", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /** Shuts down the payment expiry cron on application shutdown. */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down payment expiry cron...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
