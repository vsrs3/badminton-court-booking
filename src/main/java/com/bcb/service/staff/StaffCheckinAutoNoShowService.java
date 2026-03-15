package com.bcb.service.staff;

/**
 * Service interface for automatically marking sessions as NO_SHOW after end time.
 */
public interface StaffCheckinAutoNoShowService {

    /**
     * Auto marks PENDING sessions as NO_SHOW when their end time has passed.
     * Applies only to CONFIRMED bookings.
     */
    void autoNoShowExpiredSessions();
}
