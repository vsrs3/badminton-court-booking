package com.bcb.dto.singlebooking;

import java.util.List;
import java.util.Map;

/**
 * Full matrix-data response DTO for the single-booking matrix endpoint.
 *
 * @author AnhTN
 */
public class SingleBookingMatrixResponseDTO {

    private SingleBookingFacilityDTO facility;
    private String bookingDate;
    private String serverDate;
    private String minSelectableDate;
    private List<SingleBookingMatrixCourtDTO> courts;
    private List<SingleBookingMatrixTimeSlotDTO> slots;
    /** courtId -> list of booked slotIds */
    private Map<Integer, List<Integer>> booked;
    /** courtId -> list of disabled slotIds */
    private Map<Integer, List<Integer>> disabled;
    private List<SingleBookingMatrixSlotPriceDTO> prices;

    public SingleBookingMatrixResponseDTO() {}

    public SingleBookingFacilityDTO getFacility() { return facility; }
    public void setFacility(SingleBookingFacilityDTO facility) { this.facility = facility; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getServerDate() { return serverDate; }
    public void setServerDate(String serverDate) { this.serverDate = serverDate; }

    public String getMinSelectableDate() { return minSelectableDate; }
    public void setMinSelectableDate(String minSelectableDate) { this.minSelectableDate = minSelectableDate; }

    public List<SingleBookingMatrixCourtDTO> getCourts() { return courts; }
    public void setCourts(List<SingleBookingMatrixCourtDTO> courts) { this.courts = courts; }

    public List<SingleBookingMatrixTimeSlotDTO> getSlots() { return slots; }
    public void setSlots(List<SingleBookingMatrixTimeSlotDTO> slots) { this.slots = slots; }

    public Map<Integer, List<Integer>> getBooked() { return booked; }
    public void setBooked(Map<Integer, List<Integer>> booked) { this.booked = booked; }

    public Map<Integer, List<Integer>> getDisabled() { return disabled; }
    public void setDisabled(Map<Integer, List<Integer>> disabled) { this.disabled = disabled; }

    public List<SingleBookingMatrixSlotPriceDTO> getPrices() { return prices; }
    public void setPrices(List<SingleBookingMatrixSlotPriceDTO> prices) { this.prices = prices; }
}

