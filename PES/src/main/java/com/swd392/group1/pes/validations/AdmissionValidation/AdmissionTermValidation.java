package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;

import java.time.LocalDate;

public class AdmissionTermValidation {
    public static String validate(AdmissionTermRequest request) {
        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date is required or end date is required";
        }

        //end date ko dc qua nam tuyen sinh
        if (request.getEndDate().getYear() ==  LocalDate.now().getYear()) {
            return "End date must be in year " + LocalDate.now().getYear();
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
        return "";
    }

    public static String processFormByManagerValidate(ProcessAdmissionFormRequest request, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if(form == null) {
            return "Form not found";
        }

        //Khi approved == false → nghĩa là đơn bị từ chối
        //bắt buộc phải nhap reason
        if(!request.isApproved()) {
            if(request.getReason().trim().isEmpty()) {
                return "Reject reason is required when form is rejected";
            }

            if (request.getReason().length() > 100) {
                return "Reject reason must not exceed 100 characters";
            }
        }
        return "";
    }
}
