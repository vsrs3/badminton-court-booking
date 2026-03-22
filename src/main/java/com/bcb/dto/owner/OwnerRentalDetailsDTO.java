package com.bcb.dto.owner;

import java.util.ArrayList;
import java.util.List;

public class OwnerRentalDetailsDTO {

    private String scope;
    private String title;
    private List<OwnerRentalDetailRowDTO> rows = new ArrayList<>();

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<OwnerRentalDetailRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<OwnerRentalDetailRowDTO> rows) {
        this.rows = rows;
    }
}
