package com.bcb.dto.mybooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed booking view (View Booking Details).
 *
 * @author AnhTN
 */
public class MyBookingDetailDTO {
    private int bookingId;

    // Facility info
    private String facilityName;
    private String fullAddress;
    private String thumbnailPath;

    // Booking info
    private LocalDate bookingDate;
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    private String recurringPatternDetails;
    private String bookingStatus;
    private String bookingType;         // SINGLE or RECURRING
    private LocalDateTime createdAt;
    private String createdAtDisplay;
    private LocalDateTime holdExpiredAt;

    // Slot details — raw individual slots (for price breakdown)
    private List<BookingSlotDetailDTO> slots;

    // Merged slots — consecutive slots on the same court merged into one block (for display)
    private List<MergedSlotDTO> mergedSlots;
    private List<MergedSlotDTO> recurringSessions;

    // Payment info
    private String paymentStatus;       // UNPAID, PARTIAL, PAID
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;

    // Computed
    private double totalHours;          // sum of slot durations

    // Staff contact (from facility staff)
    private String staffPhone;
    private String staffName;

    public MyBookingDetailDTO() {}

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDate getRecurringStartDate() { return recurringStartDate; }
    public void setRecurringStartDate(LocalDate recurringStartDate) { this.recurringStartDate = recurringStartDate; }

    public LocalDate getRecurringEndDate() { return recurringEndDate; }
    public void setRecurringEndDate(LocalDate recurringEndDate) { this.recurringEndDate = recurringEndDate; }

    public String getRecurringPatternDetails() { return recurringPatternDetails; }
    public void setRecurringPatternDetails(String recurringPatternDetails) { this.recurringPatternDetails = recurringPatternDetails; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedAtDisplay() { return createdAtDisplay; }
    public void setCreatedAtDisplay(String createdAtDisplay) { this.createdAtDisplay = createdAtDisplay; }

    public LocalDateTime getHoldExpiredAt() { return holdExpiredAt; }
    public void setHoldExpiredAt(LocalDateTime holdExpiredAt) { this.holdExpiredAt = holdExpiredAt; }

    public List<BookingSlotDetailDTO> getSlots() { return slots; }
    public void setSlots(List<BookingSlotDetailDTO> slots) { this.slots = slots; }

    public List<MergedSlotDTO> getMergedSlots() { return mergedSlots; }
    public void setMergedSlots(List<MergedSlotDTO> mergedSlots) { this.mergedSlots = mergedSlots; }

    public List<MergedSlotDTO> getRecurringSessions() { return recurringSessions; }
    public void setRecurringSessions(List<MergedSlotDTO> recurringSessions) { this.recurringSessions = recurringSessions; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public String getStaffPhone() { return staffPhone; }
    public void setStaffPhone(String staffPhone) { this.staffPhone = staffPhone; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}

