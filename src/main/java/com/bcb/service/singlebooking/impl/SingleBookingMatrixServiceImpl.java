package com.bcb.service.singlebooking.impl;

import com.bcb.dto.singlebooking.*;
import com.bcb.exception.singlebooking.SingleBookingNotFoundException;
import com.bcb.model.Court;
import com.bcb.model.CourtScheduleException;
import com.bcb.model.Facility;
import com.bcb.model.FacilityPriceRule;
import com.bcb.repository.booking.*;
import com.bcb.repository.booking.impl.*;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.service.singlebooking.SingleBookingMatrixService;
import com.bcb.utils.singlebooking.SingleBookingDayTypeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds the single-booking matrix data including courts, slots, booked/disabled maps, and prices.
 *
 * @author AnhTN
 */
public class SingleBookingMatrixServiceImpl implements SingleBookingMatrixService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final FacilityRepository facilityRepo;
    private final CourtRepository courtRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FacilityPriceRuleRepository priceRuleRepo;
    private final CourtScheduleExceptionRepository exceptionRepo;
    private final CourtSlotBookingRepository courtSlotBookingRepo;

    public SingleBookingMatrixServiceImpl() {
        this.facilityRepo = new FacilityRepositoryImpl();
        this.courtRepo = new CourtRepositoryImpl();
        this.timeSlotRepo = new TimeSlotRepositoryImpl();
        this.priceRuleRepo = new FacilityPriceRuleRepositoryImpl();
        this.exceptionRepo = new CourtScheduleExceptionRepositoryImpl();
        this.courtSlotBookingRepo = new CourtSlotBookingRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public SingleBookingMatrixResponseDTO getMatrixData(int facilityId, LocalDate bookingDate) {
        // 1. Load facility
        Facility facility = facilityRepo.findActiveById(facilityId)
                .orElseThrow(() -> new SingleBookingNotFoundException("NOT_FOUND",
                        "Facility not found with id=" + facilityId));

        // 2. Load courts
        List<Court> courts = courtRepo.findActiveByFacilityId(facilityId);

        // 3. Load time slots within open/close
        List<SingleBookingMatrixTimeSlotDTO> slots =
                timeSlotRepo.findByTimeRange(facility.getOpenTime(), facility.getCloseTime());

        // 4. Load booked slots
        Map<Integer, List<Integer>> booked = courtSlotBookingRepo.findBookedSlots(facilityId, bookingDate);

        // 5. Load exceptions -> disabled
        List<CourtScheduleException> exceptions = exceptionRepo.findActiveByFacilityAndDate(facilityId, bookingDate);
        Map<Integer, List<Integer>> disabled = buildDisabledMap(courts, slots, exceptions, bookingDate);

        // 6. Compute prices
        String dayType = SingleBookingDayTypeUtil.resolve(bookingDate);
        List<SingleBookingMatrixSlotPriceDTO> prices = computePrices(facility, courts, slots, dayType);

        // 7. Build response
        SingleBookingMatrixResponseDTO resp = new SingleBookingMatrixResponseDTO();

        SingleBookingFacilityDTO fDto = new SingleBookingFacilityDTO();
        fDto.setFacilityId(facility.getFacilityId());
        fDto.setName(facility.getName());
        fDto.setOpenTime(facility.getOpenTime().format(TF));
        fDto.setCloseTime(facility.getCloseTime().format(TF));
        resp.setFacility(fDto);

        resp.setBookingDate(bookingDate.toString());
        resp.setServerDate(LocalDate.now().toString());
        resp.setMinSelectableDate(LocalDate.now().toString());

        resp.setCourts(courts.stream().map(c -> {
            SingleBookingMatrixCourtDTO dto = new SingleBookingMatrixCourtDTO();
            dto.setCourtId(c.getCourtId());
            dto.setCourtName(c.getCourtName());
            dto.setCourtTypeId(c.getCourtTypeId());
            return dto;
        }).collect(Collectors.toList()));

        resp.setSlots(slots);
        resp.setBooked(booked);
        resp.setDisabled(disabled);
        resp.setPrices(prices);

        return resp;
    }

    /**
     * Builds a disabled map: courtId -> list of disabled slotIds.
     * A slot is disabled if it falls within a CourtScheduleException range or is past (today).
     */
    private Map<Integer, List<Integer>> buildDisabledMap(
            List<Court> courts,
            List<SingleBookingMatrixTimeSlotDTO> slots,
            List<CourtScheduleException> exceptions,
            LocalDate bookingDate) {

        Map<Integer, List<Integer>> disabled = new LinkedHashMap<>();
        LocalTime now = LocalTime.now();
        boolean isToday = bookingDate.equals(LocalDate.now());

        for (Court court : courts) {
            List<Integer> disabledSlots = new ArrayList<>();
            for (SingleBookingMatrixTimeSlotDTO slot : slots) {
                LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);

                // Past check for today
                if (isToday && slotStart.isBefore(now)) {
                    disabledSlots.add(slot.getSlotId());
                    continue;
                }

                // Exception check
                LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);
                boolean isException = exceptions.stream().anyMatch(ex -> {
                    if (!ex.getCourtId().equals(court.getCourtId())) {
                        return false;
                    }
                    // If start_time/end_time are null -> whole day exception
                    if (ex.getStartTime() == null || ex.getEndTime() == null) {
                        return true;
                    }
                    // Slot overlaps with exception time range
                    return slotStart.isBefore(ex.getEndTime()) && slotEnd.isAfter(ex.getStartTime());
                });
                if (isException) {
                    disabledSlots.add(slot.getSlotId());
                }
            }
            if (!disabledSlots.isEmpty()) {
                disabled.put(court.getCourtId(), disabledSlots);
            }
        }
        return disabled;
    }

    /**
     * Computes price for each (court, slot) combination.
     * FacilityPriceRule uses start_time/end_time range; price is for 30-minute slot.
     * Rule must cover slot: rule.start_time &lt;= slot.start_time AND rule.end_time &gt;= slot.end_time.
     */
    private List<SingleBookingMatrixSlotPriceDTO> computePrices(
            Facility facility,
            List<Court> courts,
            List<SingleBookingMatrixTimeSlotDTO> slots,
            String dayType) {

        // Group courts by courtTypeId
        Map<Integer, List<Court>> courtsByType = courts.stream()
                .collect(Collectors.groupingBy(Court::getCourtTypeId));

        List<SingleBookingMatrixSlotPriceDTO> prices = new ArrayList<>();

        for (Map.Entry<Integer, List<Court>> entry : courtsByType.entrySet()) {
            int courtTypeId = entry.getKey();
            List<Court> typeCourts = entry.getValue();

            List<FacilityPriceRule> rules =
                    priceRuleRepo.findByFacilityAndCourtTypeAndDayType(facility.getFacilityId(), courtTypeId, dayType);

            for (SingleBookingMatrixTimeSlotDTO slot : slots) {
                LocalTime slotStart = LocalTime.parse(slot.getStartTime(), TF);
                LocalTime slotEnd = LocalTime.parse(slot.getEndTime(), TF);

                // Find matching rules: rule.start_time <= slot.start_time AND rule.end_time >= slot.end_time
                List<FacilityPriceRule> matchingRules = rules.stream()
                        .filter(r -> !r.getStartTime().isAfter(slotStart) && !r.getEndTime().isBefore(slotEnd))
                        .collect(Collectors.toList());

                BigDecimal price;
                if (matchingRules.isEmpty()) {
                    // PRICE_RULE_MISSING - skip price for this slot (will be validated at preview/confirm)
                    price = null;
                } else if (matchingRules.size() > 1) {
                    // PRICE_RULE_OVERLAPPED - skip
                    price = null;
                } else {
                    price = matchingRules.get(0).getPrice();
                }

                for (Court court : typeCourts) {
                    SingleBookingMatrixSlotPriceDTO dto = new SingleBookingMatrixSlotPriceDTO();
                    dto.setCourtId(court.getCourtId());
                    dto.setSlotId(slot.getSlotId());
                    dto.setPrice(price);
                    prices.add(dto);
                }
            }
        }
        return prices;
    }
}
