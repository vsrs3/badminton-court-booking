package com.bcb.service.impl;

import com.bcb.service.ManagementStaffService;
import com.bcb.repository.StaffProfileRepository;
import com.bcb.repository.impl.StaffProfileRepositoryImpl;

public class ManagementStaffServiceImpl implements ManagementStaffService {
	
	// Repository instance for database operations
	private final StaffProfileRepository profileRepo = new StaffProfileRepositoryImpl();

	@Override
	public boolean updateInfo(Integer accountId, Integer facilityId, String fullName, String email, String phoneNumber) {
		if (accountId == null || accountId <= 0) {
			throw new IllegalArgumentException("account ID không thể null");
		}
		
		return profileRepo.updateInfo(accountId, facilityId, fullName, email, phoneNumber);
	}


	@Override
	public boolean softDeleteAndActive(Integer accountId) {
		if (accountId == null || accountId <= 0) {
			throw new IllegalArgumentException("account ID không thể null");
		}
		
		return profileRepo.softDeleteAndActive(accountId);
	}


	@Override
	public boolean resetPassword(Integer accountId, String passwordHash) {
		if (accountId == null || accountId <= 0) {
			throw new IllegalArgumentException("account ID không thể null");
		}
		
		return profileRepo.resetPassword(accountId, passwordHash);
	}

}
