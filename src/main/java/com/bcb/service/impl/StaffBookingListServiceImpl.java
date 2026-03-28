package com.bcb.service.impl;

import com.bcb.dto.staff.StaffBookingListDataDTO;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDTO;
import com.bcb.repository.impl.StaffBookingListRepositoryImpl;
import com.bcb.repository.staff.StaffBookingListRepository;
import com.bcb.service.staff.StaffBookingListService;

public class StaffBookingListServiceImpl implements StaffBookingListService {

    private final StaffBookingListRepository repository = new StaffBookingListRepositoryImpl();

    /**
     * Builds search criteria, runs paginated query, and returns booking list data.
     */
    @Override
    public StaffBookingListDataDTO getBookingList(int facilityId, String search, String status, boolean todayOnly, int page, int size) throws Exception {
        StaffBookingListSearchCriteriaDTO criteria = new StaffBookingListSearchCriteriaDTO();
        criteria.setFacilityId(facilityId);
        criteria.setSearch(search);
        criteria.setHasSearch(search != null);
        criteria.setNumericSearch(search != null && search.matches("\\d+"));
        criteria.setLikePattern(search != null ? "%" + search + "%" : null);
        criteria.setStatus(status);
        criteria.setTodayOnly(todayOnly);
        criteria.setTodayDate(java.sql.Date.valueOf(java.time.LocalDate.now()));

        // Build query with pagination (page/size/offset).
        int totalRows = repository.countBookings(criteria);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
        int normalizedPage = Math.min(page, totalPages);
        int offset = (normalizedPage - 1) * size;

        StaffBookingListDataDTO data = new StaffBookingListDataDTO();
        data.setPage(normalizedPage);
        data.setSize(size);
        data.setTotalRows(totalRows);
        data.setTotalPages(totalPages);
        data.setBookings(repository.findBookings(criteria, offset, size));
        return data;
    }
}

