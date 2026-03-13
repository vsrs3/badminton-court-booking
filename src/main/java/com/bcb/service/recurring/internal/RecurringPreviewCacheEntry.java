package com.bcb.service.recurring.internal;

import com.bcb.dto.recurring.RecurringPatternDTO;
import com.bcb.dto.recurring.RecurringPreviewSessionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * In-memory preview cache object to bridge preview -> confirm.
 *
 * @author AnhTN
 */
public class RecurringPreviewCacheEntry {

    private Integer facilityId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<RecurringPatternDTO> patterns;
    private List<RecurringPreviewSessionDTO> sessions;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<RecurringPatternDTO> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<RecurringPatternDTO> patterns) {
        this.patterns = patterns;
    }

    public List<RecurringPreviewSessionDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<RecurringPreviewSessionDTO> sessions) {
        this.sessions = sessions;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

