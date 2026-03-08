package com.bcb.repository.voucher;

import com.bcb.dto.voucher.VoucherDashboardDTO;
import com.bcb.dto.voucher.VoucherFilterDTO;
import com.bcb.dto.voucher.VoucherDTO;
import com.bcb.dto.voucher.VoucherUsageDTO;
import com.bcb.model.Voucher;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Voucher data access operations.
 *
 * @author AnhTN
 */
public interface VoucherRepository {

    /**
     * Find all vouchers with optional filter/pagination.
     * Returns VoucherDTO with computed status and usage count.
     *
     * @param filter Filter and pagination params
     * @return List of VoucherDTO
     */
    List<VoucherDTO> findAll(VoucherFilterDTO filter);

    /**
     * Count total vouchers matching filter (without pagination).
     *
     * @param filter Filter params
     * @return total count
     */
    int count(VoucherFilterDTO filter);

    /**
     * Find voucher by ID. Returns raw entity.
     *
     * @param voucherId Voucher ID
     * @return Optional Voucher
     */
    Optional<Voucher> findById(int voucherId);

    /**
     * Find VoucherDTO by ID including computed fields.
     *
     * @param voucherId Voucher ID
     * @return Optional VoucherDTO
     */
    Optional<VoucherDTO> findDTOById(int voucherId);

    /**
     * Check if a voucher code already exists (case-sensitive).
     *
     * @param code Voucher code
     * @param excludeId Exclude this voucher ID from check (for update), 0 = no exclusion
     * @return true if code exists
     */
    boolean existsByCode(String code, int excludeId);

    /**
     * Insert a new voucher.
     *
     * @param voucher Voucher entity
     * @return Generated voucher_id
     */
    int insert(Voucher voucher);

    /**
     * Update existing voucher.
     *
     * @param voucher Voucher entity
     * @return rows affected
     */
    int update(Voucher voucher);

    /**
     * Soft delete voucher (set is_active = 0).
     *
     * @param voucherId Voucher ID
     * @return rows affected
     */
    int softDelete(int voucherId);

    /**
     * Hard delete voucher permanently from database.
     * Only safe to call when hasUsageHistory() returns false.
     *
     * @param voucherId Voucher ID
     * @return rows affected
     */
    int hardDelete(int voucherId);

    /**
     * Check whether a voucher has any usage history in VoucherUsage or Invoice tables.
     * Used to decide soft-delete vs hard-delete.
     *
     * @param voucherId Voucher ID
     * @return true if at least one usage record exists
     */
    boolean hasUsageHistory(int voucherId);

    /**
     * Get list of facility IDs linked to a voucher.
     *
     * @param voucherId Voucher ID
     * @return List of facility IDs
     */
    List<Integer> findFacilityIdsByVoucherId(int voucherId);

    /**
     * Replace all facility associations for a voucher.
     * Deletes existing rows then inserts new ones.
     *
     * @param voucherId    Voucher ID
     * @param facilityIds  List of facility IDs (empty = applies to all)
     */
    void replaceFacilityLinks(int voucherId, List<Integer> facilityIds);

    /**
     * Get dashboard statistics for the owner voucher dashboard.
     *
     * @return VoucherDashboardDTO
     */
    VoucherDashboardDTO getDashboardStats();

    /**
     * Get paginated voucher usage history for a specific voucher.
     *
     * @param voucherId Voucher ID
     * @param offset    Pagination offset
     * @param limit     Page size
     * @return List of VoucherUsageDTO
     */
    List<VoucherUsageDTO> findUsageByVoucherId(int voucherId, int offset, int limit);

    /**
     * Count total usage rows for a voucher.
     *
     * @param voucherId Voucher ID
     * @return count
     */
    int countUsageByVoucherId(int voucherId);
}
