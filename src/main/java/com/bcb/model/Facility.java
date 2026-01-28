package com.bcb.model;

import java.time.LocalTime;

/**
 * Facility (Location) entity model.
 * Represents a badminton court facility/venue.
 */
public class Facility {
    private int facilityId;
    private String name;
    private String province;
    private String district;
    private String ward;
    private String address;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isActive;

    // Empty constructor
    public Facility() {
    }

    // Full constructor
    public Facility(int facilityId, String name, String province, String district, String ward,
                    String address, String description, LocalTime openTime, LocalTime closeTime,
                    boolean isActive) {
        this.facilityId = facilityId;
        this.name = name;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isActive = isActive;
    }

    // Getters and setters
    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Facility{" +
                "facilityId=" + facilityId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                ", isActive=" + isActive +
                '}';
    }
}
