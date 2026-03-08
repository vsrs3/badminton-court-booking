package com.bcb.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bcb.dto.ReviewDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.model.Review;

public interface ReviewService {
	
	/**
	 * View review customer
	 * @param dto
	 * @return optional
	 */
	Review viewReview(ReviewDTO dto);
	
	/**
	 * Edit review customer
	 * @param dto
	 * @return boolean
	 */
	boolean editReview(ReviewDTO dto);
	
	/**
	 * View review list location
	 * @param dto
	 * @return List<Review>
	 */
	List<Review> listLocationReview(Integer facilityId);
	
	/**
	 * Add review customer
	 * @param dto
	 * @return boolean
	 */
	boolean addReview(ReviewDTO dto);
	
	
	/**
	 * Find existed booking Id in Review table DB
	 * @param accountId
	 * @return Set<bookingId> in Review table
	 */
	Set<Integer> getReviewedBookingIds(Integer accountId);
	
	/**
	 * Find facility ID
	 * @param accountId
	 * @param bookingId
	 * @return facility ID in Booking table
	 */
	Integer getFacilityIdFromBooking (Integer accountId, Integer bookingId);
	
	/**
	 * Delete user review
	 * @param dto
	 * @return boolean status
	 */
	boolean deleteReview(ReviewDTO dto);
}
