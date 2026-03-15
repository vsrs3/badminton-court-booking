package com.bcb.repository.staff;

import com.bcb.model.CourtScheduleException;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;

public interface StaffScheduleExceptionRepository {
    LocalTime[] findSlotTime(Connection conn, int slotId) throws Exception;

    LocalTime[] findTimeSlotBounds(Connection conn) throws Exception;

    boolean hasBooking(Connection conn, int facilityId, int courtId, LocalDate date, int slotId) throws Exception;

    boolean hasActiveExceptionOverlap(Connection conn, int facilityId, int courtId, LocalDate date,
                                      LocalTime start, LocalTime end) throws Exception;

    CourtScheduleException findActiveExceptionById(Connection conn, int facilityId, int exceptionId) throws Exception;

    int insertException(Connection conn, CourtScheduleException ex) throws Exception;

    void deactivateException(Connection conn, int exceptionId) throws Exception;
}
