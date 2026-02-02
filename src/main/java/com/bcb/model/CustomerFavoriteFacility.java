package com.bcb.model;

public class CustomerFavoriteFacility {
    private Integer favoriteId;
    private Integer accountId;
    private Integer facilityId;

    public CustomerFavoriteFacility() {}

    public Integer getFavoriteId() { return favoriteId; }
    public void setFavoriteId(Integer favoriteId) { this.favoriteId = favoriteId; }
    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }
}