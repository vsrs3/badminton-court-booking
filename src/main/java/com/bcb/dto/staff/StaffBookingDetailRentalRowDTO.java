package com.bcb.dto.staff;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailRentalRowDTO {
    private int courtId;
    private String courtName;
    private String bookingDate;
    private int slotId;
    private String startTime;
    private String endTime;
    private String rentalItemsText;
    private BigDecimal rentalTotal;
    private List<StaffBookingDetailRentalItemDTO> rentalItems = new ArrayList<>();

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

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
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

    public String getRentalItemsText() {
        return rentalItemsText;
    }

    public void setRentalItemsText(String rentalItemsText) {
        this.rentalItemsText = rentalItemsText;
    }

    public BigDecimal getRentalTotal() {
        return rentalTotal;
    }

    public void setRentalTotal(BigDecimal rentalTotal) {
        this.rentalTotal = rentalTotal;
    }

    public List<StaffBookingDetailRentalItemDTO> getRentalItems() {
        return rentalItems;
    }

    public void setRentalItems(List<StaffBookingDetailRentalItemDTO> rentalItems) {
        this.rentalItems = rentalItems;
    }
}
