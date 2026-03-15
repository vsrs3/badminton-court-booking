package com.bcb.dto.owner;

import java.util.List;

public class PeakHourDashboardDTO {

    private List<PeakHourSlotDTO> heatmap;

    // Danh sách khung giờ cao điểm và thấp điểm
    private List<String> peakSlots;       // ["17:00", "17:30", "18:00"]
    private List<String> lowSlots;        // ["06:00", "06:30"]

    // Khoảng bình thường — không gắn giờ cụ thể
    private String normalTimeRange;       // "08:00 – 17:00"

    public PeakHourDashboardDTO() {}

    public List<PeakHourSlotDTO> getHeatmap()        { return heatmap; }
    public List<String>          getPeakSlots()      { return peakSlots; }
    public List<String>          getLowSlots()       { return lowSlots; }
    public String                getNormalTimeRange() { return normalTimeRange; }

    public void setHeatmap(List<PeakHourSlotDTO> v)   { this.heatmap         = v; }
    public void setPeakSlots(List<String> v)           { this.peakSlots       = v; }
    public void setLowSlots(List<String> v)            { this.lowSlots        = v; }
    public void setNormalTimeRange(String v)           { this.normalTimeRange = v; }
}