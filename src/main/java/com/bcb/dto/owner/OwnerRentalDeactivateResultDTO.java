package com.bcb.dto.owner;

public class OwnerRentalDeactivateResultDTO {

    private int facilityId;
    private int month;
    private int year;
    private int deactivatedCount;

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDeactivatedCount() {
        return deactivatedCount;
    }

    public void setDeactivatedCount(int deactivatedCount) {
        this.deactivatedCount = deactivatedCount;
    }
}
