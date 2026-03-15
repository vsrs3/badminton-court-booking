package com.bcb.dto.recurring;

import com.bcb.dto.CourtViewDTO;

import java.util.List;

/**
 * Setup payload for recurring create/preview screens.
 * Includes courts and facility operating-time options.
 *
 * @author AnhTN
 */
public class RecurringFacilitySetupDTO {

    private String facilityName;
    private List<CourtViewDTO> courts;
    private String openTime;
    private String closeTime;
    private List<String> timeOptions;

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public List<CourtViewDTO> getCourts() {
        return courts;
    }

    public void setCourts(List<CourtViewDTO> courts) {
        this.courts = courts;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public List<String> getTimeOptions() {
        return timeOptions;
    }

    public void setTimeOptions(List<String> timeOptions) {
        this.timeOptions = timeOptions;
    }
}

