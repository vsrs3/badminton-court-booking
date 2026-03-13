package com.bcb.dto.owner;

import java.math.BigDecimal;
import java.util.List;

public class OwnerRevenueChartDTO {
    private List<String>     labels; 		// ["Mon","Tue",...] or ["Jan","Feb",...]
    private List<BigDecimal> data;    		// [4200, 5000, ...]  

    public OwnerRevenueChartDTO() {}

    public OwnerRevenueChartDTO(List<String> labels, List<BigDecimal> data) {
        this.labels = labels;
        this.data   = data;
    }

    public List<String>     getLabels() { return labels; }
    public List<BigDecimal> getData()   { return data; }

    public void setLabels(List<String> labels)         { this.labels = labels; }
    public void setData(List<BigDecimal> data)         { this.data   = data; }
}