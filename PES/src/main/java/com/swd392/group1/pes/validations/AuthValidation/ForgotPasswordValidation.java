package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ForgotPasswordRequest;

import java.util.regex.Pattern;

public class ForgotPasswordValidation {
    public static String validate(ForgotPasswordRequest request, AccountRepo accountRepo) {
        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        //Account không tồn tại
        if (acc == null) {
            return "Account not available";
        }

        //Email không được để trống
        if (request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        //email ton tai
        if (accountRepo.existsByEmail(request.getEmail())) {
            return "Email is already registered";
        }

        //Password ko de trong
        if (request.getPassword().trim().isEmpty()) {
            return "Password is required";
        }

        //Password hop le
        if (request.getPassword().length() < 8) {
            return "Password must be at least 8 characters";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one digit";
        }

        if (!lowerCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one lowercase letter";
        }

        if (!upperCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one uppercase letter";
        }

        if (!specialPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one special character";
        }

        //Confirm password ko duoc trong
        if (request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm Password is required";
        }

        //Confirm password = password
        if (!request.getConfirmPassword().equals(request.getPassword())){
            return "Confirm password must be the same as password";
        }

        return "";
    }
}
