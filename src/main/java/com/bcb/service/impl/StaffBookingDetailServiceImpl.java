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
            data.setRecurring(header.isRecurring());
            data.setRecurringStartDate(header.getRecurringStartDate());
            data.setRecurringEndDate(header.getRecurringEndDate());
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
            List<StaffBookingDetailRentalRowDTO> rentalRows = repository.findBookingRentalRows(conn, bookingId);
            data.setRentalRows(rentalRows);

            BigDecimal courtTotal = calculateCourtTotal(allSlots);
            data.setCourtTotal(courtTotal);

            BigDecimal rentalTotal = calculateRentalTotal(rentalRows);
            data.setRentalTotal(rentalTotal);

            BigDecimal grandTotal = courtTotal.add(rentalTotal);
            if (invoice != null) {
                invoice.setTotalAmount(grandTotal);
            }
            data.setGrandTotal(grandTotal);

            BigDecimal originalCourtTotal = calculateCourtTotalIncludingCancelled(allSlots);
            data.setOriginalCourtTotal(originalCourtTotal);

            BigDecimal originalGrandTotal = originalCourtTotal.add(rentalTotal);
            data.setOriginalGrandTotal(originalGrandTotal);

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

        BigDecimal originalTotalPrice = BigDecimal.ZERO;
        for (StaffBookingDetailSlotDTO slot : session) {
            if (slot.getPrice() != null) {
                originalTotalPrice = originalTotalPrice.add(slot.getPrice());
            }
        }

        int displaySlotCount = activeSlots.isEmpty() ? session.size() : activeSlots.size();

        StaffBookingDetailSessionDTO dto = new StaffBookingDetailSessionDTO();
        dto.setSessionIndex(index);
        dto.setCourtId(first.getCourtId());
        dto.setCourtName(first.getCourtName());
        dto.setSessionDate(first.getBookingDate());
        dto.setStartTime(first.getStartTime());
        dto.setEndTime(last.getEndTime());
        dto.setSlotCount(displaySlotCount);
        dto.setTotalPrice(totalPrice);
        dto.setOriginalTotalPrice(originalTotalPrice);
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

    private BigDecimal calculateCourtTotal(List<StaffBookingDetailSlotDTO> slots) {
        BigDecimal total = BigDecimal.ZERO;
        if (slots == null) {
            return total;
        }

        for (StaffBookingDetailSlotDTO slot : slots) {
            if (slot == null) continue;
            if ("CANCELLED".equals(slot.getSlotStatus())) continue;
            if (slot.getPrice() != null) {
                total = total.add(slot.getPrice());
            }
        }

        return total;
    }

    private BigDecimal calculateCourtTotalIncludingCancelled(List<StaffBookingDetailSlotDTO> slots) {
        BigDecimal total = BigDecimal.ZERO;
        if (slots == null) {
            return total;
        }

        for (StaffBookingDetailSlotDTO slot : slots) {
            if (slot == null) continue;
            if (slot.getPrice() != null) {
                total = total.add(slot.getPrice());
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
            boolean sameDate = safeDate(prev.getBookingDate()).equals(safeDate(curr.getBookingDate()));

            if (sameCourt && consecutive && sameDate) {
                current.add(curr);
            } else {
                sessions.add(current);
                current = new ArrayList<>();
                current.add(curr);
            }
        }
        sessions.add(current);

        sessions.sort((a, b) -> {
            String da = safeDate(a.get(0).getBookingDate());
            String db = safeDate(b.get(0).getBookingDate());
            int cmp = da.compareTo(db);
            if (cmp != 0) return cmp;
            return a.get(0).getStartTime().compareTo(b.get(0).getStartTime());
        });
        return sessions;
    }

    private String safeDate(String date) {
        return date == null ? "" : date;
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
