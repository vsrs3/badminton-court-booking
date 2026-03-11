package com.bcb.model;

/**
 * Entity representing VoucherFacility join table.
 * Defines which facilities a voucher applies to.
 * If no entries exist for a voucher, it applies to ALL facilities.
 *
 * @author AnhTN
 */
public class VoucherFacility {

    private Integer voucherId;
    private Integer facilityId;

    public VoucherFacility() {}

    public VoucherFacility(Integer voucherId, Integer facilityId) {
        this.voucherId = voucherId;
        this.facilityId = facilityId;
    }

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }
}
