package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailDataDTO {
    private int bookingId;
    private String bookingDate;
    private String bookingStatus;
    private String createdAt;
    private String customerName;
    private String customerPhone;
    private String customerType;
    private List<StaffBookingDetailSessionDTO> sessions = new ArrayList<>();
    private List<StaffBookingDetailSlotDTO> slots = new ArrayList<>();
    private StaffBookingDetailInvoiceDTO invoice;
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

    public List<StaffBookingDetailSessionDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<StaffBookingDetailSessionDTO> sessions) {
        this.sessions = sessions;
    }

    public List<StaffBookingDetailSlotDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<StaffBookingDetailSlotDTO> slots) {
        this.slots = slots;
    }

    public StaffBookingDetailInvoiceDTO getInvoice() {
        return invoice;
    }

    public void setInvoice(StaffBookingDetailInvoiceDTO invoice) {
        this.invoice = invoice;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}

