package com.bcb.service.owner;

import com.bcb.dto.owner.OwnerRentalDeactivateResultDTO;
import com.bcb.dto.owner.OwnerRentalDetailsDTO;
import com.bcb.dto.owner.OwnerRentalFacilityOptionDTO;
import com.bcb.dto.owner.OwnerRentalPurgeResultDTO;
import com.bcb.dto.owner.OwnerRentalReportSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface OwnerRentalReportService {

    List<OwnerRentalFacilityOptionDTO> getFacilityOptions(String keyword) throws Exception;

    OwnerRentalReportSummaryDTO getSummary(
            Integer facilityId,
            Integer year,
            Integer month,
            Integer day,
            Integer inactiveMonth,
            String detailScope
    ) throws Exception;

    OwnerRentalDetailsDTO getDetails(
            Integer facilityId,
            Integer year,
            Integer month,
            Integer day,
            String slotTime,
            String scope
    ) throws Exception;

    OwnerRentalDeactivateResultDTO deactivateInactiveItems(Integer facilityId, Integer year, Integer month)
            throws Exception;

    OwnerRentalPurgeResultDTO purgeRentalData(LocalDateTime start, LocalDateTime end) throws Exception;
}
