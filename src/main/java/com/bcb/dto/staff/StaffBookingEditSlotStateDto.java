package com.bcb.dto.staff;

public class StaffBookingEditSlotStateDto {
    private String slotStatus;
    private boolean released;

    public String getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(String slotStatus) {
        this.slotStatus = slotStatus;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}

