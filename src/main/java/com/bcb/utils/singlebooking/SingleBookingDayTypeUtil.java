package com.bcb.utils.singlebooking;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Utility to determine day_type (WEEKDAY/WEEKEND) from a date.
 *
 * @author AnhTN
 */
public final class SingleBookingDayTypeUtil {

    private SingleBookingDayTypeUtil() {}

    /**
     * Returns "WEEKEND" if the date is Saturday or Sunday, otherwise "WEEKDAY".
     */
    public static String resolve(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return "WEEKEND";
        }
        return "WEEKDAY";
    }
}

