package com.bcb.validation;

import com.bcb.model.Facility;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation logic for Facility entity.
 * Handles validation rules for facility operations.
 */
public class FacilityValidator {

    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final int MAX_PROVINCE_LENGTH = 100;
    private static final int MAX_DISTRICT_LENGTH = 100;
    private static final int MAX_WARD_LENGTH = 100;

    /**
     * Validates facility for creation/update.
     *
     * @param facility Facility to validate
     * @return List of validation errors (empty if valid)
     */
    public static List<String> validate(Facility facility) {
        List<String> errors = new ArrayList<>();

        if (facility == null) {
            errors.add("Facility cannot be null");
            return errors;
        }

        // Validate name (required, length)
        if (facility.getName() == null || facility.getName().trim().isEmpty()) {
            errors.add("Facility name is required");
        } else if (facility.getName().length() > MAX_NAME_LENGTH) {
            errors.add("Facility name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }

        // Validate address (required, length)
        if (facility.getAddress() == null || facility.getAddress().trim().isEmpty()) {
            errors.add("Address is required");
        } else if (facility.getAddress().length() > MAX_ADDRESS_LENGTH) {
            errors.add("Address cannot exceed " + MAX_ADDRESS_LENGTH + " characters");
        }

        // Validate province (optional, length)
        if (facility.getProvince() != null && facility.getProvince().length() > MAX_PROVINCE_LENGTH) {
            errors.add("Province cannot exceed " + MAX_PROVINCE_LENGTH + " characters");
        }

        // Validate district (optional, length)
        if (facility.getDistrict() != null && facility.getDistrict().length() > MAX_DISTRICT_LENGTH) {
            errors.add("District cannot exceed " + MAX_DISTRICT_LENGTH + " characters");
        }

        // Validate ward (optional, length)
        if (facility.getWard() != null && facility.getWard().length() > MAX_WARD_LENGTH) {
            errors.add("Ward cannot exceed " + MAX_WARD_LENGTH + " characters");
        }

        // Validate open time (required)
        if (facility.getOpenTime() == null) {
            errors.add("Open time is required");
        }

        // Validate close time (required)
        if (facility.getCloseTime() == null) {
            errors.add("Close time is required");
        }

        // Validate time range (close time must be after open time)
        if (facility.getOpenTime() != null && facility.getCloseTime() != null) {
            if (facility.getCloseTime().isBefore(facility.getOpenTime()) ||
                    facility.getCloseTime().equals(facility.getOpenTime())) {
                errors.add("Close time must be after open time");
            }
        }

        return errors;
    }

    /**
     * Quick check if facility is valid.
     *
     * @param facility Facility to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(Facility facility) {
        return validate(facility).isEmpty();
    }
}
