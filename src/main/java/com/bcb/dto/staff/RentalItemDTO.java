package com.bcb.dto.staff;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalItemDTO {
    private Integer racketRentalId;
    private Integer bookingSlotId;
    private String addedBy;
    private LocalDateTime createdAt;
    private String groupKey;
    private Integer courtId;
    private String name;
    private String startTime;
    private String endTime;
    private List<Integer> slotIds = new ArrayList<>();
    private Integer inventoryId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public Integer getRacketRentalId() {
        return racketRentalId;
    }

    public void setRacketRentalId(Integer racketRentalId) {
        this.racketRentalId = racketRentalId;
    }

    public Integer getBookingSlotId() {
        return bookingSlotId;
    }

    public void setBookingSlotId(Integer bookingSlotId) {
        this.bookingSlotId = bookingSlotId;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public Integer getCourtId() {
        return courtId;
    }

    public void setCourtId(Integer courtId) {
        this.courtId = courtId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Integer> getSlotIds() {
        return slotIds;
    }

    public void setSlotIds(List<Integer> slotIds) {
        this.slotIds = slotIds;
    }

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
