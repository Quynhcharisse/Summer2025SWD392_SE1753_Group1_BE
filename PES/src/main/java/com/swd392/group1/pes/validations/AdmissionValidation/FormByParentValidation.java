package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.requests.CancelAdmissionForm;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;

import java.time.LocalDate;

public class FormByParentValidation {

    public static String canceledValidate(CancelAdmissionForm request, Account account, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if (form == null) {
            return "Admission form not found.";
        }

        if (!form.getParent().getId().equals(account.getParent().getId())) {
            return "You do not have permission to access this form.";
        }

        if (!form.getStatus().equals(Status.PENDING_APPROVAL.getValue())) {
            return "Forms in PENDING APPROVAL status can be cancelled.";
        }
        return "";
    }

    public static String submittedForm(SubmitAdmissionFormRequest request) {
        if (request.getName().trim().isEmpty()) {
            return "Child name is required.";
        }

        if (!request.getName().contains(" ")) {
            return "Child name must include both first name and last name.";
        }

        if (request.getName().length() > 50) {
            return "Child name must not exceed 50 characters.";
        }

        if (!request.getName().matches("^[a-zA-ZÀ-Ỵà-ỵ\s]+$")) {
            return "Child name must not contain digits or special characters.";
        }

        if (request.getGender().trim().isEmpty()) {
            return "Child gender is required.";
        }

        // kiểm tra ngày sinh > hôm nay
        if (request.getDateOfBirth() == null) {
            return "Date of birth is required.";
        }

        // kiểm tra ngày sinh > hôm nay
        int age = LocalDate.now().getYear() - request.getDateOfBirth().getYear();
        if (!(age >= 3 && age <= 5)) {
            return "Child must between 3 to 5 years old.";
        }

        if (request.getPlaceOfBirth().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must not exceed 100 characters.";
        }

        if (request.getHouseholdRegistrationAddress().trim().isEmpty()) {
            return "Household registration address is required.";
        }

        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        if (request.getProfileImage().trim().isEmpty()) {
            return "Profile image is required.";
        }

        if (request.getHouseholdRegistrationImg().trim().isEmpty()) {
            return "Household registration image is required.";
        }

        if (request.getBirthCertificateImg().trim().isEmpty()) {
            return "Birth certificate image is required.";
        }

        if (request.getCommitmentImg().trim().isEmpty()) {
            return "Commitment image is required.";
        }

        String imgError;

        imgError = validateImageField("Profile image", request.getProfileImage());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Household registration image", request.getHouseholdRegistrationImg());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Birth certificate image", request.getBirthCertificateImg());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Commitment image", request.getCommitmentImg());
        if (!imgError.isEmpty()) return imgError;

        return "";
    }

    private static String validateImageField(String img, String value) {

        if (!value.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp)$")) {
            return img + " must be a valid image file (.jpg, .png, .jpeg, .gif, .bmp).";
        }
        return "";
    }
}
