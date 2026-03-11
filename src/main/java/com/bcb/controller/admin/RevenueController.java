package com.bcb.controller.admin;

import com.bcb.service.RevenueService;
import com.bcb.service.impl.RevenueServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/revenue")
public class RevenueController extends HttpServlet {

    private final RevenueService revenueService = new RevenueServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        double totalRevenue = revenueService.getTotalRevenue();

        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("transactions", revenueService.getRecentTransactions());

        request.getRequestDispatcher("/jsp/admin/revenue.jsp")
                .forward(request, response);
    }
}