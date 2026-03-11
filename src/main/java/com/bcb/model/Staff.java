package com.bcb.model;

public class Staff {
	private Integer staffId;
	private Integer accountId;
	private Integer facilityId;
	private boolean isActive;
	
	public Staff(Integer staffId, Integer accountId, Integer facilityId, boolean isActive) {
		super();
		this.staffId = staffId;
		this.accountId = accountId;
		this.facilityId = facilityId;
		this.isActive = isActive;
	}
	
	public Staff() {
		super();
	}

	public Integer getStaffId() {
		return staffId;
	}

	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Integer facilityId) {
		this.facilityId = facilityId;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
}

