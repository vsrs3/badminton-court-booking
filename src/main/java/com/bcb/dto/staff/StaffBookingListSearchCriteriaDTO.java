package com.bcb.dto.staff;

public class StaffBookingListSearchCriteriaDTO {
    private int facilityId;
    private String search;
    private boolean hasSearch;
    private boolean numericSearch;
    private String likePattern;
    private String status;
    private boolean todayOnly;
    private java.sql.Date todayDate;

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isHasSearch() {
        return hasSearch;
    }

    public void setHasSearch(boolean hasSearch) {
        this.hasSearch = hasSearch;
    }

    public boolean isNumericSearch() {
        return numericSearch;
    }

    public void setNumericSearch(boolean numericSearch) {
        this.numericSearch = numericSearch;
    }

    public String getLikePattern() {
        return likePattern;
    }

    public void setLikePattern(String likePattern) {
        this.likePattern = likePattern;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isTodayOnly() {
        return todayOnly;
    }

    public void setTodayOnly(boolean todayOnly) {
        this.todayOnly = todayOnly;
    }

    public java.sql.Date getTodayDate() {
        return todayDate;
    }

    public void setTodayDate(java.sql.Date todayDate) {
        this.todayDate = todayDate;
    }
}

