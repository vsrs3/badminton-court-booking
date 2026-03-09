package com.bcb.dto.singlebooking;

/**
 * DTO representing a single court+slot selection item from client.
 *
 * @author AnhTN
 */
public class SingleBookingSelectionItemDTO {

    private Integer courtId;
    private Integer slotId;

    public SingleBookingSelectionItemDTO() {}

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
}

