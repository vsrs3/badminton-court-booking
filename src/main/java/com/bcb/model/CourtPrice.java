package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalTime;

public class CourtPrice {
    private int priceId;
    private int courtId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal pricePerHour;

    // Transient fields for formatted time values (for JSP usage)
    private transient String startTimeFormatted;
    private transient String endTimeFormatted;

    public CourtPrice() {}

    public CourtPrice(int priceId, int courtId, LocalTime startTime, LocalTime endTime, BigDecimal pricePerHour) {
        this.priceId = priceId;
        this.courtId = courtId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pricePerHour = pricePerHour;
    }

    public int getPriceId() { return priceId; }
    public void setPriceId(int priceId) { this.priceId = priceId; }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getStartTimeFormatted() { return startTimeFormatted; }
    public void setStartTimeFormatted(String startTimeFormatted) { this.startTimeFormatted = startTimeFormatted; }

    public String getEndTimeFormatted() { return endTimeFormatted; }
    public void setEndTimeFormatted(String endTimeFormatted) { this.endTimeFormatted = endTimeFormatted; }

    @Override
    public String toString() {
        return "CourtPrice{" + "priceId=" + priceId + ", courtId=" + courtId +
               ", startTime=" + startTime + ", endTime=" + endTime + ", pricePerHour=" + pricePerHour + '}';
    }
}
