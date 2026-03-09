package com.bcb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing VoucherUsage table.
 * Records each time a voucher is applied to a booking/invoice.
 *
 * @author AnhTN
 */
public class VoucherUsage {

    private Integer usageId;
    private Integer voucherId;
    private Integer accountId;
    private Integer bookingId;
    private Integer invoiceId;
    private BigDecimal discountAmount;
    private LocalDateTime usedAt;

    public VoucherUsage() {}

    public Integer getUsageId() { return usageId; }
    public void setUsageId(Integer usageId) { this.usageId = usageId; }

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
