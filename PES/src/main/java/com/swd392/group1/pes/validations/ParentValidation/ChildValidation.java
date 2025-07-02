package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.requests.AddChildRequest;
import com.swd392.group1.pes.requests.UpdateChildRequest;

import java.time.LocalDate;
import java.time.Period;

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
            return "Gender must be Male, Female";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 3 || age > 5) {
            return "Child's age must be between 3 and 5 years.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        if (request.getProfileImage() == null || request.getProfileImage().isEmpty()) {
            return "Profile image is required.";
        }

        if (request.getHouseholdRegistrationImg() == null || request.getHouseholdRegistrationImg().isEmpty()) {
            return "Household registration image is required.";
        }

        if (request.getBirthCertificateImg() == null || request.getBirthCertificateImg().isEmpty()) {
            return "Birth certificate image is required.";
        }

        String imgError;

        imgError = validateImageField("Profile image", request.getProfileImage());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Household registration image", request.getHouseholdRegistrationImg());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Birth certificate image", request.getBirthCertificateImg());
        if (!imgError.isEmpty()) return imgError;

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
            return "Gender must be Male or Female.";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 3 || age > 5) {
            return "Child's age must be between 3 and 5 years.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        String[] images = {
                request.getProfileImage(),
                request.getHouseholdRegistrationImg(),
                request.getBirthCertificateImg(),
        };
        String[] imageNames = {
                "Profile image",
                "Household registration image",
                "Birth certificate image",
                "Commitment image"
        };

        for (int i = 0; i < images.length; i++) {
            String error = validateImageField(imageNames[i], images[i]);
            if (!error.isEmpty()) return error;
        }

        return "";
    }

    private static boolean isValidGender(String gender) {
        return gender != null && (
                gender.equalsIgnoreCase("Male") ||
                        gender.equalsIgnoreCase("Female")
        );
    }

    private static String validateImageField(String name, String value) {
        if (value == null || value.isEmpty()) {
            return name + " is required.";
        }
        if (!value.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            return name + " must be a valid image file (.jpg, .png, .jpeg, .gif, .bmp, .webp).";
        }
        return "";
    }
}
