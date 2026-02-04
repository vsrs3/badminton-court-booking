package com.bcb.service.impl;

import com.bcb.dto.BulkPriceUpdateRequestDTO;
import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.TimeSlotPriceDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.CourtType;
import com.bcb.model.Facility;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.CourtTypeRepository;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.repository.impl.CourtTypeRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityPriceRuleService;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FacilityPriceRuleServiceImpl implements FacilityPriceRuleService {

    private final FacilityPriceRuleRepository facilityPriceRuleRepository;
    private final CourtTypeRepository courtTypeRepository;
    private final FacilityRepository facilityRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public FacilityPriceRuleServiceImpl() {
        this.facilityPriceRuleRepository = new FacilityPriceRuleRepositoryImpl();
        this.courtTypeRepository = new CourtTypeRepositoryImpl();
        this.facilityRepository = new FacilityRepositoryImpl();
    }

    @Override
    public FacilityPriceViewDTO getPriceView(int facilityId, Integer courtTypeId, String dayType)
            throws BusinessException {

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found"));

        List<CourtType> courtTypes = courtTypeRepository.findAll();
        if (courtTypes.isEmpty()) {
            throw new BusinessException("No court types available in system");
        }

        int finalCourtTypeId = (courtTypeId != null)
                ? courtTypeId
                : courtTypes.get(0).getCourtTypeId();

        String finalDayType = (dayType != null)
                ? dayType
                : "WEEKDAY";

        // Get all slot
        List<TimeSlotPriceDTO> prices =
                facilityPriceRuleRepository.findTimeSlotPrices(
                        facilityId, finalCourtTypeId, finalDayType);

        // Filter slot
        LocalTime openTime = facility.getOpenTime();
        LocalTime closeTime = facility.getCloseTime();

        List<TimeSlotPriceDTO> filteredPrices = prices.stream()
                .filter(p -> {
                    LocalTime start = p.getStartTime();
                    return !start.isBefore(openTime)
                            && start.plusHours(1).isAfter(openTime)
                            && start.isBefore(closeTime);
                })
                .collect(Collectors.toList());

        // Format time
        filteredPrices.forEach(p -> {
            p.setStartTimeFormatted(p.getStartTime().format(TIME_FORMATTER));
            p.setEndTimeFormatted(p.getEndTime().format(TIME_FORMATTER));
        });

        // build view object
        FacilityPriceViewDTO viewDTO = new FacilityPriceViewDTO();
        viewDTO.setFacilityId(facilityId);
        viewDTO.setFacilityName(facility.getName());
        viewDTO.setCourtTypes(courtTypes);
        viewDTO.setCurrentCourtTypeId(finalCourtTypeId);
        viewDTO.setCurrentDayType(finalDayType);
        viewDTO.setTimeSlotPrices(filteredPrices);

        return viewDTO;
    }

    @Override
    public void updateSinglePrice(int facilityId, int courtTypeId, String dayType, int slotId, BigDecimal price) throws BusinessException {
        validateContext(facilityId, courtTypeId, dayType, slotId);
        validatePrice(price);
        facilityPriceRuleRepository.upsertPrice(facilityId, courtTypeId, dayType, slotId, price);
    }

    @Override
    public void bulkUpdatePrices(BulkPriceUpdateRequestDTO request) throws BusinessException {
        validatePrice(request.getPrice());


        for (Integer slotId : request.getSlotIds()) {
            validateContext(
                    request.getFacilityId(),
                    request.getCourtTypeId(),
                    request.getDayType(),
                    slotId
            );
        }

        facilityPriceRuleRepository.bulkUpsertPrices(
                request.getFacilityId(),
                request.getCourtTypeId(),
                request.getDayType(),
                request.getSlotIds(),
                request.getPrice()
        );
    }

    private void validateContext(int facilityId, int courtTypeId, String dayType, Integer slotId) throws BusinessException {
        facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found"));
        
        courtTypeRepository.findById(courtTypeId)
                .orElseThrow(() -> new BusinessException("Court type not found"));
        
        if (!"WEEKDAY".equals(dayType) && !"WEEKEND".equals(dayType)) {
            throw new BusinessException("Invalid day type");
        }
        
        if (slotId != null) {
            // check if slotId exists in the time slot list
            List<TimeSlotPriceDTO> allSlots = facilityPriceRuleRepository.findTimeSlotPrices(facilityId, courtTypeId, dayType);
            boolean exists = allSlots.stream().anyMatch(s -> s.getSlotId() == slotId);
            if (!exists) {
                throw new BusinessException("Slot not found");
            }
        }
    }

    private void validatePrice(BigDecimal price) throws BusinessException {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
    }
}
