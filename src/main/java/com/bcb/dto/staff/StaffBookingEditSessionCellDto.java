package com.bcb.dto.staff;

import java.time.LocalTime;

public class StaffBookingEditSessionCellDto {
    private int bookingSlotId;
    private int courtId;
    private int slotId;
    private String slotStatus;
    private LocalTime start;
    private LocalTime end;

    public int getBookingSlotId() {
        return bookingSlotId;
    }

    public void setBookingSlotId(int bookingSlotId) {
        this.bookingSlotId = bookingSlotId;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(String slotStatus) {
        this.slotStatus = slotStatus;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}

