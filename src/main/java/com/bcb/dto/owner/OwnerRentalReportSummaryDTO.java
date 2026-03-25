package com.bcb.dto.owner;

import java.util.ArrayList;
import java.util.List;

public class OwnerRentalReportSummaryDTO {

    private int facilityId;
    private String facilityName;
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private int selectedInactiveMonth;
    private List<OwnerRentalPointDTO> monthlyRevenue = new ArrayList<>();
    private List<OwnerRentalPointDTO> dailyRevenue = new ArrayList<>();
    private List<OwnerRentalPointDTO> hourlyRevenue = new ArrayList<>();
    private List<OwnerRentalTopItemDTO> topItems = new ArrayList<>();
    private List<OwnerRentalInactiveItemDTO> inactiveItems = new ArrayList<>();
    private OwnerRentalDetailsDTO details = new OwnerRentalDetailsDTO();

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(int selectedYear) {
        this.selectedYear = selectedYear;
    }

    public int getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(int selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(int selectedDay) {
        this.selectedDay = selectedDay;
    }

    public int getSelectedInactiveMonth() {
        return selectedInactiveMonth;
    }

    public void setSelectedInactiveMonth(int selectedInactiveMonth) {
        this.selectedInactiveMonth = selectedInactiveMonth;
    }

    public List<OwnerRentalPointDTO> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<OwnerRentalPointDTO> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public List<OwnerRentalPointDTO> getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(List<OwnerRentalPointDTO> dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public List<OwnerRentalPointDTO> getHourlyRevenue() {
        return hourlyRevenue;
    }

    public void setHourlyRevenue(List<OwnerRentalPointDTO> hourlyRevenue) {
        this.hourlyRevenue = hourlyRevenue;
    }

    public List<OwnerRentalTopItemDTO> getTopItems() {
        return topItems;
    }

    public void setTopItems(List<OwnerRentalTopItemDTO> topItems) {
        this.topItems = topItems;
    }

    public List<OwnerRentalInactiveItemDTO> getInactiveItems() {
        return inactiveItems;
    }

    public void setInactiveItems(List<OwnerRentalInactiveItemDTO> inactiveItems) {
        this.inactiveItems = inactiveItems;
    }

    public OwnerRentalDetailsDTO getDetails() {
        return details;
    }

    public void setDetails(OwnerRentalDetailsDTO details) {
        this.details = details;
    }
}
