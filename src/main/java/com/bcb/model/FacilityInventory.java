package com.bcb.model;

public class FacilityInventory {
    private Integer facilityInventoryId;
    private Integer facilityId;
    private Integer inventoryId;
    private Integer totalQuantity;
    private Integer availableQuantity;

    public FacilityInventory() {}

    public Integer getFacilityInventoryId() { return facilityInventoryId; }
    public void setFacilityInventoryId(Integer facilityInventoryId) { this.facilityInventoryId = facilityInventoryId; }
    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }
    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
}