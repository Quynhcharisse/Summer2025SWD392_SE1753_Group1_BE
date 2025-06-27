package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;

public class AdmissionTermValidation {
    public static String createTermValidate(CreateAdmissionTermRequest request) {
        if (request.getStartDate() == null) {
            return "Start date and end date are required";
        }

        if (request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        //Ngày bắt đầu phải trước ngày kết thúc
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Start date must be before end date";
        }

        if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        if (request.getExpectedClasses() <= 0) {
            return "Expected classes must be greater than 0";
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
