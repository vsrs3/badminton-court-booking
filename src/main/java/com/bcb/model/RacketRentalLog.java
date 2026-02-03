package com.bcb.model;

import java.time.LocalDateTime;

public class RacketRentalLog {
    private Integer rentalId;
    private Integer bookingId;
    private Integer facilityInventoryId;
    private Integer quantity;
    private Integer staffId;
    private LocalDateTime rentedAt;
    private LocalDateTime returnedAt;

    public RacketRentalLog() {}

    public Integer getRentalId() { return rentalId; }
    public void setRentalId(Integer rentalId) { this.rentalId = rentalId; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public Integer getFacilityInventoryId() { return facilityInventoryId; }
    public void setFacilityInventoryId(Integer facilityInventoryId) { this.facilityInventoryId = facilityInventoryId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public LocalDateTime getRentedAt() { return rentedAt; }
    public void setRentedAt(LocalDateTime rentedAt) { this.rentedAt = rentedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}