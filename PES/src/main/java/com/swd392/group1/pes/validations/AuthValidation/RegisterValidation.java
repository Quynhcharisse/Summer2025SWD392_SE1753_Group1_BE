package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RegisterRequest;

import java.util.regex.Pattern;

public class RegisterValidation {
    public static String validate(RegisterRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            return "Invalid email format.";
        }

        if (accountRepo.existsByEmail(request.getEmail())) {
            return "This email is already registered.";
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        if (request.getPassword().length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one digit.";
        }
        if (!lowerCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!upperCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!specialPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one special character.";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        if (!request.getConfirmPassword().equals(request.getPassword())) {
            return "Confirm password does not match password.";
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }

        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes.";
        }

        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }

        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid prefix and be 10 digits.";
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }

        if (!request.getGender().trim().equals("male") &&
                !request.getGender().trim().equals("female")) {
            return "Gender must be 'male', 'female'";
        }

        if (request.getIdentityNumber() == null || request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required.";
        }

        Pattern idPattern = Pattern.compile("^\\d{12}$");
        if (!idPattern.matcher(request.getIdentityNumber()).matches()) {
            return "Identity number must be exactly 12 digits.";
        }

        return "";
    }
}
