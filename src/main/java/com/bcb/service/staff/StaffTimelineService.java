package com.bcb.service.staff;

import com.bcb.dto.staff.StaffTimelineDataDto;

import java.time.LocalDate;

public interface StaffTimelineService {
    StaffTimelineDataDto getTimeline(int facilityId, LocalDate bookingDate) throws Exception;
}

