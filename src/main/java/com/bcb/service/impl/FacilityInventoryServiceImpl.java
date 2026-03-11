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
        if (facilityId <= 0) {
            throw new IllegalArgumentException("Facility ID không hợp lệ.");
        }

        if (inventoryId <= 0) {
            throw new IllegalArgumentException("Inventory ID không hợp lệ.");
        }

        if (totalQuantity < 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm không được nhỏ hơn 0.");
        }

        boolean exists = repository.existsByFacilityAndInventory(facilityId, inventoryId);
        if (exists) {
            throw new IllegalArgumentException("Sản phẩm này đã được gán cho sân.");
        }

        repository.assignToFacility(facilityId, inventoryId, totalQuantity, totalQuantity);
    }

    @Override
    public void updateQuantity(int facilityInventoryId, int totalQuantity) {
        if (facilityInventoryId <= 0) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }

        if (totalQuantity < 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm không được nhỏ hơn 0.");
        }

        FacilityInventory existing = repository.findById(facilityInventoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu đồ gán sân.");
        }

        repository.updateQuantity(facilityInventoryId, totalQuantity, totalQuantity);
    }

    @Override
    public void removeById(int facilityInventoryId) {
        if (facilityInventoryId <= 0) {
            throw new IllegalArgumentException("Mã đồ gán sân không hợp lệ.");
        }

        FacilityInventory existing = repository.findById(facilityInventoryId);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu để gỡ.");
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
}