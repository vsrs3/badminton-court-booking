package com.bcb.dto.mybooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO representing a booking item in "My Bookings" list.
 * Includes holdExpiredAt and paidAmount to support Pay / Pay Remaining buttons.
 *
 * @author AnhTN
 */
public class MyBookingListDTO {
    private int bookingId;
    private String facilityName;
    private String fullAddress;         // province + district + ward + address
    private LocalDate bookingDate;
    private String bookingStatus;       // PENDING, CONFIRMED, EXPIRED, CANCELLED, COMPLETED
    private String bookingType;         // SINGLE or RECURRING
    private String slotDetails;         // merged consecutive slots, e.g. "Sân A1: 17:00-19:00"
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;      // amount already paid (for PARTIAL status)
    private String paymentStatus;       // UNPAID, PARTIAL, PAID
    private LocalDateTime createdAt;
    private LocalDateTime holdExpiredAt; // hold expiry for PENDING bookings
    private String thumbnailPath;       // facility image

    private boolean reviewed;           // check customer review or not

    public MyBookingListDTO() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getSlotDetails() { return slotDetails; }
    public void setSlotDetails(String slotDetails) { this.slotDetails = slotDetails; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getHoldExpiredAt() { return holdExpiredAt; }
    public void setHoldExpiredAt(LocalDateTime holdExpiredAt) { this.holdExpiredAt = holdExpiredAt; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

	public boolean isReviewed() { return reviewed; }
	public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    
    
}

