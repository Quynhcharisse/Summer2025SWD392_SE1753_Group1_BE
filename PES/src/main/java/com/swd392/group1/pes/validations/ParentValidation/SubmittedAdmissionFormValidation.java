package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;

public class SubmittedAdmissionFormValidation {
    public static String validate(SubmitAdmissionFormRequest request) {
        // Không được để trống
        if (request.getHouseholdRegistrationAddress() == null || request.getHouseholdRegistrationAddress().isEmpty()) {
            return "Household registration address is required.";
        }

        // Không được để trống
        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        return "";
    }

}
