package com.bcb.dto.staff;

import java.math.BigDecimal;

public class StaffBookingDetailRentalRowDTO {
    private String courtName;
    private String startTime;
    private String endTime;
    private String rentalItemsText;
    private BigDecimal rentalTotal;

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
}