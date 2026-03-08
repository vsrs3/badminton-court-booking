package com.bcb.dto.staff;

public class StaffBookingCreateSlotDto {
    private int courtId;
    private int slotId;

    public StaffBookingCreateSlotDto() {
    }

    public StaffBookingCreateSlotDto(int courtId, int slotId) {
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
