package com.bcb.dto.mybooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for individual slot detail within a booking detail view.
 */
public class BookingSlotDetailDTO {
    private int bookingSlotId;
    private String courtName;
    private String startTime;       // HH:mm
    private String endTime;         // HH:mm
    private BigDecimal price;
    private String slotStatus;      // PENDING, CHECKED_IN, CHECK_OUT, NO_SHOW, CANCELLED
    private LocalDate bookingDate;
    private BigDecimal rentalTotal = BigDecimal.ZERO;
    private List<BookingSlotRentalItemDTO> rentalItems = new ArrayList<>();

    public BookingSlotDetailDTO() {}

    public int getBookingSlotId() { return bookingSlotId; }
    public void setBookingSlotId(int bookingSlotId) { this.bookingSlotId = bookingSlotId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getSlotStatus() { return slotStatus; }
    public void setSlotStatus(String slotStatus) { this.slotStatus = slotStatus; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getRentalTotal() { return rentalTotal; }
    public void setRentalTotal(BigDecimal rentalTotal) { this.rentalTotal = rentalTotal; }

    public List<BookingSlotRentalItemDTO> getRentalItems() { return rentalItems; }
    public void setRentalItems(List<BookingSlotRentalItemDTO> rentalItems) { this.rentalItems = rentalItems; }
}

