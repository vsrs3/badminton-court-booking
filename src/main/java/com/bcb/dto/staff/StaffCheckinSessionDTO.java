package com.bcb.dto.staff;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StaffCheckinSessionDTO {
    private List<Integer> slotIds = new ArrayList<>();
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public List<Integer> getSlotIds() {
        return slotIds;
    }

    public void setSlotIds(List<Integer> slotIds) {
        this.slotIds = slotIds;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}

