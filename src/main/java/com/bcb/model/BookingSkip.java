package com.bcb.model;

import java.time.LocalDate;

public class BookingSkip {
    private Integer skipId;
    private Integer recurringId;
    private LocalDate skipDate;
    private String reason;

    public BookingSkip() {}

    public Integer getSkipId() { return skipId; }
    public void setSkipId(Integer skipId) { this.skipId = skipId; }
    public Integer getRecurringId() { return recurringId; }
    public void setRecurringId(Integer recurringId) { this.recurringId = recurringId; }
    public LocalDate getSkipDate() { return skipDate; }
    public void setSkipDate(LocalDate skipDate) { this.skipDate = skipDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}