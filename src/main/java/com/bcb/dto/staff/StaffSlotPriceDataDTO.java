package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffSlotPriceDataDTO {
    private String dayType;
    private List<StaffSlotPriceItemDTO> prices = new ArrayList<>();

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public List<StaffSlotPriceItemDTO> getPrices() {
        return prices;
    }

    public void setPrices(List<StaffSlotPriceItemDTO> prices) {
        this.prices = prices;
    }
}

