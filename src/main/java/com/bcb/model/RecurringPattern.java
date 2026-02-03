package com.bcb.model;

public class RecurringPattern {
    private Integer patternId;
    private Integer recurringId;
    private Integer dayOfWeek;
    private Integer slotId;

    public RecurringPattern() {}

    public Integer getPatternId() { return patternId; }
    public void setPatternId(Integer patternId) { this.patternId = patternId; }
    public Integer getRecurringId() { return recurringId; }
    public void setRecurringId(Integer recurringId) { this.recurringId = recurringId; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
}