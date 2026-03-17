package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusCellDTO {
    private int courtId;
    private int slotId;
    private int bookingId;
    private String customerName;
    private String customerKey;
    private String status;
    private int totalQuantity;
    private int itemCount;
    private List<StaffRentalStatusItemDTO> items = new ArrayList<>();

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public List<StaffRentalStatusItemDTO> getItems() {
        return items;
    }

    public void setItems(List<StaffRentalStatusItemDTO> items) {
        this.items = items;
    }
}
