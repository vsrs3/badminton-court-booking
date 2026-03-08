package com.bcb.dto.staff;

public class StaffBookingListItemDto {
    private int bookingId;
    private String customerName;
    private String phone;
    private String bookingDate;
    private String bookingStatus;
    private String paymentStatus;
    private String courtDisplay;
    private boolean hasNoShow;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCourtDisplay() {
        return courtDisplay;
    }

    public void setCourtDisplay(String courtDisplay) {
        this.courtDisplay = courtDisplay;
    }

    public boolean isHasNoShow() {
        return hasNoShow;
    }

    public void setHasNoShow(boolean hasNoShow) {
        this.hasNoShow = hasNoShow;
    }
}
