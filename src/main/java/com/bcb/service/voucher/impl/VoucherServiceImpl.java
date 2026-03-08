package com.bcb.service.voucher.impl;

import com.bcb.dto.voucher.VoucherDashboardDTO;
import com.bcb.dto.voucher.VoucherDTO;
import com.bcb.dto.voucher.VoucherFilterDTO;
import com.bcb.dto.voucher.VoucherUsageDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Facility;
import com.bcb.model.Voucher;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.repository.voucher.VoucherRepository;
import com.bcb.repository.voucher.impl.VoucherRepositoryImpl;
import com.bcb.service.voucher.VoucherService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of VoucherService.
 * Handles all business logic for owner voucher management.
 *
 * @author AnhTN
 */
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepo;
    private final FacilityRepository facilityRepo;

    public VoucherServiceImpl() {
        this.voucherRepo  = new VoucherRepositoryImpl();
        this.facilityRepo = new FacilityRepositoryImpl();
    }

    @Override
    public Map<String, Object> getVoucherList(VoucherFilterDTO filter) {
        List<VoucherDTO> items = voucherRepo.findAll(filter);
        int totalItems = voucherRepo.count(filter);
        int totalPages = (int) Math.ceil((double) totalItems / filter.getPageSize());
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalItems", totalItems);
        result.put("totalPages", Math.max(1, totalPages));
        result.put("currentPage", filter.getPage());
        return result;
    }

    @Override
    public VoucherDTO getVoucherDetail(int voucherId) throws BusinessException {
        return voucherRepo.findDTOById(voucherId)
            .orElseThrow(() -> new BusinessException("VOUCHER_NOT_FOUND",
                "Không tìm thấy voucher với ID: " + voucherId));
    }

    @Override
    public Voucher getVoucherEntity(int voucherId) throws BusinessException {
        return voucherRepo.findById(voucherId)
            .orElseThrow(() -> new BusinessException("VOUCHER_NOT_FOUND",
                "Không tìm thấy voucher với ID: " + voucherId));
    }

    @Override
    public int createVoucher(Voucher voucher, List<Integer> facilityIds) throws BusinessException {
        validate(voucher);
        if (voucherRepo.existsByCode(voucher.getCode(), 0)) {
            throw new BusinessException("DUPLICATE_CODE",
                "Mã voucher '" + voucher.getCode() + "' đã tồn tại.");
        }
        int newId = voucherRepo.insert(voucher);
        if (newId > 0) voucherRepo.replaceFacilityLinks(newId, facilityIds);
        return newId;
    }

    @Override
    public void updateVoucher(Voucher voucher, List<Integer> facilityIds) throws BusinessException {
        voucherRepo.findById(voucher.getVoucherId())
            .orElseThrow(() -> new BusinessException("VOUCHER_NOT_FOUND",
                "Không tìm thấy voucher với ID: " + voucher.getVoucherId()));
        validate(voucher);
        if (voucherRepo.existsByCode(voucher.getCode(), voucher.getVoucherId())) {
            throw new BusinessException("DUPLICATE_CODE",
                "Mã voucher '" + voucher.getCode() + "' đã được sử dụng bởi voucher khác.");
        }
        voucherRepo.update(voucher);
        voucherRepo.replaceFacilityLinks(voucher.getVoucherId(), facilityIds);
    }

    @Override
    public String deleteVoucher(int voucherId) throws BusinessException {
        voucherRepo.findById(voucherId)
            .orElseThrow(() -> new BusinessException("VOUCHER_NOT_FOUND",
                "Không tìm thấy voucher với ID: " + voucherId));
        if (voucherRepo.hasUsageHistory(voucherId)) {
            // Voucher đã từng sử dụng → chỉ được xóa mềm để giữ lịch sử
            voucherRepo.softDelete(voucherId);
            return "SOFT";
        } else {
            // Chưa từng sử dụng → xóa vĩnh viễn
            voucherRepo.hardDelete(voucherId);
            return "HARD";
        }
    }

    @Override
    public VoucherDashboardDTO getDashboardStats() {
        return voucherRepo.getDashboardStats();
    }

    @Override
    public Map<String, Object> getUsageHistory(int voucherId, int page, int pageSize) {
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        int safePage     = Math.max(1, page);
        int offset       = (safePage - 1) * safePageSize;
        List<VoucherUsageDTO> items = voucherRepo.findUsageByVoucherId(voucherId, offset, safePageSize);
        int totalItems  = voucherRepo.countUsageByVoucherId(voucherId);
        int totalPages  = (int) Math.ceil((double) totalItems / safePageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalItems", totalItems);
        result.put("totalPages", Math.max(1, totalPages));
        result.put("currentPage", safePage);
        return result;
    }

    @Override
    public List<Facility> getAllFacilities() {
        return facilityRepo.findAll(Integer.MAX_VALUE, 0);
    }

    // =====================================================================
    // PRIVATE VALIDATION
    // =====================================================================

    /**
     * Validates a Voucher entity against all business rules.
     * Mirrors the same rules enforced by the frontend form.
     *
     * Rules:
     * - code, name, discountType, validFrom, validTo are required
     * - discountValue > 0; if PERCENTAGE then also ≤ 100
     * - validFrom must be strictly before validTo
     * - minOrderAmount ≥ 0 (if provided)
     * - maxDiscountAmount ≥ 0 (if provided, only meaningful for PERCENTAGE)
     * - usageLimit ≥ 1 (if provided)
     * - perUserLimit ≥ 1
     *
     * @param v Voucher to validate
     * @throws BusinessException with a user-friendly Vietnamese message on any rule violation
     * @author AnhTN
     */
    private void validate(Voucher v) throws BusinessException {

        // ── Required: code ──
        if (v.getCode() == null || v.getCode().isBlank()) {
            throw new BusinessException("REQUIRED_CODE", "Mã voucher không được để trống.");
        }
        if (v.getCode().length() > 50) {
            throw new BusinessException("CODE_TOO_LONG", "Mã voucher không được vượt quá 50 ký tự.");
        }

        // ── Required: name ──
        if (v.getName() == null || v.getName().isBlank()) {
            throw new BusinessException("REQUIRED_NAME", "Tên voucher không được để trống.");
        }
        if (v.getName().length() > 255) {
            throw new BusinessException("NAME_TOO_LONG", "Tên voucher không được vượt quá 255 ký tự.");
        }

        // ── Description max length ──
        if (v.getDescription() != null && v.getDescription().length() > 500) {
            throw new BusinessException("DESC_TOO_LONG", "Mô tả không được vượt quá 500 ký tự.");
        }

        // ── Required: discountType ──
        if (v.getDiscountType() == null || v.getDiscountType().isBlank()) {
            throw new BusinessException("REQUIRED_DISCOUNT_TYPE", "Vui lòng chọn loại giảm giá.");
        }
        if (!"PERCENTAGE".equals(v.getDiscountType()) && !"FIXED_AMOUNT".equals(v.getDiscountType())) {
            throw new BusinessException("INVALID_DISCOUNT_TYPE",
                "Loại giảm giá không hợp lệ. Chỉ chấp nhận PERCENTAGE hoặc FIXED_AMOUNT.");
        }

        // ── Required: discountValue ──
        if (v.getDiscountValue() == null) {
            throw new BusinessException("REQUIRED_DISCOUNT_VALUE", "Vui lòng nhập giá trị giảm.");
        }
        if ("PERCENTAGE".equals(v.getDiscountType())) {
            // Must be (0, 100]
            if (v.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0
                    || v.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("INVALID_DISCOUNT_VALUE",
                    "Giá trị giảm phần trăm phải từ 1 đến 100.");
            }
        } else {
            // FIXED_AMOUNT: must be > 0
            if (v.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_DISCOUNT_VALUE",
                    "Giá trị giảm phải lớn hơn 0.");
            }
        }

        // ── minOrderAmount ≥ 0 ──
        if (v.getMinOrderAmount() != null
                && v.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_MIN_ORDER",
                "Đơn hàng tối thiểu không được âm.");
        }

        // ── maxDiscountAmount ≥ 0 (only validated when provided) ──
        if (v.getMaxDiscountAmount() != null
                && v.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_MAX_DISCOUNT",
                "Giảm tối đa không được âm.");
        }

        // ── Required: validFrom ──
        if (v.getValidFrom() == null) {
            throw new BusinessException("REQUIRED_VALID_FROM", "Vui lòng chọn thời gian bắt đầu.");
        }

        // ── Required: validTo ──
        if (v.getValidTo() == null) {
            throw new BusinessException("REQUIRED_VALID_TO", "Vui lòng chọn thời gian kết thúc.");
        }

        // ── validFrom must be strictly before validTo ──
        if (!v.getValidFrom().isBefore(v.getValidTo())) {
            throw new BusinessException("INVALID_DATE_RANGE",
                "Thời gian bắt đầu phải trước thời gian kết thúc.");
        }

        // ── usageLimit ≥ 1 (if provided) ──
        if (v.getUsageLimit() != null && v.getUsageLimit() < 1) {
            throw new BusinessException("INVALID_USAGE_LIMIT",
                "Giới hạn sử dụng phải ít nhất là 1.");
        }

        // ── perUserLimit ≥ 1 ──
        if (v.getPerUserLimit() == null || v.getPerUserLimit() < 1) {
            throw new BusinessException("INVALID_PER_USER_LIMIT",
                "Giới hạn mỗi user phải ít nhất là 1.");
        }

        // ── applicableBookingType ──
        if (v.getApplicableBookingType() == null
                || (!v.getApplicableBookingType().equals("SINGLE")
                    && !v.getApplicableBookingType().equals("RECURRING")
                    && !v.getApplicableBookingType().equals("BOTH"))) {
            throw new BusinessException("INVALID_BOOKING_TYPE",
                "Loại booking áp dụng không hợp lệ.");
        }
    }
}
