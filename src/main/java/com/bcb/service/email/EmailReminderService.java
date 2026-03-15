package com.bcb.service.email;

public interface EmailReminderService {
    void runUpcomingReminders();

    void runPaymentReminders();
}
