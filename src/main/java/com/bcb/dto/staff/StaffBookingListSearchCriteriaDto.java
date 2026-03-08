package com.bcb.dto.staff;

public class StaffBookingListSearchCriteriaDto {
    private int facilityId;
    private String search;
    private boolean hasSearch;
    private boolean numericSearch;
    private String likePattern;

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
}

