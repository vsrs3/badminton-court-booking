package com.bcb.service.owner.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bcb.dto.owner.PeakHourDashboardDTO;
import com.bcb.dto.owner.PeakHourSlotDTO;
import com.bcb.repository.owner.DashboardPeakHourRepository;
import com.bcb.repository.owner.impl.DashboardPeakHourRepositoryImpl;
import com.bcb.service.owner.DashboardPeakHourService;
import com.google.gson.Gson;

public class DashboardPeakHourServiceImpl implements DashboardPeakHourService {

    private final DashboardPeakHourRepository repo = new DashboardPeakHourRepositoryImpl();
    private final Gson gson = new Gson();

    @Override
    public String getPeakHourJson() {

        // ── 1. Lấy dữ liệu từ repository ──────────────────────────
        List<PeakHourSlotDTO> rawHeatmap      = repo.getHeatmapData();
        List<PeakHourSlotDTO> classifiedSlots = repo.getClassifiedSlots();
        List<String>          allSlotTimes    = repo.getAllSlotTimes();

        // ── 2. Build map: slotTime → slotType (từ classified data) ─
        //    classifiedSlots chứa tất cả slot 05:00–22:00 với type đã tính
        Map<String, String> typeMap = new HashMap<>();
        for (PeakHourSlotDTO s : classifiedSlots) {
            typeMap.put(s.getSlotTime(), s.getSlotType());
        }

        // ── 3. Gán slotType vào rawHeatmap trước khi pad ──────────
        //    rawHeatmap chỉ chứa slot có booking thực tế (không có NO_DATA)
        for (PeakHourSlotDTO s : rawHeatmap) {
            String type = typeMap.getOrDefault(s.getSlotTime(), "NORMAL");
            s.setSlotType(type);
        }

        // ── 4. Pad heatmap đủ 7 ngày × tất cả slot ────────────────
        //    Slot thiếu (không có booking ngày đó) → NO_DATA
        List<PeakHourSlotDTO> heatmap = padHeatmap(rawHeatmap, allSlotTimes);

        // ── 5. Phân nhóm slot theo loại ───────────────────────────
        List<String> peakSlots   = new ArrayList<>();
        List<String> lowSlots    = new ArrayList<>();
        List<String> normalSlots = new ArrayList<>();

        for (PeakHourSlotDTO slot : classifiedSlots) {
            String type = slot.getSlotType();
            if (type == null) continue;
            switch (type) {
                case "PEAK"   -> peakSlots.add(slot.getSlotTime());
                case "LOW"    -> lowSlots.add(slot.getSlotTime());
                case "NORMAL" -> normalSlots.add(slot.getSlotTime());
                // NO_DATA không cần thêm vào danh sách
            }
        }

        // ── 6. Tạo chuỗi mô tả khoảng bình thường ─────────────────
        //    Không gắn giờ cụ thể — chỉ hiển thị khoảng đầu→cuối
        String normalTimeRange;
        if (normalSlots.isEmpty()) {
            normalTimeRange = "Không xác định";
        } else {
            normalTimeRange = normalSlots.get(0)
                            + " – "
                            + normalSlots.get(normalSlots.size() - 1);
        }

        // ── 7. Build và serialize DTO ──────────────────────────────
        PeakHourDashboardDTO dto = new PeakHourDashboardDTO();
        dto.setHeatmap(heatmap);
        dto.setPeakSlots(peakSlots);
        dto.setLowSlots(lowSlots);
        dto.setNormalTimeRange(normalTimeRange);

        return gson.toJson(dto);
    }

    /**
     * Pad heatmap đủ 7 ngày × tất cả slot trong allSlots.
     * - Slot có booking thực tế (trong rawHeatmap) → giữ nguyên, slotType đã được gán.
     * - Slot không có booking cho ngày đó → tạo mới với bookingCount=0, pct=0, slotType=NO_DATA.
     *
     * Lưu ý: slotType=NO_DATA nghĩa là "không có booking trong ngày cụ thể đó",
     * khác với slotType=LOW (có booking nhưng ít hơn bình thường).
     */
    private List<PeakHourSlotDTO> padHeatmap(List<PeakHourSlotDTO> raw,
                                              List<String> allSlots) {
        // Build lookup map: "dayOfWeek-slotTime" → dto
        Map<String, PeakHourSlotDTO> map = new java.util.LinkedHashMap<>();
        for (PeakHourSlotDTO s : raw) {
            map.put(s.getDayOfWeek() + "-" + s.getSlotTime(), s);
        }

        List<PeakHourSlotDTO> result = new ArrayList<>();
        for (int d = 1; d <= 7; d++) {
            for (String time : allSlots) {
                String key = d + "-" + time;
                if (map.containsKey(key)) {
                    result.add(map.get(key));
                } else {
                    // Ngày này không có booking cho slot này → NO_DATA
                    result.add(new PeakHourSlotDTO(d, time, 0, BigDecimal.ZERO, "NO_DATA"));
                }
            }
        }
        return result;
    }
}