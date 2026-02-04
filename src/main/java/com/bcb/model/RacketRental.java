package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RacketRental {
    private Integer racketRentalId;
    private Integer bookingSlotID;
    private Integer inventoryId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String addedBy;
    private LocalDateTime createdAt;

    public RacketRental() {}

    public Integer getRacketRentalId() { return racketRentalId; }
    public void setRacketRentalId(Integer racketRentalId) { this.racketRentalId = racketRentalId; }
    public Integer getBookingSlotID() { return bookingSlotID; }
    public void setBookingSlotID(Integer bookingSlotID) { this.bookingSlotID = bookingSlotID; }
    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}