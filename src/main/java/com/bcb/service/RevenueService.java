package com.bcb.service;

import java.util.List;

public interface RevenueService {

    double getTotalRevenue();

    List<Object[]> getRecentTransactions();

}