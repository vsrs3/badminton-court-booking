package com.bcb.service.staff;

import com.bcb.dto.staff.StaffTimelineDataDTO;

import java.time.LocalDate;

public interface StaffTimelineService {
    StaffTimelineDataDTO getTimeline(int facilityId, LocalDate bookingDate) throws Exception;
}

