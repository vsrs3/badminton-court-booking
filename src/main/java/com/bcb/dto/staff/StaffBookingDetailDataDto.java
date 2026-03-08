package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailDataDto {
    private int bookingId;
    private String bookingDate;
    private String bookingStatus;
    private String createdAt;
    private String customerName;
    private String customerPhone;
    private String customerType;
    private List<StaffBookingDetailSessionDto> sessions = new ArrayList<>();
    private List<StaffBookingDetailSlotDto> slots = new ArrayList<>();
    private StaffBookingDetailInvoiceDto invoice;
    private String etag;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public List<StaffBookingDetailSessionDto> getSessions() {
        return sessions;
    }

    public void setSessions(List<StaffBookingDetailSessionDto> sessions) {
        this.sessions = sessions;
    }

    public List<StaffBookingDetailSlotDto> getSlots() {
        return slots;
    }

    public void setSlots(List<StaffBookingDetailSlotDto> slots) {
        this.slots = slots;
    }

    public StaffBookingDetailInvoiceDto getInvoice() {
        return invoice;
    }

    public void setInvoice(StaffBookingDetailInvoiceDto invoice) {
        this.invoice = invoice;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
