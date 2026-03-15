package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusDataDTO {
    private String selectedDate;
    private List<StaffRentalStatusCourtDTO> courts = new ArrayList<>();
    private List<StaffTimelineSlotDTO> slots = new ArrayList<>();
    private List<StaffRentalStatusCellDTO> cells = new ArrayList<>();
    private List<StaffRentalInventoryStockDTO> inventoryItems = new ArrayList<>();

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public List<StaffRentalStatusCourtDTO> getCourts() {
        return courts;
    }

    public void setCourts(List<StaffRentalStatusCourtDTO> courts) {
        this.courts = courts;
    }

    public List<StaffTimelineSlotDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<StaffTimelineSlotDTO> slots) {
        this.slots = slots;
    }

    public List<StaffRentalStatusCellDTO> getCells() {
        return cells;
    }

    public void setCells(List<StaffRentalStatusCellDTO> cells) {
        this.cells = cells;
    }

    public List<StaffRentalInventoryStockDTO> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<StaffRentalInventoryStockDTO> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }
}
