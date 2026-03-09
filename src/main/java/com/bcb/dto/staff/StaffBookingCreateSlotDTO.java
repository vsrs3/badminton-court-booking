package com.bcb.dto.staff;

public class StaffBookingCreateSlotDTO {
    private int courtId;
    private int slotId;

    public StaffBookingCreateSlotDTO() {
    }

    public StaffBookingCreateSlotDTO(int courtId, int slotId) {
        this.courtId = courtId;
        this.slotId = slotId;
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
}

