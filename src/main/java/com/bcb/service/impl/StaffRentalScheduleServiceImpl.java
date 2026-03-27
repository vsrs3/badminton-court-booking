package com.bcb.service.impl;

import com.bcb.dto.staff.InventoryRentalScheduleSaveItemDTO;
import com.bcb.dto.staff.StaffRentalInventoryItemDTO;
import com.bcb.repository.impl.StaffRentalScheduleRepositoryImpl;
import com.bcb.repository.staff.StaffRentalScheduleRepository;
import com.bcb.service.staff.StaffRentalScheduleService;
import com.bcb.utils.DBContext;
import com.bcb.utils.api.JsonResponseUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaffRentalScheduleServiceImpl implements StaffRentalScheduleService {

    private final StaffRentalScheduleRepository repository = new StaffRentalScheduleRepositoryImpl();

    /**
     * Returns rental inventory for a slot with paging and selected items.
     */
    @Override
    public String getSlotInventoryJson(
            int facilityId,
            String bookingDate,
            int courtId,
            int slotId,
            String keyword,
            String priceSort,
            int page,
            int pageSize
    ) throws Exception {
        LocalDate date = parseBookingDate(bookingDate);
        String normalizedPriceSort = normalizePriceSort(priceSort);

        if (courtId <= 0 || slotId <= 0) {
            throw new IllegalArgumentException("Thiếu thông tin sân hoặc slot.");
        }

        int total = repository.countRentalItems(facilityId, keyword);
        int totalPages = Math.max(1, (int) Math.ceil(total * 1.0 / pageSize));
        int normalizedPage = Math.min(Math.max(page, 1), totalPages);

        List<StaffRentalInventoryItemDTO> items = repository.findRentalItemsForSlot(
                facilityId, date, courtId, slotId, keyword, normalizedPriceSort, normalizedPage, pageSize
        );
        List<StaffRentalInventoryItemDTO> suggestionItems = repository.findRentalItemsForSlot(
                facilityId, date, courtId, slotId, keyword, normalizedPriceSort, 1, 50
        );
        List<StaffRentalInventoryItemDTO> selectedItems = repository.findSelectedItemsForSlot(
                facilityId, date, courtId, slotId
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("page", normalizedPage);
        data.put("pageSize", pageSize);
        data.put("total", total);
        data.put("totalPages", totalPages);
        data.put("priceSort", normalizedPriceSort);
        data.put("items", items);
        data.put("suggestionItems", suggestionItems);
        data.put("selectedItems", selectedItems);

        return JsonResponseUtil.success("Tải danh sách đồ thuê thành công", data);
    }

    /**
     * Saves per-slot rental schedule by replacing existing rows for the slot.
     */
    @Override
    public String saveSlotRentalSchedule(String body, int facilityId) throws Exception {
        JSONObject json = new JSONObject(body);
        LocalDate bookingDate = parseBookingDate(json.optString("bookingDate", null));
        int courtId = json.optInt("courtId", 0);
        int slotId = json.optInt("slotId", 0);

        if (courtId <= 0 || slotId <= 0) {
            throw new IllegalArgumentException("Thiếu thông tin sân hoặc slot.");
        }

        JSONArray itemsJson = json.optJSONArray("items");
        List<InventoryRentalScheduleSaveItemDTO> items = new ArrayList<>();
        if (itemsJson != null) {
            for (int i = 0; i < itemsJson.length(); i++) {
                JSONObject itemJson = itemsJson.optJSONObject(i);
                if (itemJson == null) {
                    continue;
                }

                int inventoryId = itemJson.optInt("inventoryId", 0);
                int quantity = itemJson.optInt("quantity", 0);
                // Validate quantities before saving schedule rows.
                if (inventoryId <= 0 || quantity <= 0) {
                    continue;
                }

                InventoryRentalScheduleSaveItemDTO item = new InventoryRentalScheduleSaveItemDTO();
                item.setInventoryId(inventoryId);
                item.setQuantity(quantity);
                items.add(item);
            }
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                /*
                 * Rental schedule save flow:
                 * - Delete existing rows for the slot.
                 * - Insert new rows for selected items.
                 */
                repository.replaceRentalSchedule(conn, facilityId, bookingDate, courtId, slotId, items);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        List<StaffRentalInventoryItemDTO> selectedItems = repository.findSelectedItemsForSlot(
                facilityId, bookingDate, courtId, slotId
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("selectedItems", selectedItems);
        return JsonResponseUtil.success("Lưu lịch đồ thuê thành công", data);
    }

    private LocalDate parseBookingDate(String bookingDate) {
        if (bookingDate == null || bookingDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu ngày đặt sân.");
        }
        return LocalDate.parse(bookingDate.trim());
    }

    private String normalizePriceSort(String value) {
        if ("price_desc".equals(value) || "price_asc".equals(value)) {
            return value;
        }
        return "default";
    }
}
