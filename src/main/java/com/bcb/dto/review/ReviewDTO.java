package com.bcb.dto.review;

public class ReviewDTO {
	
	private Integer bookingId, accountId, rating;
	private String comment;
	
	// edit, create review
	public ReviewDTO(Integer bookingId, Integer accountId, Integer rating,
			String comment) {
		super();
		this.bookingId = bookingId;
		this.accountId = accountId;
		this.rating = rating;
		this.comment = comment;
	}
	
	// view, list review
	public ReviewDTO(Integer bookingId, Integer accountId) {
		super();
		this.bookingId = bookingId;
		this.accountId = accountId;
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
}
