package com.swd392.group1.pes.validations.HRValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.CreateTeacherRequest;

public class CreateTeacherValidation {
    public static String validate(CreateTeacherRequest request, AccountRepo accountRepo) {
        if (request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format";
        }

        if (accountRepo.existsByEmail(request.getEmail())) {
            return "Email is already registered";
        }

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

        if (!request.getGender().trim().equals("male") &&
                !request.getGender().trim().equals("female") &&
                !request.getGender().trim().equals("other")) {
            return "Gender must be male, female, or other";
        }

        if (request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required";
        }

        if (!request.getIdentityNumber().matches("^\\d{12}$")) {
            return "Identity number must have 12 digits";
        }

        return "";
    }
}
