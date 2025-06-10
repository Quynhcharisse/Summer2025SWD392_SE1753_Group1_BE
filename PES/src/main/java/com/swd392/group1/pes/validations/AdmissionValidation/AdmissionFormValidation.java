package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;

public class AdmissionFormValidation {
    public static String processFormByManagerValidate(ProcessAdmissionFormRequest request, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if (form == null) {
            return "Form not found";
        }

        //Khi approved == false → nghĩa là đơn bị từ chối
        //bắt buộc phải nhap reason
        if (!request.isApproved()) {
            if (request.getReason().trim().isEmpty()) {
                return "Reject reason is required when form is rejected";
            }

            if (request.getReason().length() > 100) {
                return "Reject reason must not exceed 100 characters";
            }
        }
        return "";
    }
}
