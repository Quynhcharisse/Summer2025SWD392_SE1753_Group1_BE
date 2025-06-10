package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.requests.UpdateProfileRequest;

public class UpdateProfileValidation {
    public static String validate(UpdateProfileRequest request) {
        // Name validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (!request.getName().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes.";
        }
        int nameLength = request.getName().trim().length();
        if (nameLength < 2 || nameLength > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        // Phone validation
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }
        if (!request.getPhone().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits.";
        }

        // Gender validation
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }
        String gender = request.getGender().trim().toLowerCase();
        if (!gender.equals("male") && !gender.equals("female")) {
            return "Gender must be male or female.";
        }

        return "";
    }
}
