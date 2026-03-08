package com.bcb.repository.booking;

import com.bcb.model.CourtScheduleException;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for CourtScheduleException lookup in single-booking context.
 *
 * @author AnhTN
 */
public interface CourtScheduleExceptionRepository {

    /**
     * Finds active exceptions for courts of a facility on a given date.
     */
    List<CourtScheduleException> findActiveByFacilityAndDate(int facilityId, LocalDate date);
}

