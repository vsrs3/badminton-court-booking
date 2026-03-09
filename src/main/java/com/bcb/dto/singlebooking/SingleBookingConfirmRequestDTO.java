package com.bcb.dto.singlebooking;

import java.util.List;

/**
 * Request DTO for the confirm-and-pay endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingConfirmRequestDTO {

    private Integer facilityId;
    private String bookingDate;
    private Integer depositPercent;
    private List<SingleBookingSelectionItemDTO> selections;

    public SingleBookingConfirmRequestDTO() {}

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public Integer getDepositPercent() { return depositPercent; }
    public void setDepositPercent(Integer depositPercent) { this.depositPercent = depositPercent; }

    public List<SingleBookingSelectionItemDTO> getSelections() { return selections; }
    public void setSelections(List<SingleBookingSelectionItemDTO> selections) { this.selections = selections; }
}

