package com.bcb.dto.voucher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for Voucher Dashboard statistics response.
 * Used by the AJAX API endpoint GET /api/owner/vouchers/dashboard.
 *
 * @author AnhTN
 */
public class VoucherDashboardDTO {

    /** Total vouchers (active + inactive) */
    private int totalVouchers;

    /** Vouchers with is_active = true AND within valid period */
    private int activeVouchers;

    /** Number of voucher uses in current month */
    private int monthlyUsageCount;

    /** Total discount amount given in current month */
    private BigDecimal monthlyDiscountTotal;

    /**
     * Line chart: daily usage count for last 30 days
     * Key = date string "yyyy-MM-dd", Value = count
     */
    private List<Map<String, Object>> dailyUsageChart;

    /**
     * Bar chart: top vouchers by usage count
     * Each entry: { code, name, usageCount }
     */
    private List<Map<String, Object>> topVouchers;

    public VoucherDashboardDTO() {}

    public int getTotalVouchers() { return totalVouchers; }
    public void setTotalVouchers(int totalVouchers) { this.totalVouchers = totalVouchers; }

    public int getActiveVouchers() { return activeVouchers; }
    public void setActiveVouchers(int activeVouchers) { this.activeVouchers = activeVouchers; }

    public int getMonthlyUsageCount() { return monthlyUsageCount; }
    public void setMonthlyUsageCount(int monthlyUsageCount) { this.monthlyUsageCount = monthlyUsageCount; }

    public BigDecimal getMonthlyDiscountTotal() { return monthlyDiscountTotal; }
    public void setMonthlyDiscountTotal(BigDecimal monthlyDiscountTotal) { this.monthlyDiscountTotal = monthlyDiscountTotal; }

    public List<Map<String, Object>> getDailyUsageChart() { return dailyUsageChart; }
    public void setDailyUsageChart(List<Map<String, Object>> dailyUsageChart) { this.dailyUsageChart = dailyUsageChart; }

    public List<Map<String, Object>> getTopVouchers() { return topVouchers; }
    public void setTopVouchers(List<Map<String, Object>> topVouchers) { this.topVouchers = topVouchers; }
}
