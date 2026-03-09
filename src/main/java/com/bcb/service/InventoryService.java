package com.bcb.service;

import com.bcb.model.Inventory;
import java.util.List;

public interface InventoryService {
    List<Inventory> getAll();
    List<Inventory> search(String keyword);
    Inventory getById(int id);
    void create(Inventory inventory);
    void update(Inventory inventory);
    void delete(int id);

    List<Inventory> getWithPagination(int limit, int offset, String keyword);

    int countInventory(String keyword);

    List<Inventory> getActiveNotAssignedToFacility(int facilityId, String keyword);

    List<Inventory> getActiveNotAssignedToFacilityWithPagination(int facilityId, int limit, int offset, String keyword);

    int countActiveNotAssignedToFacility(int facilityId, String keyword);
}