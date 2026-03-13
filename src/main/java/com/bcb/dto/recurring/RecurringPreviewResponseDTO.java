package com.bcb.dto.recurring;

import java.math.BigDecimal;
import java.util.List;

/**
 * Preview response payload for recurring booking.
 *
 * @author AnhTN
 */
public class RecurringPreviewResponseDTO {

    private String previewToken;
    private Integer totalSessions;
    private Integer availableSessions;
    private Integer conflictSessions;
    private BigDecimal totalAmount;
    private List<RecurringPreviewSessionDTO> sessions;

    public String getPreviewToken() {
        return previewToken;
    }

    public void setPreviewToken(String previewToken) {
        this.previewToken = previewToken;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public Integer getAvailableSessions() {
        return availableSessions;
    }

    public void setAvailableSessions(Integer availableSessions) {
        this.availableSessions = availableSessions;
    }

    public Integer getConflictSessions() {
        return conflictSessions;
    }

    public void setConflictSessions(Integer conflictSessions) {
        this.conflictSessions = conflictSessions;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<RecurringPreviewSessionDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<RecurringPreviewSessionDTO> sessions) {
        this.sessions = sessions;
    }
}

