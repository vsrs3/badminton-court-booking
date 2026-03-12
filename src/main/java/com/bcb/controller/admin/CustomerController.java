package com.bcb.controller.admin;

import com.bcb.service.CustomerService;
import com.bcb.service.impl.CustomerServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/customers")
public class CustomerController extends HttpServlet {

    private final CustomerService service = new CustomerServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        int totalCustomers = service.getTotalCustomers();

        List<Object[]> list = service.getLatestCustomers();

        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("customers", list);

        request.getRequestDispatcher("/jsp/admin/customers.jsp")
                .forward(request, response);
    }
}