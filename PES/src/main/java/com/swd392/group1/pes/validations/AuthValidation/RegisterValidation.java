package com.swd392.group1.pes.validations.AuthValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RegisterRequest;

import java.util.regex.Pattern;

public class RegisterValidation {
    public static String validate(RegisterRequest request, AccountRepo accountRepo) {

        //email ko bi trong
        if (request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        //email hop le
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            return "Invalid email format";
        }

        //email ton tai
        if (accountRepo.existsByEmail(request.getEmail())) {
            return "Email is already registered";
        }

        //Password ko de trong
        if (request.getPassword().trim().isEmpty()) {
            return "Password is required";
        }

        //Password hop le
        if (request.getPassword().length() < 8) {
            return "Password must be at least 8 characters";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one digit";
        }

        if (!lowerCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one lowercase letter";
        }

        if (!upperCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one uppercase letter";
        }

        if (!specialPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one special character";
        }

        //Confirm password ko duoc trong
        if (request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm Password is required";
        }

        //Confirm password = password
        if (!request.getConfirmPassword().equals(request.getPassword())){
            return "Confirm password must be the same as password";
        }

        //Name ko de trong
        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        //Name chi co letter, space
        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        //Name need to be more than 2 and less than 50
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        //Phone ko co trong
        if (request.getPhone().trim().isEmpty()) {
            return "Phone number is required";
        }

        //Phone hop le
        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits";
        }

        //Gender ko trong
        if (request.getGender().trim().isEmpty()) {
            return "Gender is required";
        }

        if (!request.getGender().trim().equals("male") && !request.getGender().trim().equals("female") && !request.getGender().trim().equals("other")) {
            return "Gender must be male, female, or other";
        }

        //Id number ko trong
        if (request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required";
        }

        //Id number phai hop le
        Pattern idPattern = Pattern.compile("^\\d{12}$");
        if (!idPattern.matcher(request.getIdentityNumber()).matches()) {
            return "Identity number must have 12 digits";
        }

        return "";
    }
}
