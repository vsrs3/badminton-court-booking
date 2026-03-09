package com.bcb.model;

public class FacilityInventory {

    private int facilityInventoryId;
    private int facilityId;
    private int inventoryId;
    private int totalQuantity;
    private int availableQuantity;

    // field dùng khi join để hiển thị ra JSP
    private String facilityName;
    private String inventoryName;

    public FacilityInventory() {
    }

    public FacilityInventory(int facilityInventoryId,
                             int facilityId,
                             int inventoryId,
                             int totalQuantity,
                             int availableQuantity,
                             String facilityName,
                             String inventoryName) {
        this.facilityInventoryId = facilityInventoryId;
        this.facilityId = facilityId;
        this.inventoryId = inventoryId;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.facilityName = facilityName;
        this.inventoryName = inventoryName;
    }

    public int getFacilityInventoryId() {
        return facilityInventoryId;
    }

    public void setFacilityInventoryId(int facilityInventoryId) {
        this.facilityInventoryId = facilityInventoryId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
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

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    @Override
    public String toString() {
        return "FacilityInventory{" +
                "facilityInventoryId=" + facilityInventoryId +
                ", facilityId=" + facilityId +
                ", inventoryId=" + inventoryId +
                ", totalQuantity=" + totalQuantity +
                ", availableQuantity=" + availableQuantity +
                ", facilityName='" + facilityName + '\'' +
                ", inventoryName='" + inventoryName + '\'' +
                '}';
    }
}