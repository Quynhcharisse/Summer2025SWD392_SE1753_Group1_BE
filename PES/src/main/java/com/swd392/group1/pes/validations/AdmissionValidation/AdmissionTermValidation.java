package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;

import java.time.LocalDateTime;

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

        //Ngày bắt đầu không được ở trong quá khứ
        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            return "Start date must not be in the past";
        }

        if (request.getMaxNumberRegistration() <= 0 || request.getMaxNumberRegistration() > 1000) {
            return "Max number registration must be greater than 0 and less than 1000";
        }

        if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        return "";
    }

    public static String updateTermValidate(UpdateAdmissionTermRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        //Ngày bắt đầu phải trước ngày kết thúc
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Start date must be before end date";
        }

        //Ngày bắt đầu không được ở trong quá khứ
        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            return "Start date must not be in the past";
        }

        if (request.getMaxNumberRegistration() <= 0 || request.getMaxNumberRegistration() > 1000) {
            return "Max number registration must be greater than 0 and less than 1000";
        }

        if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        return "";
    }
}
