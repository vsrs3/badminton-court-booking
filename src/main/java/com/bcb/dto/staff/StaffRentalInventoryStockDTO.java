package com.bcb.dto.staff;

public class StaffRentalInventoryStockDTO {
    private int facilityInventoryId;
    private int inventoryId;
    private String inventoryName;
    private int totalQuantity;
    private int availableQuantity;

    public int getFacilityInventoryId() {
        return facilityInventoryId;
    }

    public void setFacilityInventoryId(int facilityInventoryId) {
        this.facilityInventoryId = facilityInventoryId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
