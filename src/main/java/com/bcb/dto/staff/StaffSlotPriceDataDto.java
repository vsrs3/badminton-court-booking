package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffSlotPriceDataDto {
    private String dayType;
    private List<StaffSlotPriceItemDto> prices = new ArrayList<>();

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public List<StaffSlotPriceItemDto> getPrices() {
        return prices;
    }

    public void setPrices(List<StaffSlotPriceItemDto> prices) {
        this.prices = prices;
    }
}
