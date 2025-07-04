package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AdmissionTermValidation {
    public static String createTermValidate(CreateAdmissionTermRequest request, AdmissionTermRepo admissionTermRepo) {
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

        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            return "Start date and end date must be in the same year";
        }

        int year = request.getStartDate().getYear();
        if (admissionTermRepo.existsByYear(year)) {
            return "Admission term for year " + year + " already exists.";
        }

        //trong 1 term phai it nhat 1 grade trong create term do
        if(request.getTermItemList() == null || request.getTermItemList().isEmpty()) {
            return "At least one grade must be included in the term.";
        }

        Set<String> grades = new HashSet<>();

        for (CreateAdmissionTermRequest.TermItem termItem : request.getTermItemList()) {
            //expectedClasses > 0
            if (termItem.getExpectedClasses() <= 0) {
                return "Expected classes must be greater than 0 for grade: " + termItem.getGrade();
            }

            //hợp lệ enum
            try {
                Grade.valueOf(termItem.getGrade());
            } catch (IllegalArgumentException e) {
                return "Invalid grade: " + termItem.getGrade();
            }

            if (!grades.add(termItem.getGrade())) {
                return "Duplicate grade found: " + termItem.getGrade();
            }
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
