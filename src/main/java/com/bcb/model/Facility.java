package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Entity representing Facility table (Pure DB mapping)
 * NO computed fields, NO business logic
 */
public class Facility {

    // ============================================
    // DATABASE FIELDS ONLY
    // ============================================

    private Integer facilityId;
    private String name;

    // Location
    private String province;
    private String district;
    private String ward;
    private String address;

    // Coordinates
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Details
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;

    private Boolean isActive;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Facility() {
    }

    public Facility(Integer facilityId, String name, String province, String district,
                    String ward, String address, BigDecimal latitude, BigDecimal longitude,
                    String description, LocalTime openTime, LocalTime closeTime, Boolean isActive) {
        this.facilityId = facilityId;
        this.name = name;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isActive = isActive;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
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

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Get full location address (helper method, not a field)
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) sb.append(address);
        if (ward != null && !ward.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (province != null && !province.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Facility{" +
                "facilityId=" + facilityId +
                ", name='" + name + '\'' +
                ", province='" + province + '\'' +
                ", district='" + district + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}