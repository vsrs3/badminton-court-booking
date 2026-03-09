package com.bcb.dto.voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for VoucherUsage row in usage history table.
 * Contains denormalized user, booking, invoice info for display.
 *
 * @author AnhTN
 */
public class VoucherUsageDTO {

    private Integer usageId;
    private Integer voucherId;
    private String voucherCode;

    private Integer accountId;
    private String accountName;
    private String accountEmail;

    private Integer bookingId;
    private Integer invoiceId;

    private BigDecimal discountAmount;
    private LocalDateTime usedAt;

    public VoucherUsageDTO() {}

    public Integer getUsageId() { return usageId; }
    public void setUsageId(Integer usageId) { this.usageId = usageId; }

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
