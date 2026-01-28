package com.bcb.model;

public class Court {
    private int courtId;
    private int facilityId;
    private String courtName;
    private String description;
    private boolean isActive;

    public Court() {}

    public Court(int courtId, int facilityId, String courtName, String description, boolean isActive) {
        this.courtId = courtId;
        this.facilityId = facilityId;
        this.courtName = courtName;
        this.description = description;
        this.isActive = isActive;
    }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }
    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Court{" + "courtId=" + courtId + ", facilityId=" + facilityId +
               ", courtName='" + courtName + '\'' + ", isActive=" + isActive + '}';
    }
}
