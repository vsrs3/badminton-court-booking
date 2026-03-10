package com.bcb.dto.review;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReviewUserListDTO {
	
	private Integer accountId;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	private Integer rating;
	
	private Integer reviewId;
	private Integer bookingId;
	private String comment;
	LocalDateTime createdAt;
	
	//Facility information
    private String name;
    private String province;
    private String district;
    private String ward;
    private String address;
   
    
	public ReviewUserListDTO(Integer accountId, LocalDate dateFrom, LocalDate dateTo, Integer rating) {
		super();
		this.accountId = accountId;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.rating = rating;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getWard() {
		return ward;
	}

	public void setWard(String ward) {
		this.ward = ward;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}
}
