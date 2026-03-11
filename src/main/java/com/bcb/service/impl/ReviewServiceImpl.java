package com.bcb.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bcb.dto.review.ReviewDTO;
import com.bcb.dto.review.ReviewUserListDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.Review;
import com.bcb.repository.ReviewRepository;
import com.bcb.repository.impl.ReviewRepositoryImpl;
import com.bcb.service.ReviewService;

public class ReviewServiceImpl implements ReviewService {

	// repository
	private ReviewRepository repoReview = new ReviewRepositoryImpl();

	@Override
	public Review viewReview(ReviewDTO dto) {
		if (dto.getBookingId() == null || dto.getAccountId() == null) {
			throw new IllegalArgumentException("Account ID or Booking ID không được null");
		}

		return repoReview.viewReview(dto.getBookingId(), dto.getAccountId());
	}

	
	
	@Override
	public boolean editReview(ReviewDTO dto) {
		if (dto.getBookingId() == null || dto.getAccountId() == null
				|| dto.getRating() == null) {
			throw new IllegalArgumentException("Account ID or Booking ID không được null");
		}
		if (dto.getComment() == null || dto.getComment().isEmpty()) {
			throw new IllegalArgumentException("Rating or Comment không được null");
		}

		return repoReview.editReview(dto.getBookingId(), dto.getAccountId(), dto.getRating(), dto.getComment());
	}

	

	
	@Override
	public List<ReviewUserListDTO> listUserReview(ReviewUserListDTO dto) {
		if(dto.getAccountId() == null) {
			throw new DataAccessException("Account ID không thể null");
		}
		
		return repoReview.listUserReview(dto.getAccountId(), dto.getDateFrom(), dto.getDateTo());
	}

	
	
	@Override
	public List<Review> listLocationReview(Integer facilityId) {
		if (facilityId == null) {
			throw new IllegalArgumentException("Facility ID không được null");
		}

		return repoReview.listLocationReview(facilityId);
	}

	
	
	@Override
	public boolean addReview(ReviewDTO dto) {
		if (dto.getBookingId() == null || dto.getAccountId() == null|| dto.getRating() == null) {
			throw new IllegalArgumentException("Account ID, Booking ID, Điểm đánh giá không thể null");
		}

		if (dto.getComment().length() < 0 && dto.getComment().length() > 500) {
			throw new IllegalArgumentException("Đánh giá không được quá 500 kí tự");
		}

		return repoReview.addReview(dto.getBookingId(), dto.getAccountId(), dto.getRating(), dto.getComment());
	}

	
	
	@Override
	public Set<Integer> getReviewedBookingIds(Integer accountId) {
	    if (accountId == null)
	        throw new IllegalArgumentException("Account ID không được null");
	    return repoReview.getReviewedBookingIds(accountId);
	}

	
	
	@Override
	public Integer getFacilityIdFromBooking(Integer accountId, Integer bookingId) {

		if (accountId == null || bookingId == null)
			throw new IllegalArgumentException("Account ID or Booking ID không được null");

		return repoReview.getFacilityId(accountId, bookingId)
				.orElseThrow(() -> new DataAccessException("Không tìm thấy Facility ID"));
	}

	
	
	/*
	 * @Override public boolean deleteReview(ReviewDTO dto) {
	 * 
	 * return repoReview.deleteReview(dto.getBookingId(), dto.getAccountId()); }
	 */
	
}
