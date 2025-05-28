package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.UpdateProfileRequest;

import java.util.regex.Pattern;

public class UpdateProfileValidation {
    public static String validate(UpdateProfileRequest request, AccountRepo accountRepo) {

        Account account = accountRepo.findByEmailAndStatus(request.getEmail().trim(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (account == null) {
            return "Account not available";
        }

        //Name ko de trong
        if (request.getName().isEmpty()) {
            return "Name is required";
        }

        //Name chi co letter, space
        if (!request.getName().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        //Name need to be more than 2 and less than 50
        if (request.getName().length() < 2 || request.getName().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        //Phone ko co trong
        if (request.getPhone().isEmpty()) {
            return "Phone number is required";
        }

        //Phone hop le
        if (!request.getPhone().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits";
        }

        //Gender ko trong
        if (request.getGender().isEmpty()) {
            return "Gender is required";
        }

        if (!request.getGender().equals("male") && !request.getGender().equals("female") && !request.getGender().equals("other")) {
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
