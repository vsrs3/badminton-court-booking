package com.bcb.dto.recurring;

/**
 * Weekly recurring pattern configuration from client.
 *
 * @author AnhTN
 */
public class RecurringPatternDTO {

    private Integer dayOfWeek; // 1=Sun ... 7=Sat
    private Integer courtId;
    private String startTime; // HH:mm
    private String endTime;   // HH:mm

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getCourtId() {
        return courtId;
    }

    public void setCourtId(Integer courtId) {
        this.courtId = courtId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

