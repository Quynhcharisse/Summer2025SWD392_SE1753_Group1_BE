package com.swd392.group1.pes.validations.AdmissionValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.requests.CreateExtraTermRequest;

import java.util.List;

public class ExtraTermValidation {
    public static String createExtraTerm(CreateExtraTermRequest request, AdmissionTermRepo admissionTermRepo) {
        AdmissionTerm parentTerm = admissionTermRepo.findById(request.getParentTermId()).orElse(null);
        if (parentTerm == null) {
            return "Parent term is required.";
        }

        // tạo extra term khi term cha bị lock
        if (!parentTerm.getStatus().equals(Status.LOCKED_TERM)) {
            return "Only locked terms can have extra requests.";
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required.";
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date.";
        }

        // ko cho tạo trùng tg với term ba / extra term khác cùng  năm
        //Nếu chưa có học sinh nào được duyệt (APPROVED)
        // tạo ExtraTerm là phi logic vì đợt chính còn chưa có kết quả
        List<AdmissionTerm> termsSameYear = admissionTermRepo.findAllByYear(parentTerm.getYear());
        for (AdmissionTerm existing : termsSameYear) {
            if (existing.getId().equals(parentTerm.getId())) continue; // bỏ qua term cha

            boolean overlap = !(request.getEndDate().isBefore(existing.getStartDate()) ||
                    request.getStartDate().isAfter(existing.getEndDate()));
            if (overlap) {
                return "Extra term time overlaps with existing term: " + existing.getName();
            }
        }

        List<AdmissionTerm> existingActiveExtraTerms = admissionTermRepo.findAllByParentTerm_Id(parentTerm.getId())
                .stream()
                .filter(et -> !et.getStatus().equals(Status.LOCKED_TERM))
                .toList();

        if (!existingActiveExtraTerms.isEmpty()) {
            return ("Only one active extra term can exist at a time");
        }

        if (request.getMaxNumberRegistration() <= 0) {
            return "Maximum number of registrations must be greater than 0.";
        }
        return "";
    }
}
