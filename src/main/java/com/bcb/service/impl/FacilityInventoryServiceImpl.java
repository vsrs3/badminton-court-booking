package com.bcb.service.impl;

import com.bcb.model.FacilityInventory;
import com.bcb.repository.FacilityInventoryRepository;
import com.bcb.repository.impl.FacilityInventoryRepositoryImpl;
import com.bcb.service.FacilityInventoryService;

import java.util.List;

public class FacilityInventoryServiceImpl implements FacilityInventoryService {

    private final FacilityInventoryRepository repository = new FacilityInventoryRepositoryImpl();

    @Override
    public List<FacilityInventory> getByFacilityId(int facilityId, int limit, int offset, String keyword) {
        return repository.findByFacilityId(facilityId, limit, offset, keyword);
    }

    @Override
    public int countByFacilityId(int facilityId, String keyword) {
        return repository.countByFacilityId(facilityId, keyword);
    }

    @Override
    public void assignToFacility(int facilityId, int inventoryId, int totalQuantity) {
        validateFacilityId(facilityId);
        validateInventoryId(inventoryId);
        validateQuantity(totalQuantity);

        if (repository.existsByFacilityAndInventory(facilityId, inventoryId)) {
            throw new IllegalArgumentException("Sản phẩm này đã được gán cho sân.");
        }

        repository.assignToFacility(facilityId, inventoryId, totalQuantity, totalQuantity);
    }

    @Override
    public int assignAllToFacility(int facilityId, int totalQuantity, String keyword) {
        validateFacilityId(facilityId);
        validateQuantity(totalQuantity);

        int assignedCount = repository.assignAllToFacility(
                facilityId,
                totalQuantity,
                totalQuantity,
                normalizeKeyword(keyword)
        );

        if (assignedCount <= 0) {
            throw new IllegalArgumentException("Không có đồ khả dụng để gán cho sân.");
        }

        return assignedCount;
    }

    @Override
    public void updateQuantity(int facilityInventoryId, int totalQuantity) {
        if (facilityInventoryId <= 0) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }
        validateQuantity(totalQuantity);

        FacilityInventory existing = repository.findById(facilityInventoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu đồ đã gán cho sân.");
        }

        repository.updateQuantity(facilityInventoryId, totalQuantity, totalQuantity);
    }

    @Override
    public void updateAllQuantitiesByFacility(int facilityId, int totalQuantity) {
        validateFacilityId(facilityId);
        validateQuantity(totalQuantity);

        if (repository.countByFacilityId(facilityId, null) <= 0) {
            throw new IllegalArgumentException("Sân hiện chưa có đồ nào được gán.");
        }

        repository.updateAllQuantitiesByFacility(facilityId, totalQuantity, totalQuantity);
    }

    @Override
    public void removeById(int facilityInventoryId) {
        if (facilityInventoryId <= 0) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }

        FacilityInventory existing = repository.findById(facilityInventoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu cần gỡ.");
        }

        repository.removeById(facilityInventoryId);
    }

    @Override
    public FacilityInventory getById(int facilityInventoryId) {
        if (facilityInventoryId <= 0) {
            return null;
        }
        return repository.findById(facilityInventoryId);
    }

    @Override
    public boolean existsByFacilityAndInventory(int facilityId, int inventoryId) {
        if (facilityId <= 0 || inventoryId <= 0) {
            return false;
        }
        return repository.existsByFacilityAndInventory(facilityId, inventoryId);
    }

    private void validateFacilityId(int facilityId) {
        if (facilityId <= 0) {
            throw new IllegalArgumentException("Mã sân không hợp lệ.");
        }
    }

    private void validateInventoryId(int inventoryId) {
        if (inventoryId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được nhỏ hơn 0.");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
