package com.bcb.service;

import java.util.List;
import java.util.Optional;

import com.bcb.dto.ReviewDTO;
import com.bcb.model.Review;

public interface ReviewService {
	
	Optional<Review> viewReview(ReviewDTO dto);
	
	boolean editReview(ReviewDTO dto);
	
	List<Review> listLocationReview(ReviewDTO dto);
}
