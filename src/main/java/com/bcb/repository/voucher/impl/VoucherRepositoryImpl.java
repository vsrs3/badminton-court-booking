package com.bcb.repository.voucher.impl;

import com.bcb.dto.voucher.VoucherDashboardDTO;
import com.bcb.dto.voucher.VoucherDTO;
import com.bcb.dto.voucher.VoucherFilterDTO;
import com.bcb.dto.voucher.VoucherUsageDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.Voucher;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JDBC implementation of VoucherRepository.
 * Handles all database operations for Voucher, VoucherFacility, VoucherUsage tables.
 *
 * @author AnhTN
 */
public class VoucherRepositoryImpl implements VoucherRepository {

    private static final String BASE_SELECT =
        "SELECT v.*, " +
        "  (SELECT COUNT(*) FROM VoucherUsage vu WHERE vu.voucher_id = v.voucher_id) AS usage_count, " +
        "  (SELECT ISNULL(SUM(vu2.discount_amount),0) FROM VoucherUsage vu2 WHERE vu2.voucher_id = v.voucher_id) AS total_discount, " +
        "  (CASE WHEN EXISTS (" +
        "       SELECT 1 FROM VoucherUsage vu3 WHERE vu3.voucher_id = v.voucher_id" +
        "       UNION ALL" +
        "       SELECT 1 FROM Invoice i WHERE i.voucher_id = v.voucher_id" +
        "   ) THEN 1 ELSE 0 END) AS has_history " +
        "FROM Voucher v ";

    // =====================================================================
    // FIND ALL WITH FILTER
    // =====================================================================

