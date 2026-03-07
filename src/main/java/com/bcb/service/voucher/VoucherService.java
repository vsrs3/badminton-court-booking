package com.bcb.service.voucher;

import com.bcb.dto.voucher.VoucherDashboardDTO;
import com.bcb.dto.voucher.VoucherDTO;
import com.bcb.dto.voucher.VoucherFilterDTO;
import com.bcb.dto.voucher.VoucherUsageDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Facility;
import com.bcb.model.Voucher;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Voucher business logic operations.
 * Owner voucher management: create, update, delete, list, dashboard, detail.
 *
 * @author AnhTN
 */
public interface VoucherService {

    /**
     * Get paginated voucher list with filter.
     * Returns DTOs with computed status.
     *
     * @param filter Filter & pagination params
     * @return Map with keys: items (List<VoucherDTO>), totalItems, totalPages, currentPage
     */
    Map<String, Object> getVoucherList(VoucherFilterDTO filter);

    /**
     * Get voucher detail DTO (with facilityIds and usage stats).
     *
     * @param voucherId Voucher ID
     * @return VoucherDTO
     * @throws BusinessException if not found
     */
    VoucherDTO getVoucherDetail(int voucherId) throws BusinessException;

    /**
     * Get raw voucher entity for edit form pre-fill.
     *
     * @param voucherId Voucher ID
     * @return Voucher entity
     * @throws BusinessException if not found
     */
    Voucher getVoucherEntity(int voucherId) throws BusinessException;

    /**
     * Create a new voucher with facility links.
     *
     * @param voucher     Voucher entity to create
     * @param facilityIds List of facility IDs (empty = all)
     * @return Generated voucher ID
     * @throws BusinessException if code already exists or validation fails
     */
    int createVoucher(Voucher voucher, List<Integer> facilityIds) throws BusinessException;

    /**
     * Update an existing voucher with facility links.
     *
     * @param voucher     Voucher entity with updated fields
     * @param facilityIds List of facility IDs (empty = all)
     * @throws BusinessException if not found or code conflict
     */
    void updateVoucher(Voucher voucher, List<Integer> facilityIds) throws BusinessException;

    /**
     * Soft delete a voucher (set is_active = false).
     *
     * @param voucherId Voucher ID
     * @throws BusinessException if not found
     */
    void deleteVoucher(int voucherId) throws BusinessException;

    /**
     * Get dashboard statistics for owner voucher dashboard.
     *
     * @return VoucherDashboardDTO
     */
    VoucherDashboardDTO getDashboardStats();

    /**
     * Get paginated usage history for a specific voucher.
     *
     * @param voucherId Voucher ID
     * @param page      Page number (1-based)
     * @param pageSize  Records per page
     * @return Map with keys: items, totalItems, totalPages, currentPage
     */
    Map<String, Object> getUsageHistory(int voucherId, int page, int pageSize);

    /**
     * Get all facilities for the facility selection UI.
     *
     * @return List of Facility entities
     */
    List<Facility> getAllFacilities();
}
