package com.bcb.dto;

import com.bcb.model.CourtType;
import java.util.List;

public class FacilityPriceViewDTO {
    private int facilityId;
    private String facilityName;
    private List<CourtType> courtTypes;
    private int currentCourtTypeId;
    private String currentDayType;
    private List<TimeSlotPriceDTO> timeSlotPrices;

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public List<CourtType> getCourtTypes() {
        return courtTypes;
    }

    public void setCourtTypes(List<CourtType> courtTypes) {
        this.courtTypes = courtTypes;
    }

    public int getCurrentCourtTypeId() {
        return currentCourtTypeId;
    }

    public void setCurrentCourtTypeId(int currentCourtTypeId) {
        this.currentCourtTypeId = currentCourtTypeId;
    }

    public String getCurrentDayType() {
        return currentDayType;
    }

    public void setCurrentDayType(String currentDayType) {
        this.currentDayType = currentDayType;
    }

    public List<TimeSlotPriceDTO> getTimeSlotPrices() {
        return timeSlotPrices;
    }

    public void setTimeSlotPrices(List<TimeSlotPriceDTO> timeSlotPrices) {
        this.timeSlotPrices = timeSlotPrices;
    }
}
