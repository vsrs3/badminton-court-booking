package com.bcb.service.singlebooking.impl;

import com.bcb.dto.singlebooking.*;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.exception.singlebooking.SingleBookingValidationException;
import com.bcb.model.Court;
import com.bcb.model.Facility;
import com.bcb.model.FacilityPriceRule;
import com.bcb.repository.booking.*;
import com.bcb.repository.booking.impl.*;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.service.singlebooking.SingleBookingPreviewService;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;
import com.bcb.utils.singlebooking.SingleBookingTimeRangeUtil;
import com.bcb.validation.singlebooking.SingleBookingSelectionValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates selections and computes a pricing preview (no hold).
 *
 * @author AnhTN
 */
public class SingleBookingPreviewServiceImpl implements SingleBookingPreviewService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final FacilityRepository facilityRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;

    public SingleBookingPreviewServiceImpl() {
        this.facilityRepo = new FacilityRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public SingleBookingPreviewResponseDTO preview(SingleBookingPreviewRequestDTO request) {
        // Validate basic fields
        SingleBookingSelectionValidator.validateSelectionsNotEmpty(request.getFacilityId(), request.getSelections());

        LocalDate bookingDate = SingleBookingSelectionValidator.parseAndValidateDate(request.getBookingDate());
        SingleBookingSelectionValidator.validateNotPastDate(bookingDate);

        // Load facility
        Facility facility = facilityRepo.findActiveById(request.getFacilityId())
                .orElseThrow(() -> new SingleBookingNotFoundException("NOT_FOUND",
                        "Facility not found with id=" + request.getFacilityId()));

        // Load slots and build map
        List<SingleBookingMatrixTimeSlotDTO> slots =
                timeSlotRepo.findByTimeRange(facility.getOpenTime(), facility.getCloseTime());
        Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap = slots.stream()
                .collect(Collectors.toMap(SingleBookingMatrixTimeSlotDTO::getSlotId, s -> s));

        // Validate no past slots if today
        SingleBookingSelectionValidator.validateNoPastSlots(bookingDate, request.getSelections(), slotMap);

        // Validate min 60 min blocks
        SingleBookingSelectionValidator.validateMin60MinBlocks(request.getSelections(), slotMap);

        // Load courts
        List<Court> courts = courtRepo.findActiveByFacilityId(request.getFacilityId());
        Map<Integer, Court> courtMap = courts.stream()
                .collect(Collectors.toMap(Court::getCourtId, c -> c));

        // Compute prices per selection
        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        Map<String, BigDecimal> priceCache = new HashMap<>(); // "courtTypeId:slotId" -> price

        // Build ranges by court
        Map<Integer, List<List<Integer>>> courtBlocks =
                SingleBookingTimeRangeUtil.groupContiguousBlocks(request.getSelections(), slotMap);

        List<SingleBookingRangeDTO> ranges = new ArrayList<>();
        BigDecimal estimatedTotal = BigDecimal.ZERO;
        int totalSlots = 0;
        int totalMinutes = 0;

        for (Map.Entry<Integer, List<List<Integer>>> courtEntry : courtBlocks.entrySet()) {
            Integer courtId = courtEntry.getKey();
            Court court = courtMap.get(courtId);
            if (court == null) {
                throw new SingleBookingNotFoundException("NOT_FOUND", "Court not found with id=" + courtId);
            }

            for (List<Integer> block : courtEntry.getValue()) {
                SingleBookingRangeDTO range = new SingleBookingRangeDTO();
                range.setCourtId(courtId);
                range.setCourtName(court.getCourtName());
                range.setSlotCount(block.size());
                range.setMinutes(block.size() * 30);

                String blockStartTime = slotMap.get(block.get(0)).getStartTime();
                String blockEndTime = slotMap.get(block.get(block.size() - 1)).getEndTime();
                range.setStartTime(blockStartTime);
                range.setEndTime(blockEndTime);

                BigDecimal blockTotal = BigDecimal.ZERO;
                List<SingleBookingMatrixSlotPriceDTO> slotPrices = new ArrayList<>();

                for (Integer slotId : block) {
                    BigDecimal price = resolvePrice(facility.getFacilityId(), court.getCourtTypeId(),
                            dayType, slotId, slotMap, priceCache);
                    blockTotal = blockTotal.add(price);

                    SingleBookingMatrixSlotPriceDTO sp = new SingleBookingMatrixSlotPriceDTO();
                    sp.setCourtId(courtId);
                    sp.setSlotId(slotId);
                    sp.setPrice(price);
                    slotPrices.add(sp);
                }

                range.setSubtotal(blockTotal);
                range.setSlotPrices(slotPrices);
                ranges.add(range);

                estimatedTotal = estimatedTotal.add(blockTotal);
                totalSlots += block.size();
                totalMinutes += block.size() * 30;
            }
        }

        SingleBookingPreviewResponseDTO resp = new SingleBookingPreviewResponseDTO();
        resp.setFacilityId(facility.getFacilityId());
        resp.setFacilityName(facility.getName());
        // Build full address: address, ward, district, province
        String fullAddress = buildFullAddress(facility);
        resp.setFacilityAddress(fullAddress);
        resp.setBookingDate(bookingDate.toString());
        resp.setTotalSlots(totalSlots);
        resp.setTotalMinutes(totalMinutes);
        resp.setEstimatedTotal(estimatedTotal);
        resp.setRangesByCourt(ranges);
        return resp;
    }

    /**
     * Builds full address string from facility location fields.
     */
    private String buildFullAddress(Facility facility) {
        StringBuilder sb = new StringBuilder();
        if (facility.getAddress() != null && !facility.getAddress().isBlank()) {
            sb.append(facility.getAddress());
        }
        if (facility.getWard() != null && !facility.getWard().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(facility.getWard());
        }
        if (facility.getDistrict() != null && !facility.getDistrict().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(facility.getDistrict());
        }
        if (facility.getProvince() != null && !facility.getProvince().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(facility.getProvince());
        }
        return sb.toString();
    }

    /**
     * Resolves price for a specific slot and court type, with caching.
     * Throws validation errors for missing or overlapping rules.
     */
    private BigDecimal resolvePrice(int facilityId, int courtTypeId, String dayType,
                                    int slotId, Map<Integer, SingleBookingMatrixTimeSlotDTO> slotMap,
                                    Map<String, BigDecimal> priceCache) {
        String cacheKey = courtTypeId + ":" + slotId;
        if (priceCache.containsKey(cacheKey)) {
            return priceCache.get(cacheKey);
        }

        SingleBookingMatrixTimeSlotDTO slot = slotMap.get(slotId);
        LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
        LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);

        List<FacilityPriceRule> rules =
                priceRuleRepo.findByFacilityAndCourtTypeAndDayType(facilityId, courtTypeId, dayType);
        List<FacilityPriceRule> matching = rules.stream()
                .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            throw new SingleBookingValidationException("PRICE_RULE_MISSING",
                    "No price rule found for slot " + slot.getStartTime() + "-" + slot.getEndTime(),
                    List.of(Map.of("field", "slotId", "issue", "no_price_rule", "rejectedValue", slotId)));
        }
        if (matching.size() > 1) {
            throw new SingleBookingValidationException("PRICE_RULE_OVERLAPPED",
                    "Multiple price rules match slot " + slot.getStartTime() + "-" + slot.getEndTime(),
                    List.of(Map.of("field", "slotId", "issue", "overlapped_price_rules", "rejectedValue", slotId)));
        }

        BigDecimal price = matching.get(0).getPrice();
        priceCache.put(cacheKey, price);
        return price;
    }
}
