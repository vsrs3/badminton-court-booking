
package com.bcb.controller.customer;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ReviewController", urlPatterns = "/review-locations/*")
public class ReviewController extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		String pathInfo = request.getPathInfo();
		
		if(pathInfo == null || pathInfo.equals("/")) {
			response.sendRedirect(request.getContextPath() + "/review-locations/view");
		}
		
		try {
			if("view".equals(pathInfo)) {
				viewListReview(request, response);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
			
	}
	
	private void viewListReview(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		
	}
	
	
}
