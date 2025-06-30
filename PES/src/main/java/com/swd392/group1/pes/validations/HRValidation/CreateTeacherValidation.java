package com.swd392.group1.pes.validations.HRValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.CreateTeacherRequest;

public class CreateTeacherValidation {
    public static String validate(CreateTeacherRequest request, AccountRepo accountRepo) {

        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        if (request.getGender().trim().isEmpty()) {
            return "Gender is required";
        }

        if (!request.getGender().equals("male") &&
                !request.getGender().equals("female") &&
                !request.getGender().equals("other")) {
            return "Gender must be male, female, or other";
        }
        return "";
    }
}
