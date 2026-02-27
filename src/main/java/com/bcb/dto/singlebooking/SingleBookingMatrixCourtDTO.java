package com.bcb.dto.singlebooking;

/**
 * DTO for a court in the booking matrix.
 *
 * @author AnhTN
 */
public class SingleBookingMatrixCourtDTO {

    private Integer courtId;
    private String courtName;
    private Integer courtTypeId;

    public SingleBookingMatrixCourtDTO() {}

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public Integer getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(Integer courtTypeId) { this.courtTypeId = courtTypeId; }
}
