package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.requests.CreateExtraTermRequest;

import java.util.List;

public class ExtraTermValidation {
    public static String createExtraTerm(CreateExtraTermRequest request, AdmissionTermRepo admissionTermRepo) {
        // 2. Kiểm tra parent term tồn tại
        AdmissionTerm parentTerm = admissionTermRepo.findById(request.getParentTermId()).orElse(null);
        if (parentTerm == null) {
            return "Parent term is required.";
        }

        List<AdmissionTerm> existingActiveExtraTerms = admissionTermRepo.findAllByParentTerm_Id(parentTerm.getId())
                .stream()
                .filter(et -> !et.getStatus().equals(Status.LOCKED_TERM))
                .toList();

        if (!existingActiveExtraTerms.isEmpty()) {
            return ("Only one active extra term can exist at a time");
        }


        // 3. Kiểm tra status và chỉ tiêu
        if (!parentTerm.getStatus().equals(Status.LOCKED_TERM)) {
            return ("Only locked terms can have extra requests");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required.";
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date.";
        }

        if (request.getMaxNumberRegistration() <= 0) {
            return "Maximum number of registrations must be greater than 0.";
        }
        return "";
    }
}
