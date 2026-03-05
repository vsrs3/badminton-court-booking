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
    List<Inventory> getByFacility(int facilityId, int limit, int offset, String keyword);

    int countByFacility(int facilityId, String keyword);

    List<Inventory> getUnassigned(int limit, int offset, String keyword);

    void assignToFacility(int inventoryId, int facilityId);

    void removeFromFacility(int inventoryId);
    
    int countUnassigned(String keyword);
    
    List<Inventory> getWithPagination(int limit, int offset, String keyword);

    int countInventory(String keyword);
}