package com.bcb.dto.staff;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffTimelineDataDTO {
    private String facilityName;
    private LocalDate bookingDate;
    private List<StaffTimelineCourtDTO> courts = new ArrayList<>();
    private List<StaffTimelineSlotDTO> slots = new ArrayList<>();
    private List<StaffTimelineBookedCellDTO> bookedCells = new ArrayList<>();
    private List<StaffTimelineDisabledCellDTO> disabledCells = new ArrayList<>();

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

    public List<StaffTimelineCourtDTO> getCourts() {
        return courts;
    }

    public void setCourts(List<StaffTimelineCourtDTO> courts) {
        this.courts = courts;
    }

    public List<StaffTimelineSlotDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<StaffTimelineSlotDTO> slots) {
        this.slots = slots;
    }

    public List<StaffTimelineBookedCellDTO> getBookedCells() {
        return bookedCells;
    }

    public void setBookedCells(List<StaffTimelineBookedCellDTO> bookedCells) {
        this.bookedCells = bookedCells;
    }

    public List<StaffTimelineDisabledCellDTO> getDisabledCells() {
        return disabledCells;
    }

    public void setDisabledCells(List<StaffTimelineDisabledCellDTO> disabledCells) {
        this.disabledCells = disabledCells;
    }
}

