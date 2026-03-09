package com.bcb.service.impl;

import com.bcb.dto.FacilityPriceViewDTO;
import com.bcb.dto.PriceRuleRequestDTO;
import com.bcb.dto.TimeSlotPriceDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.CourtType;
import com.bcb.model.Facility;
import com.bcb.model.FacilityPriceRule;
import com.bcb.repository.FacilityPriceRuleRepository;
import com.bcb.repository.CourtTypeRepository;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityPriceRuleRepositoryImpl;
import com.bcb.repository.impl.CourtTypeRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityPriceRuleService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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

        String finalDayType = (dayType != null && (dayType.equals("WEEKDAY") || dayType.equals("WEEKEND")))
                ? dayType
                : "WEEKDAY";

        // Get all price rules for this context
        List<FacilityPriceRule> priceRules = facilityPriceRuleRepository.findByFacilityAndCourtTypeAndDayType(
                facilityId, finalCourtTypeId, finalDayType);

        // Convert to DTO with price per hour (multiply by 2)
        List<TimeSlotPriceDTO> timeSlotPrices = priceRules.stream()
                .map(rule -> {
                    TimeSlotPriceDTO dto = new TimeSlotPriceDTO();
                    dto.setPriceId(rule.getPriceId());
                    dto.setStartTime(rule.getStartTime());
                    dto.setEndTime(rule.getEndTime());
                    dto.setStartTimeFormatted(formatTimeForDisplay(rule.getStartTime()));
                    dto.setEndTimeFormatted(formatTimeForDisplay(rule.getEndTime()));
                    // Convert from 30-minute price to hourly price for display
                    dto.setPrice(rule.getPrice().multiply(BigDecimal.valueOf(2)));
                    return dto;
                })
                .collect(Collectors.toList());

        // Build view object
        FacilityPriceViewDTO viewDTO = new FacilityPriceViewDTO();
        viewDTO.setFacilityId(facilityId);
        viewDTO.setFacilityName(facility.getName());
        viewDTO.setCourtTypes(courtTypes);
        viewDTO.setCurrentCourtTypeId(finalCourtTypeId);
        viewDTO.setCurrentDayType(finalDayType);
        viewDTO.setTimeSlotPrices(timeSlotPrices);

        return viewDTO;
    }

    @Override
    public void createPriceRule(PriceRuleRequestDTO request) throws BusinessException {
        // Validate context
        validateContext(request.getFacilityId(), request.getCourtTypeId(), request.getDayType());

        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validate price
        validatePrice(request.getPricePerHour());

        // Check for overlap (no exclusion needed for create)
        if (facilityPriceRuleRepository.hasOverlap(
                request.getFacilityId(),
                request.getCourtTypeId(),
                request.getDayType(),
                request.getStartTime(),
                request.getEndTime(),
                null)) {
            throw new BusinessException("Khoảng thời gian bị trùng lặp với cấu hình đã tồn tại");
        }

        // Convert price from per hour to per 30 minutes
        BigDecimal pricePer30Min = request.getPricePerHour().divide(BigDecimal.valueOf(2));

        // Create entity and insert
        FacilityPriceRule priceRule = new FacilityPriceRule();
        priceRule.setFacilityId(request.getFacilityId());
        priceRule.setCourtTypeId(request.getCourtTypeId());
        priceRule.setDayType(request.getDayType());
        priceRule.setStartTime(request.getStartTime());
        priceRule.setEndTime(request.getEndTime());
        priceRule.setPrice(pricePer30Min);

        facilityPriceRuleRepository.insert(priceRule);
    }

    @Override
    public void updatePriceRule(PriceRuleRequestDTO request) throws BusinessException {
        if (request.getPriceId() == null) {
            throw new BusinessException("Price ID is required for update");
        }

        // Verify the rule exists
        FacilityPriceRule existingRule = facilityPriceRuleRepository.findById(request.getPriceId())
                .orElseThrow(() -> new BusinessException("Price rule not found"));

        // Validate context
        validateContext(request.getFacilityId(), request.getCourtTypeId(), request.getDayType());

        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validate price
        validatePrice(request.getPricePerHour());

        // Check for overlap (exclude current rule)
        if (facilityPriceRuleRepository.hasOverlap(
                request.getFacilityId(),
                request.getCourtTypeId(),
                request.getDayType(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPriceId())) {
            throw new BusinessException("Khoảng thời gian bị trùng lặp với cấu hình đã tồn tại");
        }

        // Convert price from per hour to per 30 minutes
        BigDecimal pricePer30Min = request.getPricePerHour().divide(BigDecimal.valueOf(2));

        // Update entity
        existingRule.setFacilityId(request.getFacilityId());
        existingRule.setCourtTypeId(request.getCourtTypeId());
        existingRule.setDayType(request.getDayType());
        existingRule.setStartTime(request.getStartTime());
        existingRule.setEndTime(request.getEndTime());
        existingRule.setPrice(pricePer30Min);

        facilityPriceRuleRepository.update(existingRule);
    }

    @Override
    public void deletePriceRule(int priceId) throws BusinessException {
        // Verify the rule exists
        facilityPriceRuleRepository.findById(priceId)
                .orElseThrow(() -> new BusinessException("Price rule not found"));

        facilityPriceRuleRepository.delete(priceId);
    }

    private void validateContext(int facilityId, int courtTypeId, String dayType) throws BusinessException {
        facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found"));
        
        courtTypeRepository.findById(courtTypeId)
                .orElseThrow(() -> new BusinessException("Court type not found"));
        
        if (!"WEEKDAY".equals(dayType) && !"WEEKEND".equals(dayType)) {
            throw new BusinessException("Invalid day type. Must be WEEKDAY or WEEKEND");
        }
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime)
            throws BusinessException {
        if (startTime == null || endTime == null) {
            throw new BusinessException("Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("End time must be after start time");
        }
    }

    private void validatePrice(BigDecimal price) throws BusinessException {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
    }

    /**
     * Parse time string from UI (supports 24:00 for end of day)
     * @param timeStr Time string in HH:mm format
     * @return LocalTime object (24:00 is converted to 23:59:59.999999999)
     */
    private java.time.LocalTime parseTimeInput(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }

        // Special case: Handle 24:00 as end of day (23:59:59.999999999)
        if ("24:00".equals(timeStr)) {
            return java.time.LocalTime.of(23, 59, 59, 999999999);
        }

        return java.time.LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * Format time for display in UI (converts LocalTime.MAX back to 24:00)
     * @param time LocalTime object
     * @return Time string in HH:mm format (23:59:59.999999999 becomes 24:00)
     */
    private String formatTimeForDisplay(java.time.LocalTime time) {
        if (time == null) {
            return "";
        }

        // Special case: Display end of day as 24:00
        if (time.getHour() == 23 && time.getMinute() == 59 && time.getSecond() == 59) {
            return "24:00";
        }

        return time.format(TIME_FORMATTER);
    }
}

