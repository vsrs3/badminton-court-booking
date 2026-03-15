package com.bcb.dto.staff;

public class StaffBookingListItemDTO {
    private int bookingId;
    private String customerName;
    private String phone;
    private String bookingDate;
    private boolean recurring;
    private String recurringStartDate;
    private String recurringEndDate;
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

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurringStartDate() {
        return recurringStartDate;
    }

    public void setRecurringStartDate(String recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    public String getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(String recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
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

