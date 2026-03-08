package com.bcb.service.impl;

import com.bcb.dto.staff.StaffBookingListDataDto;
import com.bcb.dto.staff.StaffBookingListSearchCriteriaDto;
import com.bcb.repository.impl.StaffBookingListRepositoryImpl;
import com.bcb.repository.staff.StaffBookingListRepository;
import com.bcb.service.staff.StaffBookingListService;

public class StaffBookingListServiceImpl implements StaffBookingListService {

    private final StaffBookingListRepository repository = new StaffBookingListRepositoryImpl();

    @Override
    public StaffBookingListDataDto getBookingList(int facilityId, String search, int page, int size) throws Exception {
        StaffBookingListSearchCriteriaDto criteria = new StaffBookingListSearchCriteriaDto();
        criteria.setFacilityId(facilityId);
        criteria.setSearch(search);
        criteria.setHasSearch(search != null);
        criteria.setNumericSearch(search != null && search.matches("\\d+"));
        criteria.setLikePattern(search != null ? "%" + search + "%" : null);

        int totalRows = repository.countBookings(criteria);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
        int normalizedPage = Math.min(page, totalPages);
        int offset = (normalizedPage - 1) * size;

        StaffBookingListDataDto data = new StaffBookingListDataDto();
        data.setPage(normalizedPage);
        data.setSize(size);
        data.setTotalRows(totalRows);
        data.setTotalPages(totalPages);
        data.setBookings(repository.findBookings(criteria, offset, size));
        return data;
    }
}
