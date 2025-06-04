package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RestPasswordRequest;

import java.util.regex.Pattern;

public class ResetPasswordValidation {
    public static String validateResetPassword(RestPasswordRequest request, AccountRepo accountRepo) {
        if (request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        if (acc == null) {
            return "Account not available";
        }

        if (request.getNewPassword().trim().isEmpty()) {
            return "Password is required";
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            return "New password must be different from old password";
        }

        if (request.getNewPassword().length() < 8) {
            return "Password must be at least 8 characters";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one digit";
        }

        if (!lowerCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one lowercase letter";
        }

        if (!upperCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one uppercase letter";
        }

        if (!specialPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one special character";
        }

        if (request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm Password is required";
        }

        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            return "Confirm password must match the new password";
        }

        return "";
    }
}
