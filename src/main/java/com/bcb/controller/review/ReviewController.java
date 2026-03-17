package com.bcb.controller.review;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.bcb.dto.review.ReviewDTO;
import com.bcb.dto.review.ReviewUserListDTO;
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
			
			case "user-list" -> listUserReview(request, response);
			
			case "location-list" -> listLocationReview(request, response);
		
			case "view" -> viewReview(request, response);
	
			default -> {
				response.sendRedirect(request.getContextPath() + "/reviews");
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

		if (action == null || action.isEmpty()) return;

		switch (action) {
			case "add" -> addReview(request, response);
	
			case "edit" -> editReview(request, response);
	
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

			ReviewDTO dto = new ReviewDTO(bookingId, accountId, rating, comment);
			boolean result = reviewService.addReview(dto);

			if (result) {
				session.setAttribute("successMessage", "Tạo đánh giá thành công");
			} else {
				session.setAttribute("errorMessage", "Tạo đánh giá thất bại");
			}
			response.sendRedirect(request.getContextPath() + "/my-bookings");

		} catch (Exception e) {
			session.setAttribute("errorMessage", "Không thể tạo đánh giá!");
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

			ReviewDTO dto = new ReviewDTO(bookingId, accountId, rating, comment);
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
			session.setAttribute ("errorMessage", "Không thể chỉnh sửa đánh giá");
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
			session.setAttribute("errorMessage", "Không thể xem đánh giá!");
			response.sendRedirect(request.getContextPath() + "/my-bookings");
		}

	}

	
	private void listUserReview(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    HttpSession session = request.getSession();
	    Account user = (Account) session.getAttribute("account");

	    String dateFromStr = request.getParameter("dateFrom");
	    String dateToStr   = request.getParameter("dateTo");
	    String ratingParam = request.getParameter("rating");

	    try {
	    	
	        // Null parse 
	        LocalDate dateFrom = (dateFromStr != null && !dateFromStr.isEmpty()) ? LocalDate.parse(dateFromStr) : null;
	        LocalDate dateTo   = (dateToStr != null && !dateToStr.isEmpty()) ? LocalDate.parse(dateToStr) : null;
	        Integer accountId = user.getAccountId();
	        
	        ReviewUserListDTO dto = new ReviewUserListDTO(accountId, dateFrom, dateTo);
	        List<ReviewUserListDTO> list = reviewService.listUserReview(dto);

	        session.setAttribute("listUserReview", list);
	        request.setAttribute("dateFrom", dateFromStr);
	        request.setAttribute("dateTo", dateToStr);
	        request.setAttribute("selectedRating", ratingParam);
	        
	        request.setAttribute("section", "review-list-user");
	        request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);

	    } catch (Exception e) {
	        session.setAttribute("errorMessage", "Lỗi không lấy được danh sách đánh giá!");
	        response.sendRedirect(request.getContextPath() + "/reviews");
	    }
	}

	
	private void listLocationReview (HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		String facilityIdParam = request.getParameter("facilityId");
		
		try {
			Integer facilityId = Integer.parseInt(facilityIdParam);
			List<Review> reviews = reviewService.listLocationReview(facilityId);
			
			if(reviews == null) {
				request.setAttribute("error", "danh sách review bị null");
			} else {
				request.setAttribute("reviews", reviews);
			}
			
			request.getRequestDispatcher("/jsp/owner/court/court-list.jsp").forward(request, response);
			
		} catch (Exception e) {
			request.setAttribute("error", "Lỗi Param null hoặc khi truy vấn database");
			request.getRequestDispatcher("/jsp/owner/court/court-list.jsp").forward(request, response);
		}
	}
	
	
	private Account getAuthenticatedCustomer (HttpServletRequest request, HttpServletResponse response)
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
			response.sendRedirect(request.getContextPath() + "/");
			return null;
		}
		return account;
	}
}