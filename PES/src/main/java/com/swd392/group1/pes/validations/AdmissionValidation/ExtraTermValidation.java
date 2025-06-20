package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.requests.CreateExtraTermRequest;

public class ExtraTermValidation {
    public static String createExtraTerm(CreateExtraTermRequest request) {
        if (request.getAdmissionTermId() == null) {
            return "Admission term ID is required.";
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required.";
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date.";
        }

        if (request.getMaxNumberRegistration() <= 0) {
            return "Maximum number of registrations must be greater than 0.";
        }
        return "";
    }
}
