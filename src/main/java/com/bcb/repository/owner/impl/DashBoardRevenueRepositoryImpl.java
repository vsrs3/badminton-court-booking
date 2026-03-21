package com.bcb.repository.owner.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bcb.dto.owner.OwnerRevenueCardDTO;
import com.bcb.repository.owner.DashBoardRevenueRepository;
import com.bcb.utils.DBContext;

/**
 * REVENUE LOGIC — Net revenue (đã trừ refund).
 *
 * CTE RevenueByDate gồm 2 phần UNION ALL:
 *
 *   Phần 1 — Thu tiền (Payment SUCCESS):
 *     revenue = +Payment.paid_amount
 *     date    =  Payment.payment_time
 *
 *   Phần 2 — Hoàn tiền (Invoice REFUNDED):
 *     revenue = -Invoice.refund_due
 *     date    =  Invoice.created_at   ← đơn giản, không cần GROUP BY,
 *                                        không cần JOIN thêm bảng Payment
 */
public class DashBoardRevenueRepositoryImpl implements DashBoardRevenueRepository {

    private static final String CTE_TEMPLATE =
        "WITH RevenueByDate AS ( "

        // ── Phần 1: Cộng tiền đã thu ────────────────────────────────────
        + "  SELECT "
        + "    CAST(p.payment_time AS DATE) AS rev_date, "
        + "    p.paid_amount                AS revenue "
        + "  FROM Payment p "
        + "  WHERE p.payment_status = 'SUCCESS' "
        + "    AND p.payment_time   IS NOT NULL "
        + "    AND {DATE_RANGE_PAYMENT} "

        + "  UNION ALL "

        // ── Phần 2: Trừ tiền đã hoàn ────────────────────────────────────
        // Dùng Invoice.created_at làm ngày refund vì không có refunded_at.
        // Không cần JOIN Payment, không cần GROUP BY.
        + "  SELECT "
        + "    CAST(i.created_at AS DATE) AS rev_date, "
        + "    -i.refund_due              AS revenue "
        + "  FROM Invoice i "
        + "  WHERE i.refund_status = 'REFUNDED' "
        + "    AND i.refund_due    > 0 "
        + "    AND {DATE_RANGE_REFUND} "
        + ") ";

    private String buildCte(String dateRangePayment, String dateRangeRefund) {
        return CTE_TEMPLATE
                .replace("{DATE_RANGE_PAYMENT}", dateRangePayment)
                .replace("{DATE_RANGE_REFUND}",  dateRangeRefund);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Hằng số tuần — Monday-based (SQL Server: CN=1, T2=2 ... T7=7)
    // ─────────────────────────────────────────────────────────────────────
    private static final String DAYS_TO_MON = "((DATEPART(WEEKDAY, GETDATE()) + 5) % 7)";
    private static final String THIS_MON    = "CAST(DATEADD(DAY, -" + DAYS_TO_MON + ",     GETDATE()) AS DATE)";
    private static final String LAST_MON    = "CAST(DATEADD(DAY, -" + DAYS_TO_MON + " - 7, GETDATE()) AS DATE)";
    private static final String LAST_SUN    = "CAST(DATEADD(DAY, -" + DAYS_TO_MON + " - 1, GETDATE()) AS DATE)";

    // =====================================================================
    // DAILY
    // =====================================================================
    @Override
    public OwnerRevenueCardDTO getDailyRevenue() {

        final String rangeStart = "CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)";

        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + rangeStart,
                "CAST(i.created_at   AS DATE) >= " + rangeStart
        )
            + "SELECT "
            + "  ISNULL(SUM(CASE WHEN rev_date = CAST(GETDATE()                  AS DATE) THEN revenue END), 0.0) AS this_day, "
            + "  ISNULL(SUM(CASE WHEN rev_date = CAST(DATEADD(DAY,-1,GETDATE()) AS DATE) THEN revenue END), 0.0) AS last_day "
            + "FROM RevenueByDate";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OwnerRevenueCardDTO(
                        rs.getBigDecimal("this_day"),
                        rs.getBigDecimal("last_day"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi SQL Daily Revenue: " + e.getMessage(), e);
        }
        return new OwnerRevenueCardDTO();
    }

    // =====================================================================
    // WEEKLY
    // =====================================================================
    @Override
    public OwnerRevenueCardDTO getWeeklyRevenue() {

        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + LAST_MON,
                "CAST(i.created_at   AS DATE) >= " + LAST_MON
        )
            + "SELECT "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + THIS_MON + " AND rev_date <= CAST(GETDATE() AS DATE) "
            + "    THEN revenue END), 0.0) AS this_week, "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + LAST_MON + " AND rev_date <= " + LAST_SUN
            + "    THEN revenue END), 0.0) AS last_week "
            + "FROM RevenueByDate";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OwnerRevenueCardDTO(
                        rs.getBigDecimal("this_week"),
                        rs.getBigDecimal("last_week"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi SQL Weekly Revenue: " + e.getMessage(), e);
        }
        return new OwnerRevenueCardDTO();
    }

    // =====================================================================
    // MONTHLY
    // =====================================================================
    @Override
    public OwnerRevenueCardDTO getMontlyRevenue() {

        final String startLastMonth =
                "CAST(DATEFROMPARTS(YEAR(DATEADD(MONTH,-1,GETDATE())), MONTH(DATEADD(MONTH,-1,GETDATE())), 1) AS DATE)";
        final String startThisMonth =
                "CAST(DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) AS DATE)";
        final String endLastMonth   =
                "CAST(EOMONTH(DATEADD(MONTH,-1,GETDATE())) AS DATE)";

        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + startLastMonth,
                "CAST(i.created_at   AS DATE) >= " + startLastMonth
        )
            + "SELECT "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + startThisMonth + " AND rev_date <= CAST(GETDATE() AS DATE) "
            + "    THEN revenue END), 0.0) AS this_month, "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + startLastMonth + " AND rev_date <= " + endLastMonth
            + "    THEN revenue END), 0.0) AS last_month "
            + "FROM RevenueByDate";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OwnerRevenueCardDTO(
                        rs.getBigDecimal("this_month"),
                        rs.getBigDecimal("last_month"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi SQL Monthly Revenue: " + e.getMessage(), e);
        }
        return new OwnerRevenueCardDTO();
    }

    // =====================================================================
    // YEARLY
    // =====================================================================
    @Override
    public OwnerRevenueCardDTO getYearlyRevenue() {

        final String startLastYear =
                "CAST(DATEFROMPARTS(YEAR(GETDATE())-1,  1,  1) AS DATE)";
        final String startThisYear =
                "CAST(DATEFROMPARTS(YEAR(GETDATE()),    1,  1) AS DATE)";
        final String endLastYear   =
                "CAST(DATEFROMPARTS(YEAR(GETDATE())-1, 12, 31) AS DATE)";

        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + startLastYear,
                "CAST(i.created_at   AS DATE) >= " + startLastYear
        )
            + "SELECT "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + startThisYear + " AND rev_date <= CAST(GETDATE() AS DATE) "
            + "    THEN revenue END), 0.0) AS this_year, "
            + "  ISNULL(SUM(CASE "
            + "    WHEN rev_date >= " + startLastYear + " AND rev_date <= " + endLastYear
            + "    THEN revenue END), 0.0) AS last_year "
            + "FROM RevenueByDate";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OwnerRevenueCardDTO(
                        rs.getBigDecimal("this_year"),
                        rs.getBigDecimal("last_year"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi SQL Yearly Revenue: " + e.getMessage(), e);
        }
        return new OwnerRevenueCardDTO();
    }
}