package com.bcb.repository.owner.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bcb.dto.owner.OwnerRevenueChartDTO;
import com.bcb.repository.owner.DashboardRevenueChartRepository;
import com.bcb.utils.DBContext;

/**
 * CHART REVENUE LOGIC — nhất quán với DashBoardRevenueRepositoryImpl:
 *
 *   Phần 1 — Thu tiền: revenue = +Payment.paid_amount, date = Payment.payment_time
 *   Phần 2 — Hoàn tiền: revenue = -Invoice.refund_due, date = Invoice.created_at
 *
 * Không dùng Booking.booking_date, không dùng Invoice.paid_amount trực tiếp.
 *
 * CTE_BASE được tái sử dụng cho tất cả query bằng cách thêm WHERE
 * khác nhau ở bên ngoài thông qua {DATE_FILTER} và {GROUP_BY_EXPR}.
 */
public class DashboardRevenueChartRepositoryImpl implements DashboardRevenueChartRepository {

    // ─────────────────────────────────────────────────────────────────────
    // CTE dùng chung — {DATE_FILTER_PAYMENT} và {DATE_FILTER_REFUND}
    // giới hạn khoảng quét để tránh full scan.
    // ─────────────────────────────────────────────────────────────────────
    private static final String CTE_TEMPLATE =
        "WITH RevenueByDate AS ( "
        // Phần 1: Cộng tiền đã thu
        + "  SELECT CAST(p.payment_time AS DATE) AS rev_date, p.paid_amount AS revenue "
        + "  FROM Payment p "
        + "  WHERE p.payment_status = 'SUCCESS' "
        + "    AND p.payment_time IS NOT NULL "
        + "    AND {DATE_FILTER_PAYMENT} "
        + "  UNION ALL "
        // Phần 2: Trừ tiền đã hoàn
        + "  SELECT CAST(i.created_at AS DATE) AS rev_date, -i.refund_due AS revenue "
        + "  FROM Invoice i "
        + "  WHERE i.refund_status = 'REFUNDED' "
        + "    AND i.refund_due > 0 "
        + "    AND {DATE_FILTER_REFUND} "
        + ") ";

    private String buildCte(String paymentFilter, String refundFilter) {
        return CTE_TEMPLATE
                .replace("{DATE_FILTER_PAYMENT}", paymentFilter)
                .replace("{DATE_FILTER_REFUND}",  refundFilter);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Hằng số ngày — Monday-based week
    // ─────────────────────────────────────────────────────────────────────
    private static final String DAYS_TO_MON = "((DATEPART(WEEKDAY, GETDATE()) + 5) % 7)";
    private static final String THIS_MON    = "CAST(DATEADD(DAY, -"     + DAYS_TO_MON + ",     GETDATE()) AS DATE)";
    private static final String LAST_MON    = "CAST(DATEADD(DAY, -"     + DAYS_TO_MON + " - 7, GETDATE()) AS DATE)";
    private static final String LAST_SUN    = "CAST(DATEADD(DAY, -"     + DAYS_TO_MON + " - 1, GETDATE()) AS DATE)";

    // ─────────────────────────────────────────────────────────────────────
    // Helper: thực thi SQL → OwnerRevenueChartDTO
    // ─────────────────────────────────────────────────────────────────────
    private OwnerRevenueChartDTO query(String sql, String labelCol, String dataCol) {
        List<String>     labels = new ArrayList<>();
        List<BigDecimal> data   = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labels.add(rs.getString(labelCol));
                data.add(rs.getBigDecimal(dataCol));
            }
            return new OwnerRevenueChartDTO(labels, data);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn chart: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // DAILY THIS WEEK — nhóm theo ngày trong tuần hiện tại
    // Label: 3 ký tự đầu tên thứ tiếng Anh (Mon, Tue, ...)
    // =====================================================================
    @Override
    public OwnerRevenueChartDTO getDailyRevenueThisWeek() {
        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + THIS_MON
                + " AND CAST(p.payment_time AS DATE) <= CAST(GETDATE() AS DATE)",
                "CAST(i.created_at AS DATE) >= " + THIS_MON
                + " AND CAST(i.created_at AS DATE) <= CAST(GETDATE() AS DATE)"
        )
            + "SELECT "
            + "  LEFT(DATENAME(WEEKDAY, rev_date), 3) AS day_label, "
            + "  ISNULL(SUM(revenue), 0)              AS revenue "
            + "FROM RevenueByDate "
            + "GROUP BY rev_date, DATENAME(WEEKDAY, rev_date) "
            + "ORDER BY rev_date";

        return query(sql, "day_label", "revenue");
    }

    // =====================================================================
    // DAILY PREV WEEK — nhóm theo ngày trong tuần trước
    // =====================================================================
    @Override
    public OwnerRevenueChartDTO getDailyRevenuePreviousWeek() {
        String sql = buildCte(
                "CAST(p.payment_time AS DATE) >= " + LAST_MON
                + " AND CAST(p.payment_time AS DATE) <= " + LAST_SUN,
                "CAST(i.created_at AS DATE) >= " + LAST_MON
                + " AND CAST(i.created_at AS DATE) <= " + LAST_SUN
        )
            + "SELECT "
            + "  LEFT(DATENAME(WEEKDAY, rev_date), 3) AS day_label, "
            + "  ISNULL(SUM(revenue), 0)              AS revenue "
            + "FROM RevenueByDate "
            + "GROUP BY rev_date, DATENAME(WEEKDAY, rev_date) "
            + "ORDER BY rev_date";

        return query(sql, "day_label", "revenue");
    }

    // =====================================================================
    // MONTHLY THIS YEAR — nhóm theo tháng trong năm nay
    // Label: 3 ký tự đầu tên tháng tiếng Anh (Jan, Feb, ...)
    // =====================================================================
    @Override
    public OwnerRevenueChartDTO getMonthlyRevenueThisYear() {
        String sql = buildCte(
                "YEAR(p.payment_time) = YEAR(GETDATE())",
                "YEAR(i.created_at)   = YEAR(GETDATE())"
        )
            + "SELECT "
            + "  LEFT(DATENAME(MONTH, rev_date), 3) AS month_label, "
            + "  ISNULL(SUM(revenue), 0)            AS revenue "
            + "FROM RevenueByDate "
            + "GROUP BY MONTH(rev_date), DATENAME(MONTH, rev_date) "
            + "ORDER BY MONTH(rev_date)";

        return query(sql, "month_label", "revenue");
    }

    // =====================================================================
    // MONTHLY PREV YEAR — nhóm theo tháng trong năm ngoái
    // =====================================================================
    @Override
    public OwnerRevenueChartDTO getMonthlyRevenuePreviousYear() {
        String sql = buildCte(
                "YEAR(p.payment_time) = YEAR(GETDATE()) - 1",
                "YEAR(i.created_at)   = YEAR(GETDATE()) - 1"
        )
            + "SELECT "
            + "  LEFT(DATENAME(MONTH, rev_date), 3) AS month_label, "
            + "  ISNULL(SUM(revenue), 0)            AS revenue "
            + "FROM RevenueByDate "
            + "GROUP BY MONTH(rev_date), DATENAME(MONTH, rev_date) "
            + "ORDER BY MONTH(rev_date)";

        return query(sql, "month_label", "revenue");
    }
}