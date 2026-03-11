package com.bcb.dto.singlebooking;

import java.util.List;

/**
 * Request DTO for the preview endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingPreviewRequestDTO {

    private Integer facilityId;
    private String bookingDate;
    private List<SingleBookingSelectionItemDTO> selections;

    public SingleBookingPreviewRequestDTO() {}

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public List<SingleBookingSelectionItemDTO> getSelections() { return selections; }
    public void setSelections(List<SingleBookingSelectionItemDTO> selections) { this.selections = selections; }
}

