package com.bcb.model;

import java.math.BigDecimal;

public class BookingSlot {
    private Integer bookingSlotId;
    private Integer bookingId;
    private Integer courtID;
    private Integer slotId;
    private BigDecimal price;

    public BookingSlot() {}

    public Integer getBookingSlotId() { return bookingSlotId; }
    public void setBookingSlotId(Integer bookingSlotId) { this.bookingSlotId = bookingSlotId; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getCourtID() {
        return courtID;
    }

    public void setCourtID(Integer courtID) {
        this.courtID = courtID;
    }
}