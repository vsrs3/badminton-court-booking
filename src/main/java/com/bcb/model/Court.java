package com.bcb.model;

public class Court {
    private int courtId;
    private int facilityId;
    private int courtTypeId;
    private String courtName;
    private boolean isActive;

    public Court() {}

    public Court(int courtId, int facilityId, int courtTypeId, String courtName, boolean isActive) {
        this.courtId = courtId;
        this.facilityId = facilityId;
        this.courtTypeId = courtTypeId;
        this.courtName = courtName;
        this.isActive = isActive;
    }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }
    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }
    public int getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(int courtTypeId) { this.courtTypeId = courtTypeId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Court{" + "courtId=" + courtId + ", facilityId=" + facilityId +
               ", courtName='" + courtName + '\'' + ", isActive=" + isActive + '}';
    }
}
