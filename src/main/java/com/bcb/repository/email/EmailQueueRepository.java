package com.bcb.repository.email;

import com.bcb.dto.email.EmailQueueItemDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailQueueRepository {
    void enqueue(String emailType, int bookingId, String toEmail, String payloadJson) throws Exception;

    List<EmailQueueItemDTO> findAndMarkPending(int limit) throws Exception;

    void markSent(int emailId) throws Exception;

    void markFailed(int emailId, int retryCount, LocalDateTime nextAttemptAt, String lastError, String status)
            throws Exception;
}
