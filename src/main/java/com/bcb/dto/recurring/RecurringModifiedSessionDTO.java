package com.bcb.dto.recurring;

/**
 * One-time override for a conflicted recurring session.
 *
 * @author AnhTN
 */
public class RecurringModifiedSessionDTO {

    private String sessionId;
    private Integer newCourtId;
    private String newStartTime;
    private String newEndTime;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getNewCourtId() {
        return newCourtId;
    }

    public void setNewCourtId(Integer newCourtId) {
        this.newCourtId = newCourtId;
    }

    public String getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(String newStartTime) {
        this.newStartTime = newStartTime;
    }

    public String getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(String newEndTime) {
        this.newEndTime = newEndTime;
    }
}

