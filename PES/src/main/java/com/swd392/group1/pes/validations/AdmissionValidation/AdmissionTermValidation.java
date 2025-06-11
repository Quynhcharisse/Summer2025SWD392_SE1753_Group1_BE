package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;

import java.time.LocalDate;

public class AdmissionTermValidation {
    public static String createTermValidate(CreateAdmissionTermRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date is required or end date is required";
        }

        if (request.getMaxNumberRegistration() <= 0 || request.getMaxNumberRegistration() > 1000) {
            return "Max number registration must be greater than 0 and less than 1000";
        }

        if (request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        if (request.getReservationFee() < 0) {
            return "Reservation fee must be non-negative";
        }
        if (request.getServiceFee() < 0) {
            return "Service fee must be non-negative";
        }
        if (request.getUniformFee() < 0) {
            return "Uniform fee must be non-negative";
        }
        if (request.getLearningMaterialFee() < 0) {
            return "Learning material fee must be non-negative";
        }
        if (request.getFacilityFee() < 0) {
            return "Facility fee must be non-negative";
        }

        return "";
    }

    public static String updateTermValidate(UpdateAdmissionTermRequest request) {
        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date is required or end date is required";
        }

        //end date ko dc qua nam tuyen sinh
        //Tránh trường hợp tạo đợt tuyển sinh cho năm 2026
        // nhưng lại kết thúc trong 2025 hoặc 2027 — điều này sẽ gây mâu thuẫn trong hệ thống
        if (request.getEndDate().getYear() !=  request.getYear()) {
            return "End date must be in year " + request.getYear();
        }

        if (!request.getStartDate().isBefore(request.getEndDate())) {
            return "Start date must be before end date";
        }

        if (request.getYear() <= LocalDate.now().getYear()) {// 2026
            return "Year must be greater than " + LocalDate.now().getYear();
        }

        if (request.getMaxNumberRegistration() <= 0 || request.getMaxNumberRegistration() > 1000) {
            return "Max number registration must be greater than 0 and less than 1000";
        }

        if (request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        if (request.getReservationFee() < 0) {
            return "Reservation fee must be non-negative";
        }
        if (request.getServiceFee() < 0) {
            return "Service fee must be non-negative";
        }
        if (request.getUniformFee() < 0) {
            return "Uniform fee must be non-negative";
        }
        if (request.getLearningMaterialFee() < 0) {
            return "Learning material fee must be non-negative";
        }
        if (request.getFacilityFee() < 0) {
            return "Facility fee must be non-negative";
        }

        return "";
    }
}
