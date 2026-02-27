package com.bcb.validation.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.dto.singlebooking.SingleBookingSelectionItemDTO;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.utils.singlebooking.SingleBookingTimeRangeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates single-booking selections: date, past-slot, min-60m rule.
 *
 * @author AnhTN
 */
public final class SingleBookingSelectionValidator {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private SingleBookingSelectionValidator() {}

    /**
     * Validates date string format (YYYY-MM-DD) and returns parsed LocalDate.
     */
    public static LocalDate parseAndValidateDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new SingleBookingValidationException("VALIDATION_ERROR",
                    "bookingDate is required.",
                    List.of(Map.of("field", "bookingDate", "issue", "required")));
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new SingleBookingValidationException("VALIDATION_ERROR",
                    "bookingDate format invalid. Expected YYYY-MM-DD.",
                    List.of(Map.of("field", "bookingDate", "issue", "invalid_format", "rejectedValue", dateStr)));
        }
    }

    /**
     * Validates that bookingDate is not in the past.
     */
    public static void validateNotPastDate(LocalDate bookingDate) {
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new SingleBookingValidationException("PAST_DATE",
                    "Không thể đặt lịch cho ngày quá khứ.",
                    List.of(Map.of("field", "bookingDate", "issue", "must_be_today_or_future",
                            "rejectedValue", bookingDate.toString())));
        }
    }

    /**
     * Validates that today's past slots are not selected.
     */
    public static void validateNoPastSlots(LocalDate bookingDate,
                                           List<SingleBookingSelectionItemDTO> selections,
                                           Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        if (!bookingDate.equals(LocalDate.now())) {
            return;
        }
        LocalTime now = LocalTime.now();
        List<Map<String, Object>> pastDetails = new ArrayList<>();
        for (SingleBookingSelectionItemDTO sel : selections) {
            SingleBookingMatrixTimeSlotDTO slot = slotMap.get(sel.getSlotId());
            if (slot != null) {
                LocalTime start = LocalTime.parse(slot.getStartTime(), TF);
                if (start.isBefore(now)) {
                    pastDetails.add(Map.of("field", "slotId", "issue", "past_time_slot",
                            "rejectedValue", sel.getSlotId()));
                }
            }
        }
        if (!pastDetails.isEmpty()) {
            throw new SingleBookingValidationException("PAST_TIME_SLOT",
                    "Không thể chọn khung giờ đã qua.",
                    pastDetails);
        }
    }

    /**
     * Validates selections are not empty and facilityId is present.
     */
    public static void validateSelectionsNotEmpty(Integer facilityId,
                                                  List<SingleBookingSelectionItemDTO> selections) {
        List<Map<String, Object>> details = new ArrayList<>();
        if (facilityId == null) {
            details.add(Map.of("field", "facilityId", "issue", "required"));
        }
        if (selections == null || selections.isEmpty()) {
            details.add(Map.of("field", "selections", "issue", "required"));
        }
        if (!details.isEmpty()) {
            throw new SingleBookingValidationException("VALIDATION_ERROR",
                    "facilityId and selections are required.", details);
        }
    }

    /**
     * Validates minimum 60-minute block rule per court.
     */
    public static void validateMin60MinBlocks(List<SingleBookingSelectionItemDTO> selections,
                                              Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {
        Map<Integer, List<List<Integer>>> blocks =
                SingleBookingTimeRangeUtil.groupContiguousBlocks(selections, slotMap);
        List<Integer> violating = SingleBookingTimeRangeUtil.findBlocksUnder60Min(blocks);
        if (!violating.isEmpty()) {
            List<Map<String, Object>> details = violating.stream()
                    .map(courtId -> Map.<String, Object>of(
                            "field", "courtId",
                            "issue", "min_block_60_minutes",
                            "rejectedValue", courtId))
                    .collect(Collectors.toList());
            throw new SingleBookingValidationException("MIN_DURATION_60M",
                    "Mỗi block liên tiếp trên 1 sân phải >= 60 phút (>= 2 slot 30p).",
                    details);
        }
    }

    /**
     * Validates depositPercent is either 30 or 100.
     */
    public static void validateDepositPercent(Integer depositPercent) {
        if (depositPercent == null || (depositPercent != 30 && depositPercent != 100)) {
            throw new SingleBookingValidationException("VALIDATION_ERROR",
                    "depositPercent must be 30 or 100.",
                    List.of(Map.of("field", "depositPercent", "issue", "invalid_value",
                            "rejectedValue", String.valueOf(depositPercent))));
        }
    }
}
