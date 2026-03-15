package com.bcb.dto.staff;

import java.util.ArrayList;
import java.util.List;

public class StaffRentalStatusCourtDTO {
    private int courtId;
    private String courtName;
    private List<StaffRentalStatusRowDTO> rows = new ArrayList<>();

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public List<StaffRentalStatusRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<StaffRentalStatusRowDTO> rows) {
        this.rows = rows;
    }
}
