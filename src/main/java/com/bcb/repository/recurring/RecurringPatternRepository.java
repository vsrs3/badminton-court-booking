package com.bcb.repository.recurring;

import java.sql.Connection;

/**
 * Repository for RecurringPattern writes.
 *
 * @author AnhTN
 */
public interface RecurringPatternRepository {

    void insert(Connection conn, int recurringId, int courtId, int dayOfWeek, int slotId);
}

