package com.bcb.service.impl;

import java.util.List;
import java.util.Optional;

import com.bcb.dto.ReviewDTO;
import com.bcb.model.Review;
import com.bcb.repository.ReviewRepository;
import com.bcb.repository.impl.ReviewRepositoryImpl;
import com.bcb.service.ReviewService;

public class ReviewServiceImpl implements ReviewService{
	
	private ReviewRepository repoReview = new ReviewRepositoryImpl();

	@Override
	public Optional<Review> viewReview(ReviewDTO dto) {
		if(dto.getBookingId() == null || dto.getAccountId() == null) {
			throw new IllegalArgumentException("Account ID or Booking ID cannot be null");
		}
		
		return repoReview.viewReview(dto.getBookingId(), dto.getAccountId());
	}

	@Override
	public boolean editReview(ReviewDTO dto) {
		if(dto.getBookingId() == null || dto.getAccountId() == null 
				|| dto.getFacilityId() == null || dto.getRating() == null) {
			throw new IllegalArgumentException("Account ID or Booking ID cannot be null");
		}
		if(dto.getComment() == null || dto.getComment().isEmpty()) {
			throw new IllegalArgumentException("Rating or Comment cannot be null");
		}
		
		return repoReview.editReview(dto.getBookingId(), dto.getAccountId(), dto.getRating(), dto.getComment());
	}

	@Override
	public List<Review> listLocationReview(ReviewDTO dto) {
		if(dto.getFacilityId() == null || dto.getRating() == null) {
			throw new IllegalArgumentException("Account ID or Booking ID cannot be null");
		}
		
		return repoReview.listLocationReview(dto.getFacilityId());
	}

}
