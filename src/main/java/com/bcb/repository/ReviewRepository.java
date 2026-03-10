package com.bcb.repository;

import java.util.Optional;
import java.util.Set;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.bcb.dto.review.ReviewUserListDTO;
import com.bcb.model.Review;

public interface ReviewRepository {
	
	/**
	 * Create new user review
	 * @param bookingId
	 * @param accountId
	 * @param facilityId
	 * @return
	 */
	boolean addReview (Integer bookingId,  Integer accountId, Integer rating, String comment);
	
	/**
	 * View the review detail of customer
	 * @param bookingId
	 * @param accountId
	 * @return Optional
	 */
	Review viewReview(Integer bookingId, Integer accountId);
	
	/**
	 * Update customer review
	 * @param bookingId
	 * @param accountId
	 * @param rating
	 * @param comment
	 * @return
	 */
	boolean editReview(Integer bookingId, Integer accountId, Integer rating, String comment);
	
	/**
	 * Get the review list of one location
	 * @return List<Review>
	 */
	List<Review> listLocationReview (Integer facilityId);
	
	/**
	 * Get the review list of one user
	 * @param accountId
	 * @param dateFrom
	 * @param dateTo
	 * @param rating
	 * @return List<ReviewUserListDTO>
	 */
	List<ReviewUserListDTO> listUserReview (Integer accountId, LocalDate dateFrom, LocalDate dateTo, Integer rating);
	
	/**
	 * Find all the booking id in the Review table of user 
	 * @param accountId
	 * @return
	 */
	Set<Integer> getReviewedBookingIds(Integer accountId);
	
	/**
	 * Find facility in booking table
	 * @param accountId
	 * @param bookingId
	 * @return facility id in booking table
	 */
	Optional<Integer> getFacilityId(Integer accountId, Integer bookingId);
	
	/**
	 * Delete user review
	 * @param accountId
	 * @return boolean status
	 */
	boolean deleteReview(Integer bookingId, Integer accountId);
	
}
