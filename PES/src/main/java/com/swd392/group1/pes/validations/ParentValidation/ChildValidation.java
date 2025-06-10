package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.requests.AddChildRequest;
import com.swd392.group1.pes.requests.UpdateChildRequest;

import java.time.LocalDate;

public class ChildValidation {
    public static String updateChildValidate(UpdateChildRequest request) {
        if (request.getId() <= 0) {
            return "Invalid child ID.";
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (request.getName().length() < 2 || request.getName().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (!isValidGender(request.getGender())) {
            return "Gender must be Male, Female or Other.";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        return "";
    }

    public static String addChildValidate(AddChildRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (request.getName().length() < 2 || request.getName().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (!isValidGender(request.getGender())) {
            return "Gender must be Male, Female";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        return "";
    }

    private static boolean isValidGender(String gender) {
        return gender != null &&
                (gender.equalsIgnoreCase("Male") ||
                        gender.equalsIgnoreCase("Female"));
    }
}
