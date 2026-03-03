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
    List<Inventory> findByFacility(int facilityId, int limit, int offset, String keyword);
    int countByFacility(int facilityId, String keyword);

    List<Inventory> findUnassigned(int limit, int offset, String keyword);
    int countUnassigned(String keyword);

    void assignToCourt(int inventoryId, int courtId);
    void removeFromCourt(int inventoryId);
}