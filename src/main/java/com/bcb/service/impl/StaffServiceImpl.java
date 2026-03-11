package com.bcb.service.impl;

import java.util.List;
import java.util.Optional;

import com.bcb.exception.DataAccessException;
import com.bcb.model.Account;
import com.bcb.model.Facility;
import com.bcb.model.Staff;
import com.bcb.repository.impl.StaffRepositoryImpl;
import com.bcb.repository.StaffRepository;
import com.bcb.service.StaffService;

public class StaffServiceImpl implements StaffService {

	private final StaffRepository staffRepo = new StaffRepositoryImpl();

	@Override
	public List<Account> findAll(int limit, int offset) {
		return staffRepo.findAll(limit, offset);
	}

	@Override
	public int count() {
		return staffRepo.count();
	}

	@Override
	public boolean updateStatus(Integer accountId, boolean isActive) {
		if (accountId == null) {
			throw new IllegalArgumentException("Account ID cannot be null");
		}

		return staffRepo.updateStatus(accountId, isActive);
	}

	@Override
	public List<Account> findByKeyword(String keyword, int limit, int offset) {
		return staffRepo.findByKeyword(keyword, limit, offset);
	}

	@Override
	public int countByKeyword(String keyword) {
		return staffRepo.countByKeyword(keyword);
	}

	@Override
	public Optional<Account> findById(Integer accountId) {
		return staffRepo.findById(accountId);
	}

	@Override
	public List<Facility> findFacilitiesById(Integer accountId) {
		if (accountId == null) {
			throw new IllegalArgumentException("Account ID cannot be null");
		}

		return staffRepo.findFacilitiesById(accountId);
	}

	@Override
	public List<Facility> findFacilities() {
		return staffRepo.findAllFacilities();
	}

	@Override
	public boolean createStaff(String fullName, String email, String phone, Integer facilityId) {
		if (fullName == null || fullName.isEmpty() 
				|| email == null || email.isEmpty()
				|| phone == null || phone.isEmpty()
				|| facilityId == null) {
			
			throw new DataAccessException("Tên, email, số điện thoại hoặc Id của địa điểm bị null");
		}

		return staffRepo.createStaff(fullName, email, phone, facilityId);
	}

}
