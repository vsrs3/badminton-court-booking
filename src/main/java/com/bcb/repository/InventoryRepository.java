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
}