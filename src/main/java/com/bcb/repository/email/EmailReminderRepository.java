package com.bcb.repository.email;

import com.bcb.dto.email.EmailReminderCandidateDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailReminderRepository {
    List<EmailReminderCandidateDTO> findUpcomingCandidates(LocalDateTime from, LocalDateTime to) throws Exception;

    List<EmailReminderCandidateDTO> findPaymentCandidates(LocalDateTime from, LocalDateTime to) throws Exception;
}
