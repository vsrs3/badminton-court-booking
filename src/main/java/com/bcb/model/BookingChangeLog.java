package com.bcb.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingChangeLog {
    private Integer changeId;
    private Integer bookingId;
    private Integer oldCourtId;
    private Integer newCourtId;
    private Integer oldSlotId;
    private Integer newSlotId;
    private LocalDate oldBookingDate;
    private LocalDate newBookingDate;
    private String changeType;
    private LocalDateTime changeTime;
    private String note;

    public BookingChangeLog() {}

    public Integer getChangeId() { return changeId; }
    public void setChangeId(Integer changeId) { this.changeId = changeId; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public Integer getOldCourtId() { return oldCourtId; }
    public void setOldCourtId(Integer oldCourtId) { this.oldCourtId = oldCourtId; }
    public Integer getNewCourtId() { return newCourtId; }
    public void setNewCourtId(Integer newCourtId) { this.newCourtId = newCourtId; }
    public Integer getOldSlotId() { return oldSlotId; }
    public void setOldSlotId(Integer oldSlotId) { this.oldSlotId = oldSlotId; }
    public Integer getNewSlotId() { return newSlotId; }
    public void setNewSlotId(Integer newSlotId) { this.newSlotId = newSlotId; }
    public LocalDate getOldBookingDate() { return oldBookingDate; }
    public void setOldBookingDate(LocalDate oldBookingDate) { this.oldBookingDate = oldBookingDate; }
    public LocalDate getNewBookingDate() { return newBookingDate; }
    public void setNewBookingDate(LocalDate newBookingDate) { this.newBookingDate = newBookingDate; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public LocalDateTime getChangeTime() { return changeTime; }
    public void setChangeTime(LocalDateTime changeTime) { this.changeTime = changeTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}