package com.bcb.repository;

import com.bcb.model.Customer;

import java.util.List;

public interface CustomerProfileRepository {
     boolean updateAccountInfo (String avatarPath, String fullName, String email, String phone, int accountId);

     boolean updatePassword (String newPass, int accountId);

     Customer getCustomerById (int cusId);

     List<String> emailList (String email);
}
