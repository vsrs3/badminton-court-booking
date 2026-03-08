package com.bcb.dto.staff;

import java.time.LocalTime;

public class StaffCheckinSessionSlotRowDto {
    private int bookingSlotId;
    private int courtId;
    private LocalTime startTime;
    private LocalTime endTime;

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

