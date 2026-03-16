package com.bcb.service.impl;
import com.bcb.dto.staff.StaffRefundListDataDTO;
import com.bcb.repository.impl.StaffRefundRepositoryImpl;
import com.bcb.repository.staff.StaffRefundRepository;
import com.bcb.service.staff.StaffRefundListService;
import com.bcb.utils.DBContext;
import java.sql.Connection;

public class StaffRefundListServiceImpl implements StaffRefundListService {
    private final StaffRefundRepository repository = new StaffRefundRepositoryImpl();
    @Override
    public StaffRefundListDataDTO getRefundList(int facilityId, int page, int size, String search) throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            int totalRows = repository.countPendingRefunds(conn, facilityId, search);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
            int normalizedPage = Math.min(page, totalPages);
            int offset = (normalizedPage - 1) * size;
            StaffRefundListDataDTO data = new StaffRefundListDataDTO();
            data.setPage(normalizedPage);
            data.setSize(size);
            data.setTotalRows(totalRows);
            data.setTotalPages(totalPages);
            data.setRefunds(repository.findPendingRefunds(conn, facilityId, offset, size, search));
            return data;
        }
    }
}
