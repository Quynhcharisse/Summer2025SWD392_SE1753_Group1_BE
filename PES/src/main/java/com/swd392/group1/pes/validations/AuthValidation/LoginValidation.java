package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.LoginRequest;

public class LoginValidation {
    public static String validate(LoginRequest request, AccountRepo accountRepo) {

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null) {
            return "Email or Password is incorrect";
        }

        if(acc.getStatus().equalsIgnoreCase(Status.ACCOUNT_BAN.getValue())) {
            return "Email or Password is incorrect";
        }

        if(!acc.getEmail().equals(request.getEmail())) {
            return "Email or Password is incorrect";
        }

        if(!acc.getPassword().equals(request.getPassword())) {
            return "Email or Password is incorrect";
        }

        return "";
    }
}
