package com.bcb.service;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import java.util.List;

public interface CourtService {
    List<Court> getCourtsByFacility(int facilityId) throws BusinessException;
    Court getCourtById(int courtId) throws BusinessException;
    int createCourt(Court court) throws ValidationException, BusinessException;
    void updateCourt(Court court) throws ValidationException, BusinessException;
    void deactivateCourt(int courtId) throws BusinessException;
}
