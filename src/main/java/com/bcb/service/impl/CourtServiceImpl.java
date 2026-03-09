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

import java.util.ArrayList;
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
    public Court getCourtById(int courtId) throws BusinessException {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new BusinessException("COURT_NOT_FOUND", "Court not found: " + courtId));
    }

    @Override
    public int createCourt(Court court) throws ValidationException, BusinessException {
        trimDescription(court);
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
        trimDescription(court);
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
        courtRepository.deactivate(courtId);
    }

    @Override
    public List<CourtViewDTO> getCourtsByFacilityDTO(int facilityId) {
        return courtRepository.findByFacilityForView(facilityId);
    }

    /**
     * Bulk-create courts with auto-incremented numeric suffixes.
     * Finds the current max suffix for the given prefix and starts from max+1.
     *
     * @author AnhTN
     */
    @Override
    public List<String> createBulkCourts(int facilityId, String courtNamePrefix, int count,
                                          int courtTypeId, String description)
            throws ValidationException, BusinessException {

        if (count < 1 || count > 20) {
            throw new BusinessException("INVALID_COUNT", "Số lượng sân phải từ 1 đến 20");
        }

        if (courtNamePrefix == null || courtNamePrefix.trim().isEmpty()) {
            throw new ValidationException("Tiền tố tên sân không được để trống");
        }

        String trimmedPrefix = courtNamePrefix.trim();

        if (!facilityRepository.findById(facilityId).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND", "Không tìm thấy cơ sở");
        }

        // Find max existing suffix for this prefix
        List<String> existingNames = courtRepository.findNamesByPrefix(facilityId, trimmedPrefix);
        int maxSuffix = 0;
        for (String name : existingNames) {
            String afterPrefix = name.substring(trimmedPrefix.length()).trim();
            try {
                int num = Integer.parseInt(afterPrefix);
                if (num > maxSuffix) maxSuffix = num;
            } catch (NumberFormatException ignored) {
                // not a numeric suffix, skip
            }
        }

        List<String> createdNames = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            int suffix = maxSuffix + i;
            String courtName = trimmedPrefix + " " + suffix;

            Court court = new Court();
            court.setFacilityId(facilityId);
            court.setCourtName(courtName);
            court.setCourtTypeId(courtTypeId);
            court.setDescription(description == null || description.trim().isEmpty() ? null : description.trim());
            court.setIsActive(true);

            List<String> errors = CourtValidator.validate(court);
            if (!errors.isEmpty()) {
                throw new ValidationException("Lỗi tại sân \"" + courtName + "\": " + String.join(", ", errors));
            }

            courtRepository.insert(court);
            createdNames.add(courtName);
        }

        return createdNames;
    }

    /**
     * Trim description: blank → null, otherwise trim whitespace.
     */
    private void trimDescription(Court court) {
        if (court.getDescription() != null) {
            String trimmed = court.getDescription().trim();
            court.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
    }
}

