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
}