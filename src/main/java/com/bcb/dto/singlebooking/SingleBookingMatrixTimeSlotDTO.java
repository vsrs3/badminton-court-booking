package com.bcb.dto.singlebooking;

/**
 * DTO for a time slot in the booking matrix.
 *
 * @author AnhTN
 */
public class SingleBookingMatrixTimeSlotDTO {

    private Integer slotId;
    private String startTime;
    private String endTime;

    public SingleBookingMatrixTimeSlotDTO() {}

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
