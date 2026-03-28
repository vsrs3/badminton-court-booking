package com.bcb.repository.owner;

import com.bcb.dto.owner.OwnerRentalDetailRowDTO;
import com.bcb.dto.owner.OwnerRentalFacilityOptionDTO;
import com.bcb.dto.owner.OwnerRentalInactiveItemDTO;
import com.bcb.dto.owner.OwnerRentalPointDTO;
import com.bcb.dto.owner.OwnerRentalTopItemDTO;

import java.time.LocalDate;
import java.util.List;

public interface OwnerRentalReportRepository {

    List<OwnerRentalFacilityOptionDTO> findFacilityOptions(String keyword) throws Exception;

    String findFacilityName(int facilityId) throws Exception;

    Integer findLatestRentalYear(int facilityId) throws Exception;

    List<OwnerRentalPointDTO> findMonthlyRevenue(int facilityId, int year) throws Exception;

    List<OwnerRentalPointDTO> findDailyRevenue(int facilityId, int year, int month) throws Exception;

    List<String> findSlotLabels(int facilityId) throws Exception;

    List<OwnerRentalPointDTO> findHourlyRevenue(int facilityId, LocalDate bookingDate) throws Exception;

    List<OwnerRentalTopItemDTO> findTopItems(int facilityId, int year, int month) throws Exception;

    List<OwnerRentalDetailRowDTO> findDetailRows(int facilityId, int year, int month, Integer day, String slotTime)
            throws Exception;

    List<OwnerRentalInactiveItemDTO> findInactiveItems(int facilityId, int year, int month, int limit) throws Exception;

    int deactivateInactiveItems(int facilityId, int year, int month, int limit) throws Exception;
}
