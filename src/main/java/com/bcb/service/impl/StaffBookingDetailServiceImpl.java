package com.bcb.service.impl;

import com.bcb.controller.staff.StaffBookingSnapshotTokenUtil;
import com.bcb.dto.staff.StaffBookingDetailDataDto;
import com.bcb.dto.staff.StaffBookingDetailHeaderDto;
import com.bcb.dto.staff.StaffBookingDetailSessionDto;
import com.bcb.dto.staff.StaffBookingDetailSlotDto;
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
    public StaffBookingDetailDataDto getBookingDetail(int bookingId, int staffFacilityId) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            StaffBookingDetailHeaderDto header = repository.findBookingHeader(conn, bookingId);
            if (header == null || header.getFacilityId() != staffFacilityId) {
                return null;
            }

            List<StaffBookingDetailSlotDto> allSlots = repository.findBookingSlots(conn, bookingId);
            List<List<StaffBookingDetailSlotDto>> grouped = groupIntoSessions(allSlots);

            StaffBookingDetailDataDto data = new StaffBookingDetailDataDto();
            data.setBookingId(header.getBookingId());
            data.setBookingDate(header.getBookingDate());
            data.setBookingStatus(header.getBookingStatus());
            data.setCreatedAt(header.getCreatedAt());
            data.setCustomerName(header.getCustomerName());
            data.setCustomerPhone(header.getCustomerPhone());
            data.setCustomerType(header.getCustomerType());
            data.setSlots(allSlots);
            data.setInvoice(repository.findInvoice(conn, bookingId));

            List<StaffBookingDetailSessionDto> sessions = new ArrayList<>();
            for (int i = 0; i < grouped.size(); i++) {
                sessions.add(buildSessionDto(i, grouped.get(i)));
            }
            data.setSessions(sessions);

            StaffBookingSnapshotTokenUtil.Snapshot snapshot =
                    StaffBookingSnapshotTokenUtil.loadSnapshot(conn, bookingId, staffFacilityId);
            data.setEtag(snapshot != null ? StaffBookingSnapshotTokenUtil.computeEtag(snapshot) : null);
            return data;
        }
    }

    private StaffBookingDetailSessionDto buildSessionDto(int index, List<StaffBookingDetailSlotDto> session) {
        String sessionStatus = deriveSessionStatus(session);

        List<StaffBookingDetailSlotDto> activeSlots = new ArrayList<>();
        for (StaffBookingDetailSlotDto slot : session) {
            if (!"CANCELLED".equals(slot.getSlotStatus())) {
                activeSlots.add(slot);
            }
        }

        List<StaffBookingDetailSlotDto> displaySlots = activeSlots.isEmpty() ? session : activeSlots;
        StaffBookingDetailSlotDto first = displaySlots.get(0);
        StaffBookingDetailSlotDto last = displaySlots.get(displaySlots.size() - 1);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (StaffBookingDetailSlotDto slot : activeSlots) {
            if (slot.getPrice() != null) totalPrice = totalPrice.add(slot.getPrice());
        }

        int displaySlotCount = activeSlots.isEmpty() ? session.size() : activeSlots.size();

        StaffBookingDetailSessionDto dto = new StaffBookingDetailSessionDto();
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
        for (StaffBookingDetailSlotDto slot : session) {
            ids.add(slot.getBookingSlotId());
        }
        dto.setBookingSlotIds(ids);
        dto.setBookingSlots(session);
        return dto;
    }

    private List<List<StaffBookingDetailSlotDto>> groupIntoSessions(List<StaffBookingDetailSlotDto> slots) {
        List<List<StaffBookingDetailSlotDto>> sessions = new ArrayList<>();
        if (slots.isEmpty()) return sessions;

        List<StaffBookingDetailSlotDto> current = new ArrayList<>();
        current.add(slots.get(0));

        for (int i = 1; i < slots.size(); i++) {
            StaffBookingDetailSlotDto prev = slots.get(i - 1);
            StaffBookingDetailSlotDto curr = slots.get(i);

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

    private String deriveSessionStatus(List<StaffBookingDetailSlotDto> session) {
        boolean allCancelled = true;
        boolean allCheckout = true;
        boolean allNoShow = true;
        boolean anyCheckedIn = false;

        for (StaffBookingDetailSlotDto slot : session) {
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
