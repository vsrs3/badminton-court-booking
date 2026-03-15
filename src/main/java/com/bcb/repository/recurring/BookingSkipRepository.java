package com.bcb.repository.recurring;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * Repository for BookingSkip writes.
 *
 * @author AnhTN
 */
public interface BookingSkipRepository {

    void insert(Connection conn, int recurringId, LocalDate skipDate, String reason);
}

