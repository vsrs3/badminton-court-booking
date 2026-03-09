package com.bcb.service;

import com.bcb.dto.CourtViewDTO;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import java.util.List;

public interface CourtService {
    Court getCourtById(int courtId) throws BusinessException;
    int createCourt(Court court) throws ValidationException, BusinessException;
    void updateCourt(Court court) throws ValidationException, BusinessException;
    void deactivateCourt(int courtId) throws BusinessException;
    List<CourtViewDTO> getCourtsByFacilityDTO(int facilityId);

    /**
     * Bulk-create multiple courts with auto-incremented numeric suffixes.
     * Queries existing names with the given prefix to avoid duplicates.
     *
     * @param facilityId       target facility
     * @param courtNamePrefix  name prefix, e.g. "Sân"
     * @param count            number of courts to create (1–20)
     * @param courtTypeId      court type
     * @param description      optional shared description
     * @return list of created court names
     * @throws ValidationException if any court fails validation
     * @throws BusinessException   if facility not found or count out of range
     * @author AnhTN
     */
    List<String> createBulkCourts(int facilityId, String courtNamePrefix, int count,
                                   int courtTypeId, String description)
            throws ValidationException, BusinessException;
}

