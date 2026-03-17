package com.bcb.service;

import com.bcb.model.FacilityInventory;

import java.util.List;

public interface FacilityInventoryService {

    List<FacilityInventory> getByFacilityId(int facilityId, int limit, int offset, String keyword);

    int countByFacilityId(int facilityId, String keyword);

    void assignToFacility(int facilityId, int inventoryId, int totalQuantity);

    int assignAllToFacility(int facilityId, int totalQuantity, String keyword);

    void updateQuantity(int facilityInventoryId, int totalQuantity);

    void updateAllQuantitiesByFacility(int facilityId, int totalQuantity);

    void removeById(int facilityInventoryId);

    int removeAllByFacility(int facilityId, String keyword);

    FacilityInventory getById(int facilityInventoryId);

    boolean existsByFacilityAndInventory(int facilityId, int inventoryId);
}
