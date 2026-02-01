package com.bcb.model;

public class CourtType {
    private int courtTypeId;
    private String typeCode;
    private String description;

    public CourtType() {}

    public CourtType(int courtTypeId, String typeCode, String description) {
        this.courtTypeId = courtTypeId;
        this.typeCode = typeCode;
        this.description = description;
    }

    public int getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(int courtTypeId) { this.courtTypeId = courtTypeId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
