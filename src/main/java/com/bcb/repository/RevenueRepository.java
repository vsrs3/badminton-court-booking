package com.bcb.repository;

import java.util.List;

public interface RevenueRepository {

    double getTotalRevenue();

    List<Object[]> getRecentTransactions();

}
