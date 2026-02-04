package com.bcb.dto;

public class CourtViewDTO {

    private int courtId;
    private int facilityId;
    private String courtName;

    private int courtTypeId;
    private String courtTypeCode;   // NORMAL | VIP

    public CourtViewDTO() {
    }

    public CourtViewDTO(
            int courtId,
            int facilityId,
            String courtName,
            int courtTypeId,
            String courtTypeCode) {

        this.courtId = courtId;
        this.facilityId = facilityId;
        this.courtName = courtName;
        this.courtTypeId = courtTypeId;
        this.courtTypeCode = courtTypeCode;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public int getCourtTypeId() {
        return courtTypeId;
    }

    public void setCourtTypeId(int courtTypeId) {
        this.courtTypeId = courtTypeId;
    }

    public String getCourtTypeCode() {
        return courtTypeCode;
    }

    public void setCourtTypeCode(String courtTypeCode) {
        this.courtTypeCode = courtTypeCode;
    }

}