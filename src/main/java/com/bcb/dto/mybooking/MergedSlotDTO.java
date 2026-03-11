package com.bcb.dto.mybooking;

import java.math.BigDecimal;

/**
 * DTO representing a merged (consecutive) time block for one court.
 * Used in the booking detail view to display gộp các khung giờ liên tiếp cùng sân.
 *
 * <p>Example: Sân A, 08:00-09:30 — merged from 08:00-08:30, 08:30-09:00, 09:00-09:30.
 *
 * @author AnhTN
 */
public class MergedSlotDTO {

    private String courtName;
    private String startTime;       // HH:mm — start of merged block
    private String endTime;         // HH:mm — end of merged block
    private BigDecimal totalPrice;  // sum of all slot prices within the merged block
    private int slotCount;          // number of original slots merged

    public MergedSlotDTO() {}

    public MergedSlotDTO(String courtName, String startTime, String endTime,
                         BigDecimal totalPrice, int slotCount) {
        this.courtName = courtName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.slotCount = slotCount;
    }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public int getSlotCount() { return slotCount; }
    public void setSlotCount(int slotCount) { this.slotCount = slotCount; }
}
