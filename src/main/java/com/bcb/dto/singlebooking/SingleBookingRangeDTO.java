package com.bcb.dto.singlebooking;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing a contiguous time range for a court in the preview.
 *
 * @author AnhTN
 */
public class SingleBookingRangeDTO {

    private Integer courtId;
    private String courtName;
    private String startTime;
    private String endTime;
    private Integer slotCount;
    private Integer minutes;
    private BigDecimal subtotal;
    private List<SingleBookingMatrixSlotPriceDTO> slotPrices;

    public SingleBookingRangeDTO() {}

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getSlotCount() { return slotCount; }
    public void setSlotCount(Integer slotCount) { this.slotCount = slotCount; }

    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public List<SingleBookingMatrixSlotPriceDTO> getSlotPrices() { return slotPrices; }
    public void setSlotPrices(List<SingleBookingMatrixSlotPriceDTO> slotPrices) { this.slotPrices = slotPrices; }
}
