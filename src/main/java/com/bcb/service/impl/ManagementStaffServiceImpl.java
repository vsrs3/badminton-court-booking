package com.bcb.service.impl;

import com.bcb.service.ManagementStaffService;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.ManagementStaffRepository;
import com.bcb.repository.impl.ManagementStaffRepositoryImpl;

public class ManagementStaffServiceImpl implements ManagementStaffService {
	
	// Repository instance for database operations
	private final ManagementStaffRepository profileRepo = new ManagementStaffRepositoryImpl();

	@Override
	public boolean updateInfo(Integer accountId, Integer facilityId, String fullName, String email, String phoneNumber) {
		if (accountId == null || facilityId == null) {
			throw new DataAccessException("account ID hoặc facility ID không thể null");
		}
		
		if(fullName == null || fullName.isEmpty()
				|| email == null || email.isEmpty()
				|| phoneNumber == null || phoneNumber.isEmpty()) {
			throw new DataAccessException("Tên, email hoặc số điện thoại không thể null");
		}
		
		return profileRepo.updateInfo(accountId, facilityId, fullName, email, phoneNumber);
	}


	@Override
	public boolean softDeleteAndActive(Integer accountId) {
		if (accountId == null || accountId <= 0) {
			throw new DataAccessException("account ID không thể null");
		}
		
		return profileRepo.softDeleteAndActive(accountId);
	}


	@Override
	public boolean resetPassword(Integer accountId, String passwordHash) {
		if (accountId == null || accountId <= 0) {
			throw new DataAccessException("account ID không thể null");
		}
		
		return profileRepo.resetPassword(accountId, passwordHash);
	}

}

