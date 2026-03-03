package com.bcb.model;

import java.math.BigDecimal;

public class Inventory {
    private int inventoryId;
    private String name;
    private String brand;
    private String description;
    private BigDecimal rentalPrice;
    private boolean isActive;
    private Integer courtId;
    private String courtName;
    
    public Inventory() {}
    
    public Inventory(int inventoryId, String name, String brand,
            String description, BigDecimal rentalPrice,
            boolean isActive, Integer courtId) {
this.inventoryId = inventoryId;
this.name = name;
this.brand = brand;
this.description = description;
this.rentalPrice = rentalPrice;
this.isActive = isActive;
this.courtId = courtId;

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

	public Integer getCourtId() {
	    return courtId;
	}

	public void setCourtId(Integer courtId) {
	    this.courtId = courtId;
	}
	public String getCourtName() {
	    return courtName;
	}

	public void setCourtName(String courtName) {
	    this.courtName = courtName;
	}
	
}