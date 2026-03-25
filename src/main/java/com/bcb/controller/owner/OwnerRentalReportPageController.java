package com.bcb.controller.owner;

import com.bcb.utils.BreadcrumbUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/owner/rental-report")
public class OwnerRentalReportPageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BreadcrumbUtils.builder(request).active("Báo cáo doanh thu thuê đồ").build();
        request.getRequestDispatcher("/jsp/owner/rental-report.jsp").forward(request, response);
    }
}
