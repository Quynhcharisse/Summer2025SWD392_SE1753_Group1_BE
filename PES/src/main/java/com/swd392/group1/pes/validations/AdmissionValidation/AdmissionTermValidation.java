package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;

import java.time.LocalDateTime;

public class AdmissionTermValidation {
    public static String createTermValidate(CreateAdmissionTermRequest request) {
        if (request.getStartDate() == null) {
            return "Start date is required";
        }

        if (request.getEndDate() == null) {
            return "End date is required";
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Start date must be before end date";
        }

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            return "Start date must be in the future";
        }

        return "";
    }

    public static String updateTermValidate(UpdateAdmissionTermRequest request) {

        System.out.println("Term ID: " + request.getTermId());
        if (request.getTermId() <= 0) {
            return "Term ID must be a positive number";
        }
        return "";
    }
}
