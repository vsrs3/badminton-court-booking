package com.bcb.validation;

import com.bcb.model.Court;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation logic for Court entity.
 * Handles validation rules for court operations.
 */
public class CourtValidator {

    private static final int MAX_COURT_NAME_LENGTH = 100;

    /**
     * Validates court for creation/update
     * @param court Court to validate
     * @return List of validation errors (empty if valid)
     */
    public static List<String> validate(Court court) {
        List<String> errors = new ArrayList<>();

        if (court == null) {
            errors.add("Court cannot be null");
            return errors;
        }

        // Validate facility ID (required, must be > 0)
        if (court.getFacilityId() <= 0) {
            errors.add("Facility ID is required and must be positive");
        }

        // Validate court name (required, length)
        if (court.getCourtName() == null || court.getCourtName().trim().isEmpty()) {
            errors.add("Court name is required");
        } else if (court.getCourtName().length() > MAX_COURT_NAME_LENGTH) {
            errors.add("Court name cannot exceed " + MAX_COURT_NAME_LENGTH + " characters");
        }

        // Validate court type ID
        if (court.getCourtTypeId() <= 0) {
            errors.add("Court type ID is required and must be positive");
        }

        return errors;
    }

    /**
     * Quick check if court is valid.
     *
     * @param court Court to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(Court court) {
        return validate(court).isEmpty();
    }
}
