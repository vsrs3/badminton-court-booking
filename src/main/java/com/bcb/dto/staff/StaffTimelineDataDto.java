package com.bcb.dto.staff;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffTimelineDataDto {
    private String facilityName;
    private LocalDate bookingDate;
    private List<StaffTimelineCourtDto> courts = new ArrayList<>();
    private List<StaffTimelineSlotDto> slots = new ArrayList<>();
    private List<StaffTimelineBookedCellDto> bookedCells = new ArrayList<>();
    private List<StaffTimelineDisabledCellDto> disabledCells = new ArrayList<>();

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public List<StaffTimelineCourtDto> getCourts() {
        return courts;
    }

    public void setCourts(List<StaffTimelineCourtDto> courts) {
        this.courts = courts;
    }

    public List<StaffTimelineSlotDto> getSlots() {
        return slots;
    }

    public void setSlots(List<StaffTimelineSlotDto> slots) {
        this.slots = slots;
    }

    public List<StaffTimelineBookedCellDto> getBookedCells() {
        return bookedCells;
    }

    public void setBookedCells(List<StaffTimelineBookedCellDto> bookedCells) {
        this.bookedCells = bookedCells;
    }

    public List<StaffTimelineDisabledCellDto> getDisabledCells() {
        return disabledCells;
    }

    public void setDisabledCells(List<StaffTimelineDisabledCellDto> disabledCells) {
        this.disabledCells = disabledCells;
    }
}

