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
}
