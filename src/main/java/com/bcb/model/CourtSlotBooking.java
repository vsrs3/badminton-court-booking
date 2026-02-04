package com.bcb.model;

import java.time.LocalDate;

public class CourtSlotBooking {
    private Integer courtId;
    private LocalDate bookingDate;
    private Integer slotId;
    private Integer bookingSlotID;

    public CourtSlotBooking() {}

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
    public Integer getBookingSlotID() { return bookingSlotID; }
    public void setBookingSlotID(Integer bookingSlotID) { this.bookingSlotID = bookingSlotID; }
}