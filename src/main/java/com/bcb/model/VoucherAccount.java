package com.bcb.model;

/**
 * Entity representing VoucherAccount join table.
 * Defines which accounts a voucher is restricted to.
 * If no entries exist for a voucher, it applies to ALL users.
 *
 * @author AnhTN
 */
public class VoucherAccount {

    private Integer voucherId;
    private Integer accountId;

    public VoucherAccount() {}

    public VoucherAccount(Integer voucherId, Integer accountId) {
        this.voucherId = voucherId;
        this.accountId = accountId;
    }

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }
}
