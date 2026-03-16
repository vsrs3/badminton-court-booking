package com.bcb.service.impl;

import com.bcb.repository.RevenueRepository;
import com.bcb.repository.impl.RevenueRepositoryImpl;
import com.bcb.service.RevenueService;

import java.util.List;

public class RevenueServiceImpl implements RevenueService {

    private final RevenueRepository revenueRepository = new RevenueRepositoryImpl();

    @Override
    public double getTotalRevenue() {
        return revenueRepository.getTotalRevenue();
    }

    @Override
    public List<Object[]> getRecentTransactions() {
        return revenueRepository.getRecentTransactions();
    }
}