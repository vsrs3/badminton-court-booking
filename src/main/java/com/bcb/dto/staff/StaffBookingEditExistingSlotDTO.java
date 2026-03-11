package com.bcb.dto.staff;

public class StaffBookingEditExistingSlotDTO {
    private Integer bookingSlotId;
    private String slotStatus;

    public Integer getBookingSlotId() {
        return bookingSlotId;
    }

    public void setBookingSlotId(Integer bookingSlotId) {
        this.bookingSlotId = bookingSlotId;
    }

    public String getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(String slotStatus) {
        this.slotStatus = slotStatus;
    }
}

