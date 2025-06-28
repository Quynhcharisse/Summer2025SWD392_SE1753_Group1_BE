package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.requests.ResetPassRequest;

import java.util.regex.Pattern;

public class ForgotPasswordValidation {
    public static String validate(ForgotPasswordRequest request, AccountRepo accountRepo) {
        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        // Email required
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }


        // Account must exist and be active
        if (acc == null) {
            return "No active account found with this email.";
        }

        return "";
    }

    public static String reset(ResetPassRequest request) {
        // Password required
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        //Password hop le
        if (request.getNewPassword().length() < 8) {
            return "Password must be at least 8 characters";
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

        // Confirm password required
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        // Confirm password matches
        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            return "Confirm password does not match password.";
        }

    }

}
