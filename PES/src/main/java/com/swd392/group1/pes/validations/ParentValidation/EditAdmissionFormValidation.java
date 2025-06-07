package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;

public class EditAdmissionFormValidation {
    // validation cho cancel đơn
    public static String canceledValidate(int id, Account account, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(id).orElse(null);

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
}
