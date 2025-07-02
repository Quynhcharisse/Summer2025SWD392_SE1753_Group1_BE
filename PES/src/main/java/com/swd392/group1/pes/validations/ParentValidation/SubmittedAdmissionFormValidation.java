package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class SubmittedAdmissionFormValidation {
    public static String validate(SubmitAdmissionFormRequest request, StudentRepo studentRepo, TermItemRepo termItemRepo, AdmissionFormRepo admissionFormRepo) {
        Student student = studentRepo.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return "Student not found after successful validation. This indicates a logical error.";
        }

        if (!isAgeValidForGrade(student.getDateOfBirth())) {
            return "Student's age (" + calculateAge(student.getDateOfBirth()) + " years) does not meet the required age for admission (3-5 years).";
        }

        Grade grade = calculateAge(student.getDateOfBirth()) == 3 ? Grade.SEED : (calculateAge(student.getDateOfBirth()) == 4 ? Grade.BUD : Grade.LEAF);
        List<TermItem> activeTermItemList = termItemRepo.findAllByGradeAndStatusAndAdmissionTerm_Year(grade, Status.ACTIVE_TERM_ITEM, LocalDate.now().getYear());
        System.out.println("List ACTIVE TERM: " + activeTermItemList.size());
        if (activeTermItemList.isEmpty()) {
            return "Active Term Item not found after successful validation. This indicates a logical error.";
        }

        TermItem activeTermItem = activeTermItemList.get(0);

        if (!activeTermItem.getStatus().equals(Status.ACTIVE_TERM_ITEM) || activeTermItem.getAdmissionTerm() == null || !activeTermItem.getAdmissionTerm().getStatus().equals(Status.ACTIVE_TERM)) {
            return "The admission term item is not currently open for new admissions or is invalid.";
        }

        List<Status> statusesToExcludeForNewSubmission = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<AdmissionForm> activeOrPendingForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusNotIn(
                student.getId(), activeTermItem.getId(), statusesToExcludeForNewSubmission
        );

        if (!activeOrPendingForms.isEmpty()) {
            boolean hasPendingForm = activeOrPendingForms.stream()
                    .anyMatch(form -> form.getStatus().equals(Status.PENDING_APPROVAL));
            if (hasPendingForm) {
                return "This student already has a pending admission form for the current term. New submission is not allowed.";
            }
            return "This student already has an active or processed admission form for the current term. New submission is not allowed.";
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

        if (isNotValidImage(request.getCommitmentImg())) {
            return "Commitment image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getChildCharacteristicsFormImg() == null) {
            return "Child characteristics form image is required.";
        }

        if (isNotValidImage(request.getChildCharacteristicsFormImg())) {
            return "Child characteristics form image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getNote() != null && request.getNote().length() > 300) {
            return "Note must not exceed 300 characters.";
        }

        return "";
    }

    private static boolean isNotValidImage(String fileName) {
        return fileName == null || fileName.trim().isEmpty() || !fileName.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    public static int calculateAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.YEARS.between(dob, today);
    }

    private static boolean isAgeValidForGrade(LocalDate dob) {
        int age = calculateAge(dob);
        return age >= 3 && age <= 5;
    }

}

