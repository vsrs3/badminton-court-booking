package com.bcb.dto.singlebooking;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the preview endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingPreviewResponseDTO {

    private Integer facilityId;
    private String facilityName;
    private String facilityAddress;
    private String bookingDate;
    private Integer totalSlots;
    private Integer totalMinutes;
    private BigDecimal estimatedTotal;
    private List<SingleBookingRangeDTO> rangesByCourt;

    public SingleBookingPreviewResponseDTO() {}

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFacilityAddress() { return facilityAddress; }
    public void setFacilityAddress(String facilityAddress) { this.facilityAddress = facilityAddress; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }

    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public List<SingleBookingRangeDTO> getRangesByCourt() { return rangesByCourt; }
    public void setRangesByCourt(List<SingleBookingRangeDTO> rangesByCourt) { this.rangesByCourt = rangesByCourt; }
}
