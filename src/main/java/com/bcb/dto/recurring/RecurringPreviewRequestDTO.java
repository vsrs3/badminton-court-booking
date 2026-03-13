package com.bcb.dto.recurring;

import java.util.List;

/**
 * Request body for recurring preview.
 *
 * @author AnhTN
 */
public class RecurringPreviewRequestDTO {

    private Integer facilityId;
    private String startDate; // YYYY-MM-DD
    private String endDate;   // YYYY-MM-DD
    private List<RecurringPatternDTO> patterns;

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<RecurringPatternDTO> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<RecurringPatternDTO> patterns) {
        this.patterns = patterns;
    }
}

