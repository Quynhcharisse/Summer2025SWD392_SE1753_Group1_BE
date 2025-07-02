package com.swd392.group1.pes.validations.HRValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.UpdateTeacherRequest;

public class UpdateTeacherValidation {
    public static String validate(UpdateTeacherRequest request, AccountRepo accountRepo) {

        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        if (request.getPhone().trim().isEmpty()) {
            return "Phone number is required";
        }

        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits";
        }

        if (request.getGender().trim().isEmpty()) {
            return "Gender is required";
        }

        if (!request.getGender().equals("male") &&
                !request.getGender().equals("female")) {
            return "Gender must be male, female";
        }

        return "";
    }
}
