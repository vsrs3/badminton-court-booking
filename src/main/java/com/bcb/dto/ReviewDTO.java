package com.bcb.dto;

public class ReviewDTO {
	
	private Integer bookingId, facilityId, accountId, rating;
	private String comment;
	
	//list location review
	public ReviewDTO(Integer facilityId) {
		super();
		this.facilityId = facilityId;
	}
	
	//view, create and edit review
	public ReviewDTO(Integer bookingId, Integer accountId, Integer rating,
			String comment) {
		super();
		this.bookingId = bookingId;
		this.accountId = accountId;
		this.rating = rating;
		this.comment = comment;
	}

	public Integer getBookingId() {
		return bookingId;
	}

	public void setBookingId(Integer bookingId) {
		this.bookingId = bookingId;
	}

	public Integer getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Integer facilityId) {
		this.facilityId = facilityId;
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
