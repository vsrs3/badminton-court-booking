package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffBookingListItemDto;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDto;
import com.bcb.repository.staff.StaffBookingListRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingListRepositoryImpl implements StaffBookingListRepository {

    @Override
    public int countBookings(StaffBookingListSearchCriteriaDto criteria) throws Exception {
        String fromJoin = " FROM Booking b LEFT JOIN Account a ON b.account_id = a.account_id LEFT JOIN Guest g ON b.guest_id = g.guest_id ";
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

    @Override
    public List<StaffBookingListItemDto> findBookings(StaffBookingListSearchCriteriaDto criteria, int offset, int size)
            throws Exception {
        String fromJoin = " FROM Booking b LEFT JOIN Account a ON b.account_id = a.account_id LEFT JOIN Guest g ON b.guest_id = g.guest_id ";
        String whereBase = "WHERE b.facility_id = ? AND b.booking_status != 'EXPIRED'";
        String whereSearch = buildWhereSearch(criteria);

        String sql = """
                SELECT b.booking_id,
                       COALESCE(a.full_name, g.guest_name) AS customer_name,
                       COALESCE(a.phone, g.phone) AS phone,
                       b.booking_date, b.booking_status, i.payment_status,
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
                       ) THEN 1 ELSE 0 END AS has_no_show
                """ + fromJoin + " LEFT JOIN Invoice i ON b.booking_id = i.booking_id " + whereBase + whereSearch +
                " ORDER BY b.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<StaffBookingListItemDto> results = new ArrayList<>();
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

                    StaffBookingListItemDto item = new StaffBookingListItemDto();
                    item.setBookingId(rs.getInt("booking_id"));
                    item.setCustomerName(rs.getString("customer_name"));
                    item.setPhone(rs.getString("phone"));
                    item.setBookingDate(rs.getString("booking_date"));
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

    private String buildWhereSearch(StaffBookingListSearchCriteriaDto criteria) {
        if (!criteria.isHasSearch()) {
            return "";
        }
        if (criteria.isNumericSearch()) {
            return " AND (b.booking_id = ? OR a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)";
        }
        return " AND (a.full_name LIKE ? OR g.guest_name LIKE ? OR a.phone LIKE ? OR g.phone LIKE ?)";
    }

    private int bindSearchParams(PreparedStatement ps, StaffBookingListSearchCriteriaDto criteria, int startIdx)
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
        return idx;
    }
}
