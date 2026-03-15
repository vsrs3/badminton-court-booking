package com.bcb.dto.owner;

import java.math.BigDecimal;

public class OwnerOccupancyRateChartDTO {
	private BigDecimal day; // % hôm nay
	private BigDecimal week; // % tuần này
	private BigDecimal month; // % tháng này
	private BigDecimal year; // % năm này

	public OwnerOccupancyRateChartDTO() {
		this.day = BigDecimal.ZERO;
		this.week = BigDecimal.ZERO;
		this.month = BigDecimal.ZERO;
		this.year = BigDecimal.ZERO;
	}

	public OwnerOccupancyRateChartDTO(BigDecimal day, BigDecimal week, BigDecimal month, BigDecimal year) {
		this.day = day != null ? day : BigDecimal.ZERO;
		this.week = week != null ? week : BigDecimal.ZERO;
		this.month = month != null ? month : BigDecimal.ZERO;
		this.year = year != null ? year : BigDecimal.ZERO;
	}

	public BigDecimal getDay() {
		return day;
	}

	public BigDecimal getWeek() {
		return week;
	}

	public BigDecimal getMonth() {
		return month;
	}

	public BigDecimal getYear() {
		return year;
	}

	public void setDay(BigDecimal day) {
		this.day = day;
	}

	public void setWeek(BigDecimal week) {
		this.week = week;
	}

	public void setMonth(BigDecimal month) {
		this.month = month;
	}

	public void setYear(BigDecimal year) {
		this.year = year;
	}
}
