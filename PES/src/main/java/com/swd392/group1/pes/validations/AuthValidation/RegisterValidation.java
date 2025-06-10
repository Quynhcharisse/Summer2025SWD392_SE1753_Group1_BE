package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RegisterRequest;

import java.util.regex.Pattern;

public class RegisterValidation {
    public static String validate(RegisterRequest request, AccountRepo accountRepo) {
        // Email required
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        // Email format
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            return "Invalid email format.";
        }

        // Email already registered
        if (accountRepo.existsByEmail(request.getEmail())) {
            return "This email is already registered.";
        }

        // Password required
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        // Password length
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

        // Confirm password required
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        // Confirm password matches
        if (!request.getConfirmPassword().equals(request.getPassword())) {
            return "Confirm password does not match password.";
        }

        // Name required
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }

        // Name valid characters
        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes.";
        }

        // Name length
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        // Phone required
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }

        // Phone valid
        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid prefix and be 10 digits.";
        }

        // Gender required
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }

        if (!request.getGender().trim().equals("male") &&
                !request.getGender().trim().equals("female")) {
            return "Gender must be 'male', 'female'";
        }

        // Identity number required
        if (request.getIdentityNumber() == null || request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required.";
        }

        // Identity number valid
        Pattern idPattern = Pattern.compile("^\\d{12}$");
        if (!idPattern.matcher(request.getIdentityNumber()).matches()) {
            return "Identity number must be exactly 12 digits.";
        }

        return "";
    }
}
