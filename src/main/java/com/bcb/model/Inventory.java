package com.bcb.model;

import java.math.BigDecimal;

public class Inventory {
    private int inventoryId;
    private String name;
    private String brand;
    private String description;
    private BigDecimal rentalPrice;
    private boolean isActive;
    private Integer facilityId;
    private String facilityName;
    
    public Inventory() {}
    
    public Inventory(int inventoryId, String name, String brand,
            String description, BigDecimal rentalPrice,
            boolean isActive, Integer facilityId) {

    this.inventoryId = inventoryId;
    this.name = name;
    this.brand = brand;
    this.description = description;
    this.rentalPrice = rentalPrice;
    this.isActive = isActive;
    this.facilityId = facilityId;
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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Integer getFacilityId() {
	    return facilityId;
	}

	public void setFacilityId(Integer facilityId) {
	    this.facilityId = facilityId;
	}

	public String getFacilityName() {
	    return facilityName;
	}

	public void setFacilityName(String facilityName) {
	    this.facilityName = facilityName;
	}
	
}