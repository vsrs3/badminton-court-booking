package com.bcb.service.email;

public interface EmailQueueService {

    EmailEnqueueResult enqueueBookingCreated(int bookingId);

    void enqueueBookingUpdated(int bookingId, String payloadJson);

    void enqueueBookingCancelled(int bookingId, String payloadJson);

    void processPendingEmails();

    class EmailEnqueueResult {
        public final boolean queued;
        public final String warning;

        public EmailEnqueueResult(boolean queued, String warning) {
            this.queued = queued;
            this.warning = warning;
        }
    }
}
