package com.bcb.controller.customer;

import com.bcb.dto.review.ReviewDTO;
import com.bcb.dto.review.ReviewUserListDTO;
import com.bcb.model.Account;
import com.bcb.model.Facility;
import com.bcb.model.Review;
import com.bcb.repository.ReviewRepository;
import com.bcb.repository.impl.ReviewRepositoryImpl;
import com.bcb.service.ReviewService;
import com.bcb.service.FacilityService;
import com.bcb.service.impl.FacilityServiceImpl;
import com.bcb.service.impl.ReviewServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller for customer profile page
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile"})
public class ProfileController extends HttpServlet {

	private final ReviewService reviewService = new ReviewServiceImpl();
	private final FacilityService facilityService = new FacilityServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            // Not logged in
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Account account = (Account) session.getAttribute("account");

        if (!"CUSTOMER".equals(account.getRole())) {
            // Wrong role - redirect to their dashboard
            String redirectUrl = switch (account.getRole()) {
                case "ADMIN" -> "/admin/dashboard";
                case "OWNER" -> "/owner/dashboard";
                case "STAFF" -> "/staff/dashboard";
                default -> "/";
            };
            response.sendRedirect(request.getContextPath() + redirectUrl);
            return;
        }

        // Handle section = review
        String section = request.getParameter("section");

        if ("review".equals(section) || "review-updation".equals(section)) {
            String bookingIdParam = request.getParameter("bookingId");
            if (bookingIdParam == null || bookingIdParam.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }
            try {
                Integer bookingId  = Integer.parseInt(bookingIdParam);
                Integer facilityId = reviewService.getFacilityIdFromBooking(account.getAccountId(), bookingId);
                Facility facility  = facilityService.findById(facilityId);

                session.setAttribute("facilityReview", facility);
                session.setAttribute("bookingId", bookingId);

                // load review cũ cho form edit
                if ("review-updation".equals(section)) {
                    ReviewDTO dto    = new ReviewDTO(bookingId, account.getAccountId());
                    Review userReview = reviewService.viewReview(dto);
                    session.setAttribute("userReview", userReview);
                }

            } catch (Exception e) {
                session.setAttribute("errorMessage", "Lỗi khi tải thông tin trong Profile Controller ");
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
            }
        }
       
        // Show profile page
        request.getRequestDispatcher("/jsp/customer/profile.jsp").forward(request, response);
    }
}