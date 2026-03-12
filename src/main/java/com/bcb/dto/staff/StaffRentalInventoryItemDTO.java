package com.bcb.dto.staff;

import java.math.BigDecimal;

public class StaffRentalInventoryItemDTO {
    private int facilityInventoryId;
    private int inventoryId;
    private String name;
    private String brand;
    private String description;
    private BigDecimal rentalPrice;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}