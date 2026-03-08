package com.bcb.dto.staff;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailSessionDto {
    private int sessionIndex;
    private int courtId;
    private String courtName;
    private String startTime;
    private String endTime;
    private int slotCount;
    private BigDecimal totalPrice;
    private String sessionStatus;
    private String checkinTime;
    private String checkoutTime;
    private List<Integer> bookingSlotIds = new ArrayList<>();
    private List<StaffBookingDetailSlotDto> bookingSlots = new ArrayList<>();

    public int getSessionIndex() {
        return sessionIndex;
    }

    public void setSessionIndex(int sessionIndex) {
        this.sessionIndex = sessionIndex;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public void setSlotCount(int slotCount) {
        this.slotCount = slotCount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(String checkinTime) {
        this.checkinTime = checkinTime;
    }

    public String getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(String checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public List<Integer> getBookingSlotIds() {
        return bookingSlotIds;
    }

    public void setBookingSlotIds(List<Integer> bookingSlotIds) {
        this.bookingSlotIds = bookingSlotIds;
    }

    public List<StaffBookingDetailSlotDto> getBookingSlots() {
        return bookingSlots;
    }

    public void setBookingSlots(List<StaffBookingDetailSlotDto> bookingSlots) {
        this.bookingSlots = bookingSlots;
    }
}

