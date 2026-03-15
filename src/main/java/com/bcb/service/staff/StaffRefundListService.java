package com.bcb.service.staff;

import com.bcb.dto.staff.StaffRefundListDataDTO;

public interface StaffRefundListService {
    StaffRefundListDataDTO getRefundList(int facilityId, int page, int size, String search) throws Exception;
}