    @Override
    public List<VoucherDTO> findAll(VoucherFilterDTO filter) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, filter);
        applySorting(sql, filter);
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(filter.getOffset());
        params.add(filter.getPageSize());

        List<VoucherDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapToDTO(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find vouchers", e);
        }
        return list;
    }

    @Override
    public int count(VoucherFilterDTO filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Voucher v WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        applyFilters(sql, params, filter);
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count vouchers", e);
        }
        return 0;
    }

    private void applyFilters(StringBuilder sql, List<Object> params, VoucherFilterDTO filter) {
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            sql.append(" AND (v.code LIKE ? OR v.name LIKE ?) ");
            String kw = "%" + filter.getKeyword().trim() + "%";
            params.add(kw); params.add(kw);
        }
        if (filter.getDiscountType() != null && !filter.getDiscountType().isEmpty()) {
            sql.append(" AND v.discount_type = ? ");
            params.add(filter.getDiscountType());
        }
        if (filter.getDateFrom() != null && !filter.getDateFrom().isEmpty()) {
            sql.append(" AND v.valid_from >= ? ");
            params.add(filter.getDateFrom() + " 00:00:00");
        }
        if (filter.getDateTo() != null && !filter.getDateTo().isEmpty()) {
            sql.append(" AND v.valid_to <= ? ");
            params.add(filter.getDateTo() + " 23:59:59");
        }
        if (filter.getFacilityId() != null) {
            sql.append(" AND (EXISTS (SELECT 1 FROM VoucherFacility vf WHERE vf.voucher_id = v.voucher_id AND vf.facility_id = ?) " +
                       " OR NOT EXISTS (SELECT 1 FROM VoucherFacility vf2 WHERE vf2.voucher_id = v.voucher_id)) ");
            params.add(filter.getFacilityId());
        }
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            switch (filter.getStatus()) {
                case "DISABLED":
                    sql.append(" AND v.is_active = 0 "); break;
                case "UPCOMING":
                    sql.append(" AND v.is_active = 1 AND GETDATE() < v.valid_from "); break;
                case "ACTIVE":
                    sql.append(" AND v.is_active = 1 AND GETDATE() BETWEEN v.valid_from AND v.valid_to "); break;
                case "EXPIRED":
                    sql.append(" AND v.is_active = 1 AND GETDATE() > v.valid_to "); break;
            }
        }
    }

    private void applySorting(StringBuilder sql, VoucherFilterDTO filter) {
        Set<String> allowed = new HashSet<>(Arrays.asList(
            "code","name","discount_value","valid_from","valid_to","created_at"));
        String col = (filter.getSortBy() != null && allowed.contains(filter.getSortBy()))
                     ? "v." + filter.getSortBy() : "v.created_at";
        String dir = "ASC".equalsIgnoreCase(filter.getSortDir()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);
    }

    // =====================================================================
    // FIND BY ID
    // =====================================================================

    @Override
    public Optional<Voucher> findById(int voucherId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Voucher WHERE voucher_id = ?")) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find voucher by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<VoucherDTO> findDTOById(int voucherId) {
        String sql = BASE_SELECT + " WHERE v.voucher_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VoucherDTO dto = mapToDTO(rs);
                    dto.setFacilityIds(findFacilityIdsByVoucherId(voucherId));
                    return Optional.of(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find voucher DTO by id", e);
        }
        return Optional.empty();
    }

    // =====================================================================
    // EXISTS BY CODE
    // =====================================================================

    @Override
    public boolean existsByCode(String code, int excludeId) {
        String sql = excludeId > 0
            ? "SELECT COUNT(*) FROM Voucher WHERE code = ? COLLATE SQL_Latin1_General_CP1_CS_AS AND voucher_id <> ?"
            : "SELECT COUNT(*) FROM Voucher WHERE code = ? COLLATE SQL_Latin1_General_CP1_CS_AS";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            if (excludeId > 0) ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check code existence", e);
        }
        return false;
    }

    // =====================================================================
    // INSERT
    // =====================================================================

    @Override
    public int insert(Voucher v) {
        String sql = "INSERT INTO Voucher (code, name, description, discount_type, discount_value, " +
            "min_order_amount, max_discount_amount, valid_from, valid_to, usage_limit, per_user_limit, " +
            "applicable_booking_type, is_active, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getCode());
            ps.setString(2, v.getName());
            ps.setString(3, v.getDescription());
            ps.setString(4, v.getDiscountType());
            ps.setBigDecimal(5, v.getDiscountValue());
            ps.setBigDecimal(6, v.getMinOrderAmount() != null ? v.getMinOrderAmount() : BigDecimal.ZERO);
            if (v.getMaxDiscountAmount() != null) ps.setBigDecimal(7, v.getMaxDiscountAmount()); else ps.setNull(7, Types.DECIMAL);
            ps.setTimestamp(8, Timestamp.valueOf(v.getValidFrom()));
            ps.setTimestamp(9, Timestamp.valueOf(v.getValidTo()));
            if (v.getUsageLimit() != null) ps.setInt(10, v.getUsageLimit()); else ps.setNull(10, Types.INTEGER);
            ps.setInt(11, v.getPerUserLimit() != null ? v.getPerUserLimit() : 1);
            ps.setString(12, v.getApplicableBookingType() != null ? v.getApplicableBookingType() : "SINGLE");
            ps.setBoolean(13, v.getIsActive() != null ? v.getIsActive() : true);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert voucher", e);
        }
        return 0;
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Override
    public int update(Voucher v) {
        String sql = "UPDATE Voucher SET code=?, name=?, description=?, discount_type=?, discount_value=?, " +
            "min_order_amount=?, max_discount_amount=?, valid_from=?, valid_to=?, usage_limit=?, per_user_limit=?, " +
            "applicable_booking_type=?, is_active=?, updated_at=GETDATE() WHERE voucher_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCode());
            ps.setString(2, v.getName());
            ps.setString(3, v.getDescription());
            ps.setString(4, v.getDiscountType());
            ps.setBigDecimal(5, v.getDiscountValue());
            ps.setBigDecimal(6, v.getMinOrderAmount() != null ? v.getMinOrderAmount() : BigDecimal.ZERO);
            if (v.getMaxDiscountAmount() != null) ps.setBigDecimal(7, v.getMaxDiscountAmount()); else ps.setNull(7, Types.DECIMAL);
            ps.setTimestamp(8, Timestamp.valueOf(v.getValidFrom()));
            ps.setTimestamp(9, Timestamp.valueOf(v.getValidTo()));
            if (v.getUsageLimit() != null) ps.setInt(10, v.getUsageLimit()); else ps.setNull(10, Types.INTEGER);
            ps.setInt(11, v.getPerUserLimit() != null ? v.getPerUserLimit() : 1);
            ps.setString(12, v.getApplicableBookingType() != null ? v.getApplicableBookingType() : "SINGLE");
            ps.setBoolean(13, v.getIsActive() != null ? v.getIsActive() : true);
            ps.setInt(14, v.getVoucherId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update voucher", e);
        }
    }

    // =====================================================================
    // SOFT DELETE
    // =====================================================================

    @Override
    public int softDelete(int voucherId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE Voucher SET is_active=0, updated_at=GETDATE() WHERE voucher_id=?")) {
            ps.setInt(1, voucherId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to soft delete voucher", e);
        }
    }

    @Override
    public int hardDelete(int voucherId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM Voucher WHERE voucher_id=?")) {
            ps.setInt(1, voucherId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to hard delete voucher", e);
        }
    }

    @Override
    public boolean hasUsageHistory(int voucherId) {
        // Check both VoucherUsage and Invoice tables
        String sql =
            "SELECT 1 WHERE EXISTS (" +
            "  SELECT 1 FROM VoucherUsage WHERE voucher_id = ?" +
            "  UNION ALL" +
            "  SELECT 1 FROM Invoice WHERE voucher_id = ?" +
            ")";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.setInt(2, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check usage history for voucher", e);
        }
    }

    // =====================================================================
    // FACILITY LINKS
    // =====================================================================

    @Override
    public List<Integer> findFacilityIdsByVoucherId(int voucherId) {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT facility_id FROM VoucherFacility WHERE voucher_id = ?")) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("facility_id"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find facility IDs for voucher", e);
        }
        return ids;
    }

    @Override
    public void replaceFacilityLinks(int voucherId, List<Integer> facilityIds) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM VoucherFacility WHERE voucher_id = ?")) {
                    del.setInt(1, voucherId);
                    del.executeUpdate();
                }
                if (facilityIds != null && !facilityIds.isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO VoucherFacility (voucher_id, facility_id) VALUES (?, ?)")) {
                        for (Integer fid : facilityIds) {
                            ins.setInt(1, voucherId); ins.setInt(2, fid);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to replace facility links", e);
        }
    }

    // =====================================================================
    // DASHBOARD STATS
    // =====================================================================

    @Override
    public VoucherDashboardDTO getDashboardStats() {
        VoucherDashboardDTO dto = new VoucherDashboardDTO();
        try (Connection conn = DBContext.getConnection()) {
            // Total vouchers
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Voucher");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dto.setTotalVouchers(rs.getInt(1));
            }
            // Active vouchers
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Voucher WHERE is_active=1 AND GETDATE() BETWEEN valid_from AND valid_to");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dto.setActiveVouchers(rs.getInt(1));
            }
            // Monthly usage
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt, ISNULL(SUM(discount_amount),0) AS total " +
                    "FROM VoucherUsage WHERE MONTH(used_at)=MONTH(GETDATE()) AND YEAR(used_at)=YEAR(GETDATE())");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setMonthlyUsageCount(rs.getInt("cnt"));
                    dto.setMonthlyDiscountTotal(rs.getBigDecimal("total"));
                }
            }
            // Daily chart (last 30 days)
            List<Map<String, Object>> dailyChart = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CONVERT(DATE, used_at) AS day, COUNT(*) AS cnt FROM VoucherUsage " +
                    "WHERE used_at >= DATEADD(DAY,-29,CAST(GETDATE() AS DATE)) " +
                    "GROUP BY CONVERT(DATE, used_at) ORDER BY day ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", rs.getString("day"));
                    row.put("count", rs.getInt("cnt"));
                    dailyChart.add(row);
                }
            }
            dto.setDailyUsageChart(dailyChart);
            // Top vouchers
            List<Map<String, Object>> topList = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TOP 10 v.code, v.name, COUNT(vu.usage_id) AS usage_count " +
                    "FROM Voucher v LEFT JOIN VoucherUsage vu ON v.voucher_id=vu.voucher_id " +
                    "GROUP BY v.voucher_id, v.code, v.name ORDER BY usage_count DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", rs.getString("code"));
                    row.put("name", rs.getString("name"));
                    row.put("usageCount", rs.getInt("usage_count"));
                    topList.add(row);
                }
            }
            dto.setTopVouchers(topList);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get dashboard stats", e);
        }
        return dto;
    }

    // =====================================================================
    // USAGE HISTORY
    // =====================================================================

    @Override
    public List<VoucherUsageDTO> findUsageByVoucherId(int voucherId, int offset, int limit) {
        String sql = "SELECT vu.usage_id, vu.voucher_id, v.code AS voucher_code, " +
            "vu.account_id, a.full_name AS account_name, a.email AS account_email, " +
            "vu.booking_id, vu.invoice_id, vu.discount_amount, vu.used_at " +
            "FROM VoucherUsage vu " +
            "LEFT JOIN Voucher v ON v.voucher_id=vu.voucher_id " +
            "LEFT JOIN Account a ON a.account_id=vu.account_id " +
            "WHERE vu.voucher_id=? ORDER BY vu.used_at DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<VoucherUsageDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId); ps.setInt(2, offset); ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VoucherUsageDTO u = new VoucherUsageDTO();
                    u.setUsageId(rs.getInt("usage_id"));
                    u.setVoucherId(rs.getInt("voucher_id"));
                    u.setVoucherCode(rs.getString("voucher_code"));
                    u.setAccountId(rs.getObject("account_id") != null ? rs.getInt("account_id") : null);
                    u.setAccountName(rs.getString("account_name"));
                    u.setAccountEmail(rs.getString("account_email"));
                    u.setBookingId(rs.getInt("booking_id"));
                    u.setInvoiceId(rs.getInt("invoice_id"));
                    u.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    Timestamp ts = rs.getTimestamp("used_at");
                    if (ts != null) u.setUsedAt(ts.toLocalDateTime());
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find usage history", e);
        }
        return list;
    }

    @Override
    public int countUsageByVoucherId(int voucherId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM VoucherUsage WHERE voucher_id=?")) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count usage", e);
        }
        return 0;
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM Voucher WHERE code = ? COLLATE SQL_Latin1_General_CP1_CS_AS")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find voucher by code", e);
        }
        return Optional.empty();
    }

    @Override
    public int countUsageByVoucherAndAccount(int voucherId, int accountId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM VoucherUsage WHERE voucher_id=? AND account_id=?")) {
            ps.setInt(1, voucherId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count usage by account", e);
        }
        return 0;
    }

    @Override
    public int countTotalUsageByVoucher(int voucherId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM VoucherUsage WHERE voucher_id=?")) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count total usage", e);
        }
        return 0;
    }

    @Override
    public void insertVoucherUsage(Connection conn, int voucherId, Integer accountId,
                                   int bookingId, int invoiceId, java.math.BigDecimal discountAmt) {
        String sql = "INSERT INTO VoucherUsage (voucher_id, account_id, booking_id, invoice_id, discount_amount) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            if (accountId != null) ps.setInt(2, accountId); else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, bookingId);
            ps.setInt(4, invoiceId);
            ps.setBigDecimal(5, discountAmt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert VoucherUsage", e);
        }
    }

    // =====================================================================
    // MAPPERS
    // =====================================================================

    private Voucher mapToEntity(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setVoucherId(rs.getInt("voucher_id"));
        v.setCode(rs.getString("code"));
        v.setName(rs.getString("name"));
        v.setDescription(rs.getString("description"));
        v.setDiscountType(rs.getString("discount_type"));
        v.setDiscountValue(rs.getBigDecimal("discount_value"));
        v.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        v.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        Timestamp vf = rs.getTimestamp("valid_from");
        if (vf != null) v.setValidFrom(vf.toLocalDateTime());
        Timestamp vt = rs.getTimestamp("valid_to");
        if (vt != null) v.setValidTo(vt.toLocalDateTime());
        if (rs.getObject("usage_limit") != null) v.setUsageLimit(rs.getInt("usage_limit"));
        v.setPerUserLimit(rs.getInt("per_user_limit"));
        v.setApplicableBookingType(rs.getString("applicable_booking_type"));
        v.setIsActive(rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) v.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) v.setUpdatedAt(ua.toLocalDateTime());
        return v;
    }

    private VoucherDTO mapToDTO(ResultSet rs) throws SQLException {
        VoucherDTO dto = new VoucherDTO();
        dto.setVoucherId(rs.getInt("voucher_id"));
        dto.setCode(rs.getString("code"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setDiscountType(rs.getString("discount_type"));
        dto.setDiscountValue(rs.getBigDecimal("discount_value"));
        dto.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        dto.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        Timestamp vf = rs.getTimestamp("valid_from");
        if (vf != null) dto.setValidFrom(vf.toLocalDateTime());
        Timestamp vt = rs.getTimestamp("valid_to");
        if (vt != null) dto.setValidTo(vt.toLocalDateTime());
        if (rs.getObject("usage_limit") != null) dto.setUsageLimit(rs.getInt("usage_limit"));
        dto.setPerUserLimit(rs.getInt("per_user_limit"));
        dto.setApplicableBookingType(rs.getString("applicable_booking_type"));
        dto.setIsActive(rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) dto.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) dto.setUpdatedAt(ua.toLocalDateTime());
        dto.setUsageCount(rs.getInt("usage_count"));
        dto.setTotalDiscountGiven(rs.getBigDecimal("total_discount"));
        dto.setHasHistory(rs.getInt("has_history") == 1);
        dto.setStatus(computeStatus(dto));
        return dto;
    }

    private String computeStatus(VoucherDTO dto) {
        if (dto.getIsActive() == null || !dto.getIsActive()) return "DISABLED";
        LocalDateTime now = LocalDateTime.now();
        if (dto.getValidFrom() != null && now.isBefore(dto.getValidFrom())) return "UPCOMING";
        if (dto.getValidTo() != null && now.isAfter(dto.getValidTo())) return "EXPIRED";
        return "ACTIVE";
    }
}
