package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusDataDTO {
    private List<StaffRentalStatusCourtDTO> courts = new ArrayList<>();

    public List<StaffRentalStatusCourtDTO> getCourts() {
        return courts;
    }

    public void setCourts(List<StaffRentalStatusCourtDTO> courts) {
        this.courts = courts;
    }
}
