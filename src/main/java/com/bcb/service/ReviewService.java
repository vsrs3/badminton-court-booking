package com.bcb.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.dto.review.ReviewDTO;
import com.bcb.dto.review.ReviewUserListDTO;
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
	 * View location review list 
	 * @param dto
	 * @return List<Review>
	 */
	List<Review> listLocationReview(Integer facilityId);
	
	/**
	 * View list user review
	 * @param dto
	 * @return List<ReviewUserListDTO>
	 */
	List<ReviewUserListDTO> listUserReview (ReviewUserListDTO dto);
	
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
	//boolean deleteReview(ReviewDTO dto);
}
