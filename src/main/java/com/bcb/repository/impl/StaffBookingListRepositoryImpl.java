package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingListItemDTO;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDTO;
import com.bcb.repository.staff.StaffBookingListRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingListRepositoryImpl implements StaffBookingListRepository {

    /**
     * Counts bookings with optional search and filter criteria.
     */
    @Override
    public int countBookings(StaffBookingListSearchCriteriaDTO criteria) throws Exception {
        String fromJoin = " FROM Booking b " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "LEFT JOIN RecurringBooking rb ON b.recurring_id = rb.recurring_id ";
        String whereBase = "WHERE b.facility_id = ? AND b.booking_status != 'EXPIRED'";
        String whereSearch = buildWhereSearch(criteria);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*)" + fromJoin + whereBase + whereSearch)) {
            bindSearchParams(ps, criteria, 1);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Loads a paginated list of bookings with search and status filters applied.
     */
    @Override
    public List<StaffBookingListItemDTO> findBookings(StaffBookingListSearchCriteriaDTO criteria, int offset, int size)
            throws Exception {
        String fromJoin = " FROM Booking b " +
                "LEFT JOIN Account a ON b.account_id = a.account_id " +
                "LEFT JOIN Guest g ON b.guest_id = g.guest_id " +
                "LEFT JOIN RecurringBooking rb ON b.recurring_id = rb.recurring_id ";
        String whereBase = "WHERE b.facility_id = ? AND b.booking_status != 'EXPIRED'";
        String whereSearch = buildWhereSearch(criteria);

        String selectBase = """
                SELECT b.booking_id,
                       COALESCE(a.full_name, g.guest_name) AS customer_name,
                       COALESCE(a.phone, g.phone) AS phone,
                       b.booking_date, b.booking_status, i.payment_status,
                       b.recurring_id, rb.start_date AS recurring_start_date, rb.end_date AS recurring_end_date,
                       (SELECT COUNT(DISTINCT bs2.court_id)
                        FROM BookingSlot bs2
                        WHERE bs2.booking_id = b.booking_id
                          AND bs2.slot_status <> 'CANCELLED') AS court_count,
                       (SELECT TOP 1 c2.court_name
                        FROM BookingSlot bs3
                        JOIN Court c2 ON bs3.court_id = c2.court_id
                        WHERE bs3.booking_id = b.booking_id
                          AND bs3.slot_status <> 'CANCELLED'
                        ORDER BY c2.court_name) AS first_court_name,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM BookingSlot bsn
                           WHERE bsn.booking_id = b.booking_id AND bsn.slot_status = 'NO_SHOW'
                       ) THEN 1 ELSE 0 END AS has_no_show,
                       ns.next_slot_date AS next_slot_date,
                       ls.last_slot_date AS last_slot_date,
                       eff.effective_next_date AS effective_next_date,
                       eff.effective_last_date AS effective_last_date
                """;

        String joins = fromJoin +
                " LEFT JOIN Invoice i ON b.booking_id = i.booking_id ";

        String applyNext = """
                OUTER APPLY (
                    SELECT MIN(bs.booking_date) AS next_slot_date
                    FROM BookingSlot bs
                    WHERE bs.booking_id = b.booking_id
                      AND bs.slot_status <> 'CANCELLED'
                      AND bs.booking_date >= CAST(GETDATE() AS date)
                ) ns
                """;

        String applyLast = """
                OUTER APPLY (
                    SELECT MAX(bs.booking_date) AS last_slot_date
                    FROM BookingSlot bs
                    WHERE bs.booking_id = b.booking_id
                      AND bs.slot_status <> 'CANCELLED'
                ) ls
                """;

        String applyEffective = """
                OUTER APPLY (
                    SELECT
                      CASE
                        WHEN b.recurring_id IS NULL THEN
                          CASE WHEN b.booking_date >= CAST(GETDATE() AS date) THEN b.booking_date ELSE NULL END
                        ELSE ns.next_slot_date
                      END AS effective_next_date,
                      CASE
                        WHEN b.recurring_id IS NULL THEN b.booking_date
                        ELSE ls.last_slot_date
                      END AS effective_last_date
                ) eff
                """;

        /* Prioritize upcoming/active bookings before past/completed ones. */
        String orderBy = """
                 ORDER BY
                  CASE
                    WHEN b.booking_status = 'COMPLETED' THEN 3
                    WHEN b.booking_status = 'CANCELLED' THEN 4
                    WHEN eff.effective_next_date IS NOT NULL AND b.booking_status = 'CONFIRMED' THEN 0
                    WHEN eff.effective_next_date IS NOT NULL THEN 1
                    ELSE 2
                  END,
                  CASE WHEN eff.effective_next_date IS NOT NULL THEN eff.effective_next_date ELSE NULL END ASC,
                  CASE WHEN eff.effective_next_date IS NULL THEN COALESCE(eff.effective_last_date, b.created_at) ELSE NULL END DESC,
                  b.created_at DESC
                """;

        String paging = " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        String sql = selectBase + joins + applyNext + applyLast + applyEffective + whereBase + whereSearch + orderBy + paging;

        List<StaffBookingListItemDTO> results = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = bindSearchParams(ps, criteria, 1);
            ps.setInt(idx++, offset);
            ps.setInt(idx, size);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int courtCount = rs.getInt("court_count");
                    String courtDisplay = courtCount > 1
                            ? "Nhiều sân (" + courtCount + ")"
                            : rs.getString("first_court_name");

                    StaffBookingListItemDTO item = new StaffBookingListItemDTO();
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setCustomerName(rs.getString("customer_name"));
                    item.setPhone(rs.getString("phone"));
                    item.setBookingDate(rs.getString("booking_date"));
                    item.setRecurring(rs.getObject("recurring_id") != null);
                    item.setRecurringStartDate(rs.getString("recurring_start_date"));
                    item.setRecurringEndDate(rs.getString("recurring_end_date"));
                    item.setBookingStatus(rs.getString("booking_status"));
                    item.setPaymentStatus(rs.getString("payment_status"));
                    item.setCourtDisplay(courtDisplay);
                    item.setHasNoShow(rs.getInt("has_no_show") == 1);
                    results.add(item);
                }
            }
        }

        return results;
    }

    private String buildWhereSearch(StaffBookingListSearchCriteriaDTO criteria) {
        StringBuilder where = new StringBuilder();
        if (criteria.isHasSearch()) {
            if (criteria.isNumericSearch()) {
                where.append(" AND (b.booking_id = ? OR a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)");
            } else {
                where.append(" AND (a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)");
            }
        }
        if (criteria.getStatus() != null) {
            where.append(" AND b.booking_status = ?");
        }
        if (criteria.isTodayOnly()) {
            where.append(" AND (")
                    .append(" (b.recurring_id IS NULL AND b.booking_date = ?)")
                    .append(" OR (b.recurring_id IS NOT NULL AND EXISTS (")
                    .append("   SELECT 1 FROM BookingSlot bs_today")
                    .append("   WHERE bs_today.booking_id = b.booking_id")
                    .append("     AND bs_today.slot_status <> 'CANCELLED'")
                    .append("     AND bs_today.booking_date = ?")
                    .append(" ))")
                    .append(" )");
        }
        return where.toString();
    }

    private int bindSearchParams(PreparedStatement ps, StaffBookingListSearchCriteriaDTO criteria, int startIdx)
            throws Exception {
        int idx = startIdx;
        ps.setInt(idx++, criteria.getFacilityId());
        if (criteria.isHasSearch()) {
            if (criteria.isNumericSearch()) {
                ps.setInt(idx++, Integer.parseInt(criteria.getSearch()));
            }
            ps.setString(idx++, criteria.getLikePattern());
            ps.setString(idx++, criteria.getLikePattern());
            ps.setString(idx++, criteria.getLikePattern());
            ps.setString(idx++, criteria.getLikePattern());
        }
        if (criteria.getStatus() != null) {
            ps.setString(idx++, criteria.getStatus());
        }
        if (criteria.isTodayOnly()) {
            ps.setDate(idx++, criteria.getTodayDate());
            ps.setDate(idx++, criteria.getTodayDate());
        }
        return idx;
    }
}
