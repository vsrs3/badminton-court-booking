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
        if (voucherRepo.existsByCode(voucher.getCode(), 0)) {
            throw new BusinessException("DUPLICATE_CODE",
                "Mã voucher '" + voucher.getCode() + "' đã tồn tại.");
        }
        if (voucher.getValidFrom() != null && voucher.getValidTo() != null
                && !voucher.getValidFrom().isBefore(voucher.getValidTo())) {
            throw new BusinessException("INVALID_DATE",
                "Thời gian bắt đầu phải trước thời gian kết thúc.");
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
        if (voucherRepo.existsByCode(voucher.getCode(), voucher.getVoucherId())) {
            throw new BusinessException("DUPLICATE_CODE",
                "Mã voucher '" + voucher.getCode() + "' đã được sử dụng bởi voucher khác.");
        }
        if (voucher.getValidFrom() != null && voucher.getValidTo() != null
                && !voucher.getValidFrom().isBefore(voucher.getValidTo())) {
            throw new BusinessException("INVALID_DATE",
                "Thời gian bắt đầu phải trước thời gian kết thúc.");
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
}
