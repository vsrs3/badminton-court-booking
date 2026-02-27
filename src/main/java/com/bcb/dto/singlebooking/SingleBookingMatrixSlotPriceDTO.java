package com.bcb.dto.singlebooking;

import java.math.BigDecimal;

/**
 * DTO for a single slot price in the booking matrix.
 *
 * @author AnhTN
 */
public class SingleBookingMatrixSlotPriceDTO {

    private Integer courtId;
    private Integer slotId;
    private BigDecimal price;

    public SingleBookingMatrixSlotPriceDTO() {}

    public Integer getCourtId() { return courtId; }
    public void setCourtId(Integer courtId) { this.courtId = courtId; }

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
