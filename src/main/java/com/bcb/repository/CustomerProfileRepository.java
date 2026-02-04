package com.bcb.repository;

import com.bcb.model.Account;

import java.util.List;

public interface CustomerProfileRepository {
     boolean updateAccountInfo (String avatarPath, String fullName, String email, String phone, Integer accountId);

     boolean updatePassword (String newPass, Integer accountId);

     Account getCustomerById (Integer accountId);

     List<String> emailList (String email);
}
