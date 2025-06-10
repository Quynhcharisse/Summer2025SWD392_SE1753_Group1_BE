package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.LoginRequest;

public class LoginValidation {
    public static String validate(LoginRequest request, AccountRepo accountRepo) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null || !acc.getPassword().equals(request.getPassword()) ||
                acc.getStatus().equalsIgnoreCase(Status.ACCOUNT_BAN.getValue())) {
            return "Email or password is incorrect.";
        }

        return "";
    }
}
