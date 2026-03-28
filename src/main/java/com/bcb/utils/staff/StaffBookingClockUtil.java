package com.bcb.utils.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Clock used by staff booking flows.
 *
 * Default behavior keeps the "current" time at the same morning every day so
 * staff can still create same-day bookings during test/demo sessions.
 *
 * Override formats:
 * - SYSTEM
 * - HH:mm
 * - yyyy-MM-dd
 * - yyyy-MM-ddTHH:mm[:ss]
 */
public final class StaffBookingClockUtil {

    private static final String SYS_PROP = "bcb.staff.booking.now";
    private static final String ENV_KEY = "BCB_STAFF_BOOKING_NOW";
    private static final LocalTime DEFAULT_MORNING_TIME = LocalTime.of(5, 0);

    private StaffBookingClockUtil() {
    }

    public static LocalDateTime now() {
        String override = readOverride();
        if (override == null) {
            return LocalDate.now().atTime(DEFAULT_MORNING_TIME);
        }

        LocalDateTime parsed = parseOverride(override.trim());
        if (parsed != null) {
            return parsed;
        }

        return LocalDate.now().atTime(DEFAULT_MORNING_TIME);
    }

    public static LocalDate today() {
        return now().toLocalDate();
    }

    public static LocalTime currentTime() {
        return now().toLocalTime();
    }

    public static String nowIso() {
        return now().toString();
    }

    private static String readOverride() {
        String systemValue = System.getProperty(SYS_PROP);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(ENV_KEY);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return null;
    }

    private static LocalDateTime parseOverride(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        if ("SYSTEM".equalsIgnoreCase(raw)) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(raw).atTime(DEFAULT_MORNING_TIME);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.now().atTime(LocalTime.parse(raw));
        } catch (Exception ignored) {
        }

        return null;
    }
}
