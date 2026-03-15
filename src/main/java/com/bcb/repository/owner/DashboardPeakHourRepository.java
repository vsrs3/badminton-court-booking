package com.bcb.repository.owner;

import com.bcb.dto.owner.PeakHourSlotDTO;
import java.util.List;

public interface DashboardPeakHourRepository {
	
    // Heatmap: 7 ngày × tất cả slot 05:00–22:00
    List<PeakHourSlotDTO> getHeatmapData();

    // Danh sách slot đã được phân loại PEAK/LOW/NORMAL/NO_DATA
    List<PeakHourSlotDTO> getClassifiedSlots();

    // Tất cả slot_time trong khung 05:00–22:00 (để pad heatmap)
    List<String> getAllSlotTimes();
}