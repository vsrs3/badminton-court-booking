package com.bcb.service.impl;

import com.bcb.dto.staff.StaffFacilityHoursDto;
import com.bcb.dto.staff.StaffPriceRuleDto;
import com.bcb.dto.staff.StaffSlotPriceCourtDto;
import com.bcb.dto.staff.StaffSlotPriceDataDto;
import com.bcb.dto.staff.StaffSlotPriceItemDto;
import com.bcb.dto.staff.StaffTimeSlotDto;
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
    public StaffSlotPriceDataDto getSlotPrices(int facilityId, LocalDate bookingDate) throws Exception {
        DayOfWeek dow = bookingDate.getDayOfWeek();
        String dayType = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";

        List<StaffSlotPriceCourtDto> courts = repository.findActiveCourts(facilityId);
        StaffFacilityHoursDto hours = repository.findFacilityHours(facilityId);
        List<StaffTimeSlotDto> slots = repository.findTimeSlotsWithinHours(hours.getOpenTime(), hours.getCloseTime());
        List<StaffPriceRuleDto> rules = repository.findPriceRules(facilityId, dayType);

        List<StaffSlotPriceItemDto> prices = new ArrayList<>();

        for (StaffSlotPriceCourtDto court : courts) {
            for (StaffTimeSlotDto slot : slots) {
                StaffPriceRuleDto matchedRule = findMatchedRule(rules, court.getCourtTypeId(), slot.getStartTime(), slot.getEndTime());
                if (matchedRule == null) {
                    continue;
                }
                StaffSlotPriceItemDto item = new StaffSlotPriceItemDto();
                item.setCourtId(court.getCourtId());
                item.setSlotId(slot.getSlotId());
                item.setPrice(matchedRule.getPrice());
                prices.add(item);
            }
        }

        StaffSlotPriceDataDto data = new StaffSlotPriceDataDto();
        data.setDayType(dayType);
        data.setPrices(prices);
        return data;
    }

    private StaffPriceRuleDto findMatchedRule(List<StaffPriceRuleDto> rules, int courtTypeId,
                                              LocalTime slotStart, LocalTime slotEnd) {
        for (StaffPriceRuleDto rule : rules) {
            if (rule.getCourtTypeId() == courtTypeId
                    && !slotStart.isBefore(rule.getStartTime())
                    && !slotEnd.isAfter(rule.getEndTime())) {
                return rule;
            }
        }
        return null;
    }
}
