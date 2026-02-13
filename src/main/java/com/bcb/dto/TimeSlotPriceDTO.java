package com.bcb.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TimeSlotPriceDTO {
    private int slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private String startTimeFormatted;
    private String endTimeFormatted;

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public void setStartTimeFormatted(String startTimeFormatted) {
        this.startTimeFormatted = startTimeFormatted;
    }

    public String getEndTimeFormatted() {
        return endTimeFormatted;
    }

    public void setEndTimeFormatted(String endTimeFormatted) {
        this.endTimeFormatted = endTimeFormatted;
    }
}
