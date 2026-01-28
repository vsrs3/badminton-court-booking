package com.bcb.validation;

import com.bcb.model.CourtPrice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation logic for CourtPrice entity.
 * Handles validation rules for court pricing.
 */
public class CourtPriceValidator {

    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999.99");

    /**
     * Validates court price for creation/update
     * @param courtPrice CourtPrice to validate
     * @return List of validation errors (empty if valid)
     */
    public static List<String> validate(CourtPrice courtPrice) {
        List<String> errors = new ArrayList<>();

        if (courtPrice == null) {
            errors.add("Court price cannot be null");
            return errors;
        }

        // Validate court ID (required, must be > 0)
        if (courtPrice.getCourtId() <= 0) {
            errors.add("Court ID is required and must be positive");
        }

        // Validate start time (required)
        if (courtPrice.getStartTime() == null) {
            errors.add("Start time is required");
        }

        // Validate end time (required)
        if (courtPrice.getEndTime() == null) {
            errors.add("End time is required");
        }

        // Validate time range (end time must be after start time)
        if (courtPrice.getStartTime() != null && courtPrice.getEndTime() != null) {
            if (courtPrice.getEndTime().isBefore(courtPrice.getStartTime()) ||
                    courtPrice.getEndTime().equals(courtPrice.getStartTime())) {
                errors.add("End time must be after start time");
            }
        }

        // Validate price (required, positive, within range)
        if (courtPrice.getPricePerHour() == null) {
            errors.add("Price per hour is required");
        } else if (courtPrice.getPricePerHour().compareTo(MIN_PRICE) <= 0) {
            errors.add("Price per hour must be greater than zero");
        } else if (courtPrice.getPricePerHour().compareTo(MAX_PRICE) > 0) {
            errors.add("Price per hour cannot exceed " + MAX_PRICE);
        }

        return errors;
    }

    /**
     * Quick check if court price is valid.
     *
     * @param courtPrice CourtPrice to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(CourtPrice courtPrice) {
        return validate(courtPrice).isEmpty();
    }
}
