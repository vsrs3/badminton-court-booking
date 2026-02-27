package com.bcb.dto.singlebooking;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the preview endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingPreviewResponseDTO {

    private Integer totalSlots;
    private Integer totalMinutes;
    private BigDecimal estimatedTotal;
    private List<SingleBookingRangeDTO> rangesByCourt;

    public SingleBookingPreviewResponseDTO() {}

    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }

    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public List<SingleBookingRangeDTO> getRangesByCourt() { return rangesByCourt; }
    public void setRangesByCourt(List<SingleBookingRangeDTO> rangesByCourt) { this.rangesByCourt = rangesByCourt; }
}
