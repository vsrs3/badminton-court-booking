package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffBookingListDataDTO {
    private int page;
    private int size;
    private int totalRows;
    private int totalPages;
    private List<StaffBookingListItemDTO> bookings = new ArrayList<>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<StaffBookingListItemDTO> getBookings() {
        return bookings;
    }

    public void setBookings(List<StaffBookingListItemDTO> bookings) {
        this.bookings = bookings;
    }
}

