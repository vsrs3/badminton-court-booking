package com.bcb.service.impl;

import com.bcb.utils.staff.StaffBookingSnapshotTokenUtil;
import com.bcb.dto.staff.StaffBookingDetailDataDTO;
import com.bcb.dto.staff.StaffBookingDetailHeaderDTO;
import com.bcb.dto.staff.StaffBookingDetailInvoiceDTO;
import com.bcb.dto.staff.StaffBookingDetailRentalRowDTO;
import com.bcb.dto.staff.StaffBookingDetailSessionDTO;
import com.bcb.dto.staff.StaffBookingDetailSlotDTO;
import com.bcb.repository.impl.StaffBookingDetailRepositoryImpl;
import com.bcb.repository.staff.StaffBookingDetailRepository;
import com.bcb.service.staff.StaffBookingDetailService;
import com.bcb.utils.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class StaffBookingDetailServiceImpl implements StaffBookingDetailService {

    private final StaffBookingDetailRepository repository = new StaffBookingDetailRepositoryImpl();

    @Override
    public StaffBookingDetailDataDTO getBookingDetail(int bookingId, int staffFacilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            StaffBookingDetailHeaderDTO header = repository.findBookingHeader(conn, bookingId);
            if (header == null || header.getFacilityId() != staffFacilityId) {
                return null;
            }

            List<StaffBookingDetailSlotDTO> allSlots = repository.findBookingSlots(conn, bookingId);
            List<List<StaffBookingDetailSlotDTO>> grouped = groupIntoSessions(allSlots);

            StaffBookingDetailDataDTO data = new StaffBookingDetailDataDTO();
            data.setBookingId(header.getBookingId());
            data.setBookingDate(header.getBookingDate());
            data.setBookingStatus(header.getBookingStatus());
            data.setCreatedAt(header.getCreatedAt());
            data.setCustomerName(header.getCustomerName());
            data.setCustomerPhone(header.getCustomerPhone());
            data.setCustomerType(header.getCustomerType());
            data.setSlots(allSlots);

            // Invoice
            StaffBookingDetailInvoiceDTO invoice = repository.findInvoice(conn, bookingId);
            data.setInvoice(invoice);

            // Sessions
            List<StaffBookingDetailSessionDTO> sessions = new ArrayList<>();
            for (int i = 0; i < grouped.size(); i++) {
                sessions.add(buildSessionDto(i, grouped.get(i)));
            }
            data.setSessions(sessions);

            // Rental rows
            List<StaffBookingDetailRentalRowDTO> rawRentalRows = repository.findBookingRentalRows(conn, bookingId);
            List<StaffBookingDetailRentalRowDTO> rentalRows = buildRentalRows(rawRentalRows);
            data.setRentalRows(rentalRows);

            // Rental total
            BigDecimal rentalTotal = calculateRentalTotal(rentalRows);
            data.setRentalTotal(rentalTotal);

            // Grand total = tiền sân + tiền thuê đồ
            BigDecimal courtTotal = invoice != null && invoice.getTotalAmount() != null
                    ? invoice.getTotalAmount()
                    : BigDecimal.ZERO;
            data.setGrandTotal(courtTotal.add(rentalTotal));

            // Snapshot / Etag
            StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, staffFacilityId);
            data.setEtag(snapshot != null ? StaffBookingSnapshotTokenUtil.computeEtag(snapshot) : null);

            return data;
        }
    }

    private StaffBookingDetailSessionDTO buildSessionDto(int index, List<StaffBookingDetailSlotDTO> session) {
        String sessionStatus = deriveSessionStatus(session);

        List<StaffBookingDetailSlotDTO> activeSlots = new ArrayList<>();
        for (StaffBookingDetailSlotDTO slot : session) {
            if (!"CANCELLED".equals(slot.getSlotStatus())) {
                activeSlots.add(slot);
            }
        }

        List<StaffBookingDetailSlotDTO> displaySlots = activeSlots.isEmpty() ? session : activeSlots;
        StaffBookingDetailSlotDTO first = displaySlots.get(0);
        StaffBookingDetailSlotDTO last = displaySlots.get(displaySlots.size() - 1);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (StaffBookingDetailSlotDTO slot : activeSlots) {
            if (slot.getPrice() != null) {
                totalPrice = totalPrice.add(slot.getPrice());
            }
        }

        int displaySlotCount = activeSlots.isEmpty() ? session.size() : activeSlots.size();

        StaffBookingDetailSessionDTO dto = new StaffBookingDetailSessionDTO();
        dto.setSessionIndex(index);
        dto.setCourtId(first.getCourtId());
        dto.setCourtName(first.getCourtName());
        dto.setStartTime(first.getStartTime());
        dto.setEndTime(last.getEndTime());
        dto.setSlotCount(displaySlotCount);
        dto.setTotalPrice(totalPrice);
        dto.setSessionStatus(sessionStatus);
        dto.setCheckinTime(first.getCheckinTime());
        dto.setCheckoutTime(last.getCheckoutTime());

        List<Integer> ids = new ArrayList<>();
        for (StaffBookingDetailSlotDTO slot : session) {
            ids.add(slot.getBookingSlotId());
        }
        dto.setBookingSlotIds(ids);
        dto.setBookingSlots(session);

        return dto;
    }

    /**
     * Tính tổng tiền thuê đồ từ danh sách rental rows đã gộp.
     */
    private BigDecimal calculateRentalTotal(List<StaffBookingDetailRentalRowDTO> rentalRows) {
        BigDecimal total = BigDecimal.ZERO;
        if (rentalRows == null) {
            return total;
        }

        for (StaffBookingDetailRentalRowDTO row : rentalRows) {
            if (row.getRentalTotal() != null) {
                total = total.add(row.getRentalTotal());
            }
        }
        return total;
    }

    /**
     * Gộp các dòng thuê đồ theo rule:
     * - cùng sân
     * - slot liền kề
     * - danh sách đồ thuê giống nhau
     * thì gộp thành 1 dòng
     *
     * rawRows đầu vào nên được repository trả về theo thứ tự:
     * court_name ASC, start_time ASC
     */
    private List<StaffBookingDetailRentalRowDTO> buildRentalRows(List<StaffBookingDetailRentalRowDTO> rawRows) {
        List<StaffBookingDetailRentalRowDTO> result = new ArrayList<>();
        if (rawRows == null || rawRows.isEmpty()) {
            return result;
        }

        StaffBookingDetailRentalRowDTO current = cloneRentalRow(rawRows.get(0));

        for (int i = 1; i < rawRows.size(); i++) {
            StaffBookingDetailRentalRowDTO next = rawRows.get(i);

            boolean sameCourt = safeEquals(current.getCourtName(), next.getCourtName());
            boolean consecutive = safeEquals(current.getEndTime(), next.getStartTime());
            boolean sameItems = safeEquals(current.getRentalItemsText(), next.getRentalItemsText());

            if (sameCourt && consecutive && sameItems) {
                current.setEndTime(next.getEndTime());

                BigDecimal currTotal = current.getRentalTotal() != null
                        ? current.getRentalTotal()
                        : BigDecimal.ZERO;
                BigDecimal nextTotal = next.getRentalTotal() != null
                        ? next.getRentalTotal()
                        : BigDecimal.ZERO;

                current.setRentalTotal(currTotal.add(nextTotal));
            } else {
                result.add(current);
                current = cloneRentalRow(next);
            }
        }

        result.add(current);
        return result;
    }

    private StaffBookingDetailRentalRowDTO cloneRentalRow(StaffBookingDetailRentalRowDTO src) {
        StaffBookingDetailRentalRowDTO dto = new StaffBookingDetailRentalRowDTO();
        dto.setCourtName(src.getCourtName());
        dto.setStartTime(src.getStartTime());
        dto.setEndTime(src.getEndTime());
        dto.setRentalItemsText(src.getRentalItemsText());
        dto.setRentalTotal(src.getRentalTotal());
        return dto;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private List<List<StaffBookingDetailSlotDTO>> groupIntoSessions(List<StaffBookingDetailSlotDTO> slots) {
        List<List<StaffBookingDetailSlotDTO>> sessions = new ArrayList<>();
        if (slots.isEmpty()) return sessions;

        List<StaffBookingDetailSlotDTO> current = new ArrayList<>();
        current.add(slots.get(0));

        for (int i = 1; i < slots.size(); i++) {
            StaffBookingDetailSlotDTO prev = slots.get(i - 1);
            StaffBookingDetailSlotDTO curr = slots.get(i);

            boolean sameCourt = prev.getCourtId() == curr.getCourtId();
            boolean consecutive = prev.getEndTime().equals(curr.getStartTime());

            if (sameCourt && consecutive) {
                current.add(curr);
            } else {
                sessions.add(current);
                current = new ArrayList<>();
                current.add(curr);
            }
        }
        sessions.add(current);

        sessions.sort((a, b) -> a.get(0).getStartTime().compareTo(b.get(0).getStartTime()));
        return sessions;
    }

    private String deriveSessionStatus(List<StaffBookingDetailSlotDTO> session) {
        boolean allCancelled = true;
        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean anyCheckedIn = false;

        for (StaffBookingDetailSlotDTO slot : session) {
            if (!"CANCELLED".equals(slot.getSlotStatus())) allCancelled = false;
            if (!"CHECK_OUT".equals(slot.getSlotStatus())) allCheckout = false;
            if (!"NO_SHOW".equals(slot.getSlotStatus())) allNoShow = false;
            if ("CHECKED_IN".equals(slot.getSlotStatus())) anyCheckedIn = true;
        }

        if (allCancelled) return "CANCELLED";
        if (allNoShow) return "NO_SHOW";
        if (allCheckout) return "COMPLETED";
        if (anyCheckedIn) return "CHECKED_IN";
        return "PENDING";
    }
}