package com.bcb.repository;

import java.util.Optional;
import java.util.List;

import com.bcb.model.Review;

public interface ReviewRepository {
	
	/**
	 * View the review detail of customer
	 * @param bookingId
	 * @param accountId
	 * @return Optional
	 */
	Optional<Review> viewReview(Integer bookingId, Integer accountId);
	
	/**
	 * Update custmer review
	 * @param bookingId
	 * @param accountId
	 * @param rating
	 * @param comment
	 * @return
	 */
	boolean editReview(Integer bookingId, Integer accountId, Integer rating, String comment);
	
	/**
	 * View the review list of one location
	 * @return
	 */
	List<Review> listLocationReview (Integer facilityId);
	
}
