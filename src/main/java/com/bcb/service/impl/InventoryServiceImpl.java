package com.bcb.service.impl;

import com.bcb.model.Inventory;
import com.bcb.repository.InventoryRepository;
import com.bcb.repository.impl.InventoryRepositoryImpl;
import com.bcb.service.InventoryService;

import java.util.List;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository = new InventoryRepositoryImpl();

    @Override
    public List<Inventory> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Inventory> search(String keyword) {
        return repository.search(keyword);
    }

    @Override
    public Inventory getById(int id) {
        return repository.findById(id);
    }

    @Override
    public void create(Inventory inventory) {
        repository.save(inventory);
    }

    @Override
    public void update(Inventory inventory) {
        repository.update(inventory);
    }

    @Override
    public void delete(int id) {
        repository.delete(id);
    }
}