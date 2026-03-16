package com.bcb.dto.staff;

import java.time.LocalDate;
import java.time.LocalTime;

public class StaffCheckinSessionSlotRowDTO {
    private int bookingSlotId;
    private int courtId;
    private LocalDate sessionDate;
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

