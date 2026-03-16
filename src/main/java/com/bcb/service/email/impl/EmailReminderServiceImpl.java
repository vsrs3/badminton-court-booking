package com.bcb.service.email.impl;

import com.bcb.dto.email.EmailReminderCandidateDTO;
import com.bcb.repository.email.EmailQueueRepository;
import com.bcb.repository.email.EmailReminderRepository;
import com.bcb.repository.email.impl.EmailQueueRepositoryImpl;
import com.bcb.repository.email.impl.EmailReminderRepositoryImpl;
import com.bcb.service.email.EmailReminderService;

import java.time.LocalDateTime;
import java.util.List;

public class EmailReminderServiceImpl implements EmailReminderService {

    private static final int WINDOW_MINUTES = 15;

    private final EmailReminderRepository reminderRepository = new EmailReminderRepositoryImpl();
    private final EmailQueueRepository emailQueueRepository = new EmailQueueRepositoryImpl();

    @Override
    public void runUpcomingReminders() {
        runUpcomingReminderType(24, "REMINDER_UPCOMING_24H");
        runUpcomingReminderType(2, "REMINDER_UPCOMING_2H");
        runCustomerUpcomingReminderType(24, "REMINDER_CUS_24H");
    }

    @Override
    public void runPaymentReminders() {
        runPaymentReminderType(12, "REMINDER_PAYMENT_12H");
    }

    private void runUpcomingReminderType(int leadHours, String emailType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(leadHours);
        LocalDateTime from = target.minusMinutes(WINDOW_MINUTES);
        LocalDateTime to = target.plusMinutes(WINDOW_MINUTES);

        try {
            List<EmailReminderCandidateDTO> candidates = reminderRepository.findUpcomingCandidates(from, to);
            enqueueCandidates(candidates, emailType);
        } catch (Exception ignored) {
        }
    }

    private void runPaymentReminderType(int leadHours, String emailType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(leadHours);
        LocalDateTime from = target.minusMinutes(WINDOW_MINUTES);
        LocalDateTime to = target.plusMinutes(WINDOW_MINUTES);

        try {
            List<EmailReminderCandidateDTO> candidates = reminderRepository.findPaymentCandidates(from, to);
            enqueueCandidates(candidates, emailType);
        } catch (Exception ignored) {
        }
    }

    private void runCustomerUpcomingReminderType(int leadHours, String emailType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(leadHours);
        LocalDateTime from = target.minusMinutes(WINDOW_MINUTES);
        LocalDateTime to = target.plusMinutes(WINDOW_MINUTES);

        try {
            List<EmailReminderCandidateDTO> candidates = reminderRepository.findUpcomingCustomerCandidates(from, to);
            enqueueCandidates(candidates, emailType);
        } catch (Exception ignored) {
        }
    }

    private void enqueueCandidates(List<EmailReminderCandidateDTO> candidates, String emailType) throws Exception {
        if (candidates == null || candidates.isEmpty()) return;
        for (EmailReminderCandidateDTO c : candidates) {
            if (c.getStartAt() == null) continue;
            boolean exists = emailQueueRepository.existsReminder(c.getBookingId(), emailType, c.getStartAt());
            if (exists) continue;
            emailQueueRepository.enqueue(emailType, c.getBookingId(), c.getToEmail(), null, c.getStartAt());
        }
    }
}
