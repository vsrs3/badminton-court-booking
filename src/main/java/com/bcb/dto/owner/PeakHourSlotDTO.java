package com.bcb.dto.owner;

import java.math.BigDecimal;

public class PeakHourSlotDTO {

    private int        dayOfWeek;
    private String     slotTime;
    private int        bookingCount;
    private BigDecimal occupancyPct;
    private String     slotType;   // PEAK | LOW | NORMAL | NO_DATA

    public PeakHourSlotDTO() {}

    public PeakHourSlotDTO(int dayOfWeek, String slotTime,
                            int bookingCount, BigDecimal occupancyPct,
                            String slotType) {
        this.dayOfWeek    = dayOfWeek;
        this.slotTime     = slotTime;
        this.bookingCount = bookingCount;
        this.occupancyPct = occupancyPct != null ? occupancyPct : BigDecimal.ZERO;
        this.slotType     = slotType;
    }

    public int        getDayOfWeek()    { return dayOfWeek; }
    public String     getSlotTime()     { return slotTime; }
    public int        getBookingCount() { return bookingCount; }
    public BigDecimal getOccupancyPct() { return occupancyPct; }
    public String     getSlotType()     { return slotType; }

    public void setDayOfWeek(int v)        { this.dayOfWeek    = v; }
    public void setSlotTime(String v)      { this.slotTime     = v; }
    public void setBookingCount(int v)     { this.bookingCount = v; }
    public void setOccupancyPct(BigDecimal v) { this.occupancyPct = v; }
    public void setSlotType(String v)      { this.slotType     = v; }
}