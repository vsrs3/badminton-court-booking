package com.bcb.service.impl;

import com.bcb.dto.CourtViewDTO;
import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Court;
import com.bcb.repository.CourtRepository;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.CourtRepositoryImpl;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.CourtService;
import com.bcb.validation.CourtValidator;

import java.util.List;

public class CourtServiceImpl implements CourtService {
    private final CourtRepository courtRepository;
    private final FacilityRepository facilityRepository;

    public CourtServiceImpl() {
        this.courtRepository = new CourtRepositoryImpl();
        this.facilityRepository = new FacilityRepositoryImpl();
    }

    public CourtServiceImpl(CourtRepository courtRepository, FacilityRepository facilityRepository) {
        this.courtRepository = courtRepository;
        this.facilityRepository = facilityRepository;
    }

    @Override
    public List<Court> getCourtsByFacility(int facilityId) throws BusinessException {
        if (!facilityRepository.findById(facilityId).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND", "Facility not found: " + facilityId);
        }
        return courtRepository.findByFacility(facilityId);
    }


    @Override
    public Court getCourtById(int courtId) throws BusinessException {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException("COURT_NOT_FOUND", "Court not found: " + courtId));
    }

    @Override
    public int createCourt(Court court) throws ValidationException, BusinessException {
        List<String> errors = CourtValidator.validate(court);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }
        if (!facilityRepository.findById(court.getFacilityId()).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND", "Facility not found");
        }
        return courtRepository.insert(court);
    }

    @Override
    public void updateCourt(Court court) throws ValidationException, BusinessException {
        List<String> errors = CourtValidator.validate(court);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }
        if (!courtRepository.findById(court.getCourtId()).isPresent()) {
            throw new BusinessException("COURT_NOT_FOUND", "Court not found");
        }
        if (!facilityRepository.findById(court.getFacilityId()).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND", "Facility not found");
        }
        courtRepository.update(court);
    }

    @Override
    public void deactivateCourt(int courtId) throws BusinessException {
        if (!courtRepository.findById(courtId).isPresent()) {
            throw new BusinessException("COURT_NOT_FOUND", "Court not found");
        }
        if (courtRepository.hasActiveBookings(courtId)) {
            throw new BusinessException("COURT_HAS_BOOKINGS", "Cannot deactivate court with active bookings");
        }
        courtRepository.deactivate(courtId);
    }

    @Override
    public List<CourtViewDTO> getCourtsByFacilityDTO(int facilityId) {
        return courtRepository.findByFacilityForView(facilityId);
    }
}
