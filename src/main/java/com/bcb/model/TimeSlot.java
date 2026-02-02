package com.bcb.model;

import java.time.LocalTime;

public class TimeSlot {
    private Integer slotId;
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot() {}

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}