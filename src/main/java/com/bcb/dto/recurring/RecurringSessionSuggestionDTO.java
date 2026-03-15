package com.bcb.dto.recurring;

import java.math.BigDecimal;
import java.util.List;

/**
 * Suggested alternative for a conflicted recurring session.
 *
 * @author AnhTN
 */
public class RecurringSessionSuggestionDTO {

    private String type; // SAME_COURT_NEAREST_TIME | OTHER_COURT_SAME_TIME | OTHER_COURT_NEAREST_TIME
    private Integer courtId;
    private String courtName;
    private String startTime;
    private String endTime;
    private List<Integer> slots;
    private BigDecimal price;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCourtId() {
        return courtId;
    }

    public void setCourtId(Integer courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

