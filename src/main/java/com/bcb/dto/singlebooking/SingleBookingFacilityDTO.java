package com.bcb.dto.singlebooking;

/**
 * DTO for facility info inside the booking matrix response.
 *
 * @author AnhTN
 */
public class SingleBookingFacilityDTO {

    private Integer facilityId;
    private String name;
    private String openTime;
    private String closeTime;

    public SingleBookingFacilityDTO() {}

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
}

