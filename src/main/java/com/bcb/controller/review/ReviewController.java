package com.bcb.controller.review;

import java.io.IOException;
import java.util.Optional;

import com.bcb.dto.ReviewDTO;
import com.bcb.model.Account;
import com.bcb.model.Facility;
import com.bcb.model.Review;
import com.bcb.service.FacilityService;
import com.bcb.service.ReviewService;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.service.impl.ReviewServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ReviewController", urlPatterns = "/reviews")
public class ReviewController extends HttpServlet {

	private final ReviewService reviewService = new ReviewServiceImpl();
	private final FacilityService facilityService = new FacilityServiceImpl();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Account account = getAuthenticatedCustomer(request, response);
		if (account == null) {
			return;
		}

		String action = request.getParameter("action");

		if (action == null || action.isEmpty())
			action = "view";

		switch (action) {
			case "list" -> listReview(request, response);
	
			case "view" -> viewReview(request, response);
	
			default -> {
				response.sendRedirect(request.getContextPath() + "/my-bookings");
			}
		}
	}

	/**
	 * Nếu lỗi 405 phần "add review" hãy check web.xml và thêm vào cuối <\web-app>
	 * <servlet> <servlet-name>ReviewController</servlet-name>
	 * <servlet-class>com.bcb.controller.review.ReviewController</servlet-class>
	 * </servlet> <servlet-mapping> <servlet-name>ReviewController</servlet-name>
	 * <url-pattern>/reviews</url-pattern> </servlet-mapping>
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Check Auth
		Account account = getAuthenticatedCustomer(request, response);
		if (account == null) {
			return;
		}

		String action = request.getParameter("action");

		if (action == null || action.isEmpty())
			action = "add";

		switch (action) {
			case "add" -> addReview(request, response);
	
			case "edit" -> editReview(request, response);
			
			case "delete" -> deleteReview(request, response);
	
			default -> {
				response.sendRedirect(request.getContextPath() + "/my-bookings");
			}
		}
	}

	private void addReview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		
		Account user = (Account) session.getAttribute("account");

		String bookingIdParam = request.getParameter("bookingId");
		String ratingParam = request.getParameter("rating");
		String comment = request.getParameter("comment");

		try {
			Integer accountId = user.getAccountId();
			Integer bookingId = Integer.parseInt(bookingIdParam);
			Integer rating = Integer.parseInt(ratingParam);
			Integer facilityId = reviewService.getFacilityIdFromBooking(accountId, bookingId);

			ReviewDTO dto = new ReviewDTO(bookingId, facilityId, accountId, rating, comment);
			boolean result = reviewService.addReview(dto);

			if (result) {
				session.setAttribute("successMessage", "Tạo đánh giá thành công");
			} else {
				session.setAttribute("errorMessage", "Tạo đánh giá thất bại");
			}
			response.sendRedirect(request.getContextPath() + "/my-bookings");

		} catch (Exception e) {
			session.setAttribute("errorMessage", "Lỗi: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/my-bookings");
		}
	}

	private void editReview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		Account user = (Account) session.getAttribute("account");
		Integer bookingId = (Integer) session.getAttribute("bookingId");
		
		String ratingParam = request.getParameter("rating");
		String comment = request.getParameter("comment");

		try {
			Integer accountId = user.getAccountId();
			Integer rating = Integer.parseInt(ratingParam);
			Integer facilityId = reviewService.getFacilityIdFromBooking(accountId, bookingId);

			ReviewDTO dto = new ReviewDTO(bookingId, facilityId, accountId, rating, comment);
			boolean result = reviewService.editReview(dto);

			if (result) {
				Facility facilityReview  = facilityService.findById(facilityId);
				Review userReview = reviewService.viewReview(dto);
				
				if(userReview != null) {
					session.setAttribute("userReview", userReview);
					session.setAttribute("facilityReview", facilityReview);
				}
				
				session.setAttribute("successMessage", "Cập nhật đánh giá thành công");
				request.setAttribute("section", "review-detail");
				
			} else {
				session.setAttribute("errorMessage", "Tạo đánh giá thất bại");
				request.setAttribute("section", "review-updation");
			}
			request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
			
		} catch (Exception e) {
			session.setAttribute ("errorMessage", "Lỗi: " + e.getMessage());
			request.setAttribute("section", "review-updation");
			request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
		}
		
	}

	private void viewReview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		
		Account user = (Account) session.getAttribute("account");
		String bookingIdParam = request.getParameter("bookingId");
		
		try {
			Integer accountId = user.getAccountId();
			Integer bookingId = Integer.parseInt(bookingIdParam);
            Integer facilityId = reviewService.getFacilityIdFromBooking(user.getAccountId(), bookingId);
            
			ReviewDTO dto = new ReviewDTO(bookingId, accountId);

			Facility facilityReview  = facilityService.findById(facilityId);
			Review userReview = reviewService.viewReview(dto);

			if(userReview != null) {
				session.setAttribute("userReview", userReview);
				session.setAttribute("facilityReview", facilityReview);

				request.setAttribute("section", "review-detail");
			    request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
			    
			} else {
				session.setAttribute("errorMessage", "Không tìm thấy đánh giá");
				response.sendRedirect(request.getContextPath() + "/my-bookings");
			}
			
			

		} catch (Exception e) {
			session.setAttribute("errorMessage", "Lỗi: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/my-bookings");
		}

	}

	private void listReview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	private void deleteReview(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		
		Account user = (Account) session.getAttribute("account");
		String bookingIdParam = request.getParameter("bookingId");
		
		try {
			Integer accountId = user.getAccountId();
			Integer bookingId = Integer.parseInt(bookingIdParam);
			
			ReviewDTO dto = new ReviewDTO(bookingId, accountId);
			
			boolean isDelete = reviewService.deleteReview(dto);
			
			if(isDelete) {
				session.setAttribute("successMessage", "Xóa đánh giá thành công");
				response.sendRedirect(request.getContextPath() + "/my-bookings");
			} else {
				session.setAttribute("errorMessage", "Xóa đánh giá thất bại");
				request.setAttribute("section", "review-detail");
			    request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private Account getAuthenticatedCustomer(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath() + "/auth/login");
			return null;
		}

		Account account = (Account) session.getAttribute("account");
		if (account == null) {
			response.sendRedirect(request.getContextPath() + "/auth/login");
			return null;
		}

		if (!"CUSTOMER".equals(account.getRole())) {
			System.out.println("[auth] KHÔNG phải CUSTOMER → redirect /");
			response.sendRedirect(request.getContextPath() + "/");
			return null;
		}
		return account;
	}
}