package com.bcb.repository.recurring;

import com.bcb.model.RecurringBooking;

import java.sql.Connection;

/**
 * Repository for RecurringBooking writes.
 *
 * @author AnhTN
 */
public interface RecurringBookingRepository {

    int insert(Connection conn, RecurringBooking recurringBooking);
}

