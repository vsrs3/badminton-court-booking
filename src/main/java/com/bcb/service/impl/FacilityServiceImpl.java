package com.bcb.service.impl;

import com.bcb.exception.BusinessException;
import com.bcb.exception.ValidationException;
import com.bcb.model.Facility;
import com.bcb.repository.FacilityRepository;
import com.bcb.repository.impl.FacilityRepositoryImpl;
import com.bcb.service.FacilityService;
import com.bcb.validation.FacilityValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of FacilityService.
 * Handles business logic for facility operations.
 *
 * Note: This is a single-owner system. All facilities belong to the admin.
 */
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityServiceImpl() {
        this.facilityRepository = new FacilityRepositoryImpl();
    }

    // Constructor for dependency injection (testing)
    public FacilityServiceImpl(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public List<Facility> findAll(int limit, int offset) {
        return facilityRepository.findAll(limit, offset);
    }

    @Override
    public int count() {
        return facilityRepository.count();
    }

    @Override
    public List<Facility> findByName(String name, int limit, int offset) {
        if (name == null || name.trim().isEmpty()) {
            return findAll(limit, offset);
        }
        return facilityRepository.findByName(name, limit, offset);
    }

    @Override
    public int countByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return count();
        }
        return facilityRepository.countByName(name);
    }

    @Override
    public List<Facility> findByLocation(String address, String province, String district,
                                          String ward, int limit, int offset) {
        return facilityRepository.findByLocation(address, province, district, ward, limit, offset);
    }

    @Override
    public int countByLocation(String address, String province, String district, String ward) {
        return facilityRepository.countByLocation(address, province, district, ward);
    }

    @Override
    public List<Facility> findByKeyword(String keyword, int limit, int offset) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(limit, offset);
        }
        return facilityRepository.findByKeyword(keyword.trim(), limit, offset);
    }

    @Override
    public int countByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return count();
        }
        return facilityRepository.countByKeyword(keyword.trim());
    }

    @Override
    public Facility findById(int facilityId) throws BusinessException {
        return facilityRepository.findById(facilityId)
            .orElseThrow(() -> new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facilityId));
    }

    @Override
    public int create(Facility facility) throws ValidationException, BusinessException {
        // Validate input
        List<String> errors = FacilityValidator.validate(facility);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        try {
            return facilityRepository.insert(facility);
        } catch (Exception e) {
            throw new BusinessException("FACILITY_CREATE_ERROR",
                "Failed to create facility: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Facility facility) throws ValidationException, BusinessException {
        // Validate input
        List<String> errors = FacilityValidator.validate(facility);
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        // Check facility exists
        if (!facilityRepository.findById(facility.getFacilityId()).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facility.getFacilityId());
        }

        try {
            int rowsAffected = facilityRepository.update(facility);
            if (rowsAffected == 0) {
                throw new BusinessException("FACILITY_UPDATE_ERROR",
                    "No rows affected during update");
            }
        } catch (Exception e) {
            throw new BusinessException("FACILITY_UPDATE_ERROR",
                "Failed to update facility: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int facilityId) throws BusinessException {
        // Check facility exists
        if (!facilityRepository.findById(facilityId).isPresent()) {
            throw new BusinessException("FACILITY_NOT_FOUND",
                "Facility not found with ID: " + facilityId);
        }

        // Check if has active bookings
        if (facilityRepository.hasActiveBookings(facilityId)) {
            throw new BusinessException("FACILITY_HAS_ACTIVE_BOOKINGS",
                "Cannot delete facility with active or future bookings");
        }

        try {
            int rowsAffected = facilityRepository.softDelete(facilityId);
            if (rowsAffected == 0) {
                throw new BusinessException("FACILITY_DELETE_ERROR",
                    "No rows affected during delete");
            }
        } catch (Exception e) {
            throw new BusinessException("FACILITY_DELETE_ERROR",
                "Failed to delete facility: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<Integer, String> buildDisplayAddressMap(List<Facility> facilities) {
        Map<Integer, String> map = new HashMap<>();

        for (Facility f : facilities) {
            String fullAddress = Stream.of(
                            f.getAddress(),
                            f.getWard(),
                            f.getDistrict(),
                            f.getProvince()
                    )
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(", "));

            map.put(f.getFacilityId(), fullAddress);
        }

        return map;
    }
}
