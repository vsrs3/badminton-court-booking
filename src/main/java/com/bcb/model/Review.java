package com.bcb.model;

import java.time.LocalDateTime;

public class Review {
	private Integer reviewId, bookingId, accountId, rating;
	private String comment;
	private LocalDateTime createdAt;	
	
	public Review() {
		super();
	}

	public Review(Integer reviewId, Integer bookingId, Integer accountId, Integer rating, String comment,
			LocalDateTime createdAt) {
		super();
		this.reviewId = reviewId;
		this.bookingId = bookingId;
		this.accountId = accountId;
		this.rating = rating;
		this.comment = comment;
		this.createdAt = createdAt;
	}

	public Integer getReviewId() {
		return reviewId;
	}

	public void setReviewId(Integer reviewId) {
		this.reviewId = reviewId;
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public void setBookingId(Integer bookingId) {
		this.bookingId = bookingId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	
}
