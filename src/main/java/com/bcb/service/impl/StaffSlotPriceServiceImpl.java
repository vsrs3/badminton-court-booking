package com.bcb.service.impl;

import com.bcb.dto.staff.StaffFacilityHoursDTO;
import com.bcb.dto.staff.StaffPriceRuleDTO;
import com.bcb.dto.staff.StaffSlotPriceCourtDTO;
import com.bcb.dto.staff.StaffSlotPriceDataDTO;
import com.bcb.dto.staff.StaffSlotPriceItemDTO;
import com.bcb.dto.staff.StaffTimeSlotDTO;
import com.bcb.repository.impl.StaffSlotPriceRepositoryImpl;
import com.bcb.repository.staff.StaffSlotPriceRepository;
import com.bcb.service.staff.StaffSlotPriceService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StaffSlotPriceServiceImpl implements StaffSlotPriceService {

    private final StaffSlotPriceRepository repository = new StaffSlotPriceRepositoryImpl();

    @Override
    public StaffSlotPriceDataDTO getSlotPrices(int facilityId, LocalDate bookingDate) throws Exception {
        DayOfWeek dow = bookingDate.getDayOfWeek();
        String dayType = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        List<StaffSlotPriceCourtDTO> courts = repository.findActiveCourts(facilityId);
        StaffFacilityHoursDTO hours = repository.findFacilityHours(facilityId);
        List<StaffTimeSlotDTO> slots = repository.findTimeSlotsWithinHours(hours.getOpenTime(), hours.getCloseTime());
        List<StaffPriceRuleDTO> rules = repository.findPriceRules(facilityId, dayType);

        List<StaffSlotPriceItemDTO> prices = new ArrayList<>();

        for (StaffSlotPriceCourtDTO court : courts) {
            for (StaffTimeSlotDTO slot : slots) {
                StaffPriceRuleDTO matchedRule = findMatchedRule(rules, court.getCourtTypeId(), slot.getStartTime(), slot.getEndTime());
                if (matchedRule == null) {
                    continue;
                }
                StaffSlotPriceItemDTO item = new StaffSlotPriceItemDTO();
                item.setCourtId(court.getCourtId());
                item.setSlotId(slot.getSlotId());
                item.setPrice(matchedRule.getPrice());
                prices.add(item);
            }
        }

        StaffSlotPriceDataDTO data = new StaffSlotPriceDataDTO();
        data.setDayType(dayType);
        data.setPrices(prices);
        return data;
    }

    private StaffPriceRuleDTO findMatchedRule(List<StaffPriceRuleDTO> rules, int courtTypeId,
                                              LocalTime slotStart, LocalTime slotEnd) {
        for (StaffPriceRuleDTO rule : rules) {
            if (rule.getCourtTypeId() == courtTypeId
                    && !slotStart.isBefore(rule.getStartTime())
                    && !slotEnd.isAfter(rule.getEndTime())) {
                return rule;
            }
        }
        return null;
    }
}

