package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.requests.RefillFormRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class RefillFormValidation {
    public static String validate(RefillFormRequest request, StudentRepo studentRepo, TermItemRepo termItemRepo, AdmissionFormRepo admissionFormRepo) {
        Student student = studentRepo.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return "Student not found";
        }

        if (!isAgeValidForGrade(student.getDateOfBirth())) {
            return "Student's age (" + calculateAge(student.getDateOfBirth()) + " years) does not meet the required age for admission (3-5 years).";
        }

        TermItem activeTermItem = termItemRepo.findById(request.getTermItemId()).orElse(null);
        if (activeTermItem == null) {
            return "The admission term item was not found.";
        }

        if (!activeTermItem.getStatus().equals(Status.ACTIVE_TERM_ITEM) ||
                activeTermItem.getAdmissionTerm() == null ||
                !activeTermItem.getAdmissionTerm().getStatus().equals(Status.ACTIVE_TERM)) {
            return "The admission term item is not currently open for registration or is invalid.";
        }

        List<Status> statusesToExclude = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<AdmissionForm> nonRejectedOrCancelledForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusNotIn(
                student.getId(), activeTermItem.getId(), statusesToExclude
        );

        if (!nonRejectedOrCancelledForms.isEmpty()) {
            boolean hasPendingForm = nonRejectedOrCancelledForms.stream()
                    .anyMatch(form -> form.getStatus().equals(Status.PENDING_APPROVAL));

            if (hasPendingForm) {
                return "This student already has a pending admission form for the current term. Please wait for approval or cancel that form if you wish to resubmit.";
            } else {
                return "This student already has an active or processed admission form for the current term. Refilling is not allowed.";
            }
        }

        List<Status> statusesToInclude = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<AdmissionForm> rejectedOrCancelledForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusIn(
                student.getId(), activeTermItem.getId(), statusesToInclude
        );

        long rejectedOrCancelledCount = rejectedOrCancelledForms.size();

        if (rejectedOrCancelledCount == 0) {
            return "No rejected or cancelled admission form found for this student in the current term to refill. If you wish to submit a new form, please use the 'submit' function.";
        }

        if (rejectedOrCancelledCount > 1) {
            return "Multiple rejected or cancelled forms found for this student in the current term. Please contact support for clarification or manually select which form to refill.";
        }

        if (request.getHouseholdRegistrationAddress() == null || request.getHouseholdRegistrationAddress().trim().isEmpty()) {
            return "Household registration address is required.";
        }

        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        if (request.getCommitmentImg() == null) {
            return "Commitment image is required.";
        }

        if (!isValidImage(request.getCommitmentImg())) {
            return "Commitment image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getChildCharacteristicsFormImg() == null) {
            return "Child characteristics form image is required.";
        }

        if (!isValidImage(request.getChildCharacteristicsFormImg())) {
            return "Child characteristics form image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getNote() != null && request.getNote().length() > 300) {
            return "Note must not exceed 300 characters.";
        }

        return "";
    }

    private static boolean isValidImage(String fileName) {
        return fileName.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    private static int calculateAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.YEARS.between(dob, today);
    }

    private static boolean isAgeValidForGrade(LocalDate dob) {
        int age = calculateAge(dob);
        return age >= 3 && age <= 5;
    }
}
