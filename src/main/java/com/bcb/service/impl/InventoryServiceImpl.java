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
    @Override
    public List<Inventory> getByFacility(int facilityId, int limit, int offset, String keyword) {
        return repository.findByFacility(facilityId, limit, offset, keyword);
    }

    @Override
    public int countByFacility(int facilityId, String keyword) {
        return repository.countByFacility(facilityId, keyword);
    }

    @Override
    public List<Inventory> getUnassigned(int limit, int offset, String keyword) {
        return repository.findUnassigned(limit, offset, keyword);
    }

    @Override
    public void assignToFacility(int inventoryId, int facilityId) {
        repository.assignToCourt(inventoryId, facilityId);
    }

    @Override
    public int countUnassigned(String keyword) {
        return repository.countUnassigned(keyword);
    }
    
    @Override
    public void removeFromFacility(int inventoryId) {
        repository.removeFromCourt(inventoryId);
    }
    
    
    @Override
    public List<Inventory> getWithPagination(int limit, int offset, String keyword) {
        return repository.findWithPagination(limit, offset, keyword);
    }

    @Override
    public int countInventory(String keyword) {
        return repository.countInventory(keyword);
    }
}