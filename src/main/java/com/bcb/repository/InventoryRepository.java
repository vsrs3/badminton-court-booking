package com.bcb.repository;

import com.bcb.model.Inventory;
import java.util.List;

public interface InventoryRepository {

    List<Inventory> findAll();

    List<Inventory> search(String keyword);

    Inventory findById(int id);

    void save(Inventory inventory);

    void update(Inventory inventory);

    void delete(int id);

    List<Inventory> findWithPagination(int limit, int offset, String keyword, Boolean activeStatus, String priceSort);

    int countInventory(String keyword, Boolean activeStatus);

    List<Inventory> findActiveNotAssignedToFacility(int facilityId, String keyword);

    List<Inventory> findActiveNotAssignedToFacilityWithPagination(int facilityId, int limit, int offset, String keyword);

    int countActiveNotAssignedToFacility(int facilityId, String keyword);
}
