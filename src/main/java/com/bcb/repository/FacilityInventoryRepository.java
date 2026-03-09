package com.bcb.repository;

import com.bcb.model.FacilityInventory;
import java.util.List;

public interface FacilityInventoryRepository {

    List<FacilityInventory> findByFacilityId(int facilityId, int limit, int offset, String keyword);

    int countByFacilityId(int facilityId, String keyword);

    void assignToFacility(int facilityId, int inventoryId, int totalQuantity, int availableQuantity);

    void updateQuantity(int facilityInventoryId, int totalQuantity, int availableQuantity);

    void removeById(int facilityInventoryId);

    FacilityInventory findById(int facilityInventoryId);

    boolean existsByFacilityAndInventory(int facilityId, int inventoryId);
}