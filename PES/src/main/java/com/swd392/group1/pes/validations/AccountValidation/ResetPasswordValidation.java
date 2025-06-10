package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RestPasswordRequest;

import java.util.regex.Pattern;

public class ResetPasswordValidation {
    public static String validateResetPassword(RestPasswordRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        if (acc == null) {
            return "No active account found with this email.";
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return "New password is required.";
        }

        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            return "Old password is required.";
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            return "New password must be different from the old password.";
        }

        if (request.getNewPassword().length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one digit.";
        }
        if (!lowerCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!upperCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!specialPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one special character.";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            return "Confirm password does not match the new password.";
        }

        return "";
    }
}
