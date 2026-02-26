package com.bcb.utils.singlebooking;

import com.bcb.dto.singlebooking.SingleBookingMatrixTimeSlotDTO;
import com.bcb.dto.singlebooking.SingleBookingSelectionItemDTO;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for grouping selected slots into contiguous time ranges per court
 * and checking minimum 60-minute block rule.
 *
 * @author AnhTN
 */
public final class SingleBookingTimeRangeUtil {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private SingleBookingTimeRangeUtil() {}

    /**
     * Groups selections by courtId, sorts slots by startTime, and returns contiguous blocks.
     * Each block is a list of slotIds that form a consecutive time range.
     *
     * @param selections user selections
     * @param slotMap    slotId -> SingleBookingMatrixTimeSlotDTO
     * @return courtId -> list of contiguous blocks (each block = list of slotIds ordered by time)
     */
    public static Map<Integer, List<List<Integer>>> groupContiguousBlocks(
            List<SingleBookingSelectionItemDTO> selections,
            Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap) {

        // Group by courtId
        Map<Integer, List<Integer>> courtSlots = new LinkedHashMap<>();
        for (SingleBookingSelectionItemDTO sel : selections) {
            courtSlots.computeIfAbsent(sel.getCourtId(), k -> new ArrayList<>()).add(sel.getSlotId());
        }

        Map<Integer, List<List<Integer>>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : courtSlots.entrySet()) {
            Integer courtId = entry.getKey();
            List<Integer> slotIds = entry.getValue();

            // Sort by startTime
            slotIds.sort(Comparator.comparing(sid -> LocalTime.parse(slotMap.get(sid).getStartTime(), TF)));

            List<List<Integer>> blocks = new ArrayList<>();
            List<Integer> currentBlock = new ArrayList<>();
            currentBlock.add(slotIds.get(0));

            for (int i = 1; i < slotIds.size(); i++) {
                SingleBookingMatrixTimeSlotDTO prev = slotMap.get(slotIds.get(i - 1));
                SingleBookingMatrixTimeSlotDTO curr = slotMap.get(slotIds.get(i));
                LocalTime prevEnd = LocalTime.parse(prev.getEndTime(), TF);
                LocalTime currStart = LocalTime.parse(curr.getStartTime(), TF);

                if (prevEnd.equals(currStart)) {
                    currentBlock.add(slotIds.get(i));
                } else {
                    blocks.add(currentBlock);
                    currentBlock = new ArrayList<>();
                    currentBlock.add(slotIds.get(i));
                }
            }
            blocks.add(currentBlock);
            result.put(courtId, blocks);
        }
        return result;
    }

    /**
     * Validates that every contiguous block per court has >= 2 slots (>= 60 min).
     *
     * @return list of courtIds that violate the rule (empty if valid)
     */
    public static List<Integer> findBlocksUnder60Min(Map<Integer, List<List<Integer>>> courtBlocks) {
        List<Integer> violating = new ArrayList<>();
        for (Map.Entry<Integer, List<List<Integer>>> entry : courtBlocks.entrySet()) {
            for (List<Integer> block : entry.getValue()) {
                if (block.size() < 2) {
                    if (!violating.contains(entry.getKey())) {
                        violating.add(entry.getKey());
                    }
                }
            }
        }
        return violating;
    }
}
