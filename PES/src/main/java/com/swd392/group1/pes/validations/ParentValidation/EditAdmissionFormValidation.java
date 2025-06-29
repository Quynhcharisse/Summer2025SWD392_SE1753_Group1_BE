package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.requests.CancelAdmissionForm;

public class EditAdmissionFormValidation {
    // validation cho cancel đơn
    public static String canceledValidate(CancelAdmissionForm request, Account account, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);

        if (form == null) {
            return "Admission form not found.";
        }

        if (!form.getParent().getId().equals(account.getParent().getId())) {
            return "You do not have permission to access this form.";
        }

        if (!form.getStatus().equals(Status.PENDING_APPROVAL)) {
            return "Only pending-approval forms can be cancelled.";
        }
        return "";
    }
}
