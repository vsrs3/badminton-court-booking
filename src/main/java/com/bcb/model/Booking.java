package com.bcb.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private Integer bookingId;
    private Integer recurringId;
    private LocalDate bookingDate;
    private Integer accountId;
    private Integer guestId;
    private Integer staffId;
    private String bookingStatus;
    private LocalDateTime holdExpiredAt;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime createdAt;

    public Booking() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public Integer getRecurringId() { return recurringId; }
    public void setRecurringId(Integer recurringId) { this.recurringId = recurringId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public Integer getGuestId() { return guestId; }
    public void setGuestId(Integer guestId) { this.guestId = guestId; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public LocalDateTime getHoldExpiredAt() { return holdExpiredAt; }
    public void setHoldExpiredAt(LocalDateTime holdExpiredAt) { this.holdExpiredAt = holdExpiredAt; }
    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}