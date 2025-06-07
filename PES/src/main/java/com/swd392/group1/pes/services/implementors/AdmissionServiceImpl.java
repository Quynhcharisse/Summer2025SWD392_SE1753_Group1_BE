package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionFeeRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.validations.AdmissionValidation.AdmissionTermValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionTermRepo admissionTermRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final StudentRepo studentRepo;

    //------------------------------\\ create admission term //------------------------------\\

    @Override
    public ResponseEntity<ResponseObject> createAdmissionTerm(AdmissionTermRequest request) {

        String error = AdmissionTermValidation.validate(request);

        if (!error.isEmpty()) {
            ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.save(AdmissionTerm.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .year(request.getYear())
                .maxNumberRegistration(request.getMaxNumberRegistration())
                .grade(Grade.valueOf(request.getGrade().toUpperCase()))
                .status(Status.INACTIVE_TERM.getValue())
                .build());

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Create admission term successfully")
                        .success(true)
                        .data(term)
                        .build()
        );
    }

    //------------------------------\\ view admission term //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {

        List<AdmissionTerm> terms = admissionTermRepo.findAll();

        LocalDate today = LocalDate.now();

        for (AdmissionTerm term : terms) {
            String updateStatus = updateTermStatus(term, today);
            if (!term.getStatus().equals(updateStatus)) {
                term.setStatus(updateStatus);
            }
        }
        admissionTermRepo.saveAll(terms);

        List<Map<String, Object>> result = terms.stream()
                .map(
                        term -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", term.getId());
                            data.put("name", term.getName());
                            data.put("startDate", term.getStartDate());
                            data.put("endDate", term.getEndDate());
                            data.put("year", term.getYear());
                            data.put("maxNumberRegistration", term.getMaxNumberRegistration());
                            data.put("grade", term.getGrade().toString());
                            data.put("status", term.getStatus());
                            return data;
                        }
                )
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(result)
                        .build()
        );
    }

    private String updateTermStatus(AdmissionTerm term, LocalDate today) {
        if (today.isBefore(term.getStartDate())) {
            return Status.INACTIVE_TERM.getValue();
        } else if (!today.isAfter(term.getEndDate())) {
            return Status.ACTIVE_TERM.getValue();
        } else {
            return Status.LOCKED_TERM.getValue();
        }
    }
    //------------------------------\\ update admission term //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> updateAdmissionTerm(AdmissionTermRequest request) {

        String error = AdmissionTermValidation.validate(request);

        if (!error.isEmpty()) {
            ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.findById(request.getId()).orElse(null);

        if (term == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setYear(request.getYear());
        term.setMaxNumberRegistration(request.getMaxNumberRegistration());
        term.setGrade(Grade.valueOf(request.getGrade().toLowerCase()));
        admissionTermRepo.save(term);


        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Update term successfully")
                        .success(true)
                        .data(term)
                        .build()
        );
    }

    //------------------------------\\ view admission form list //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList() {
        List<Map<String, Object>> formList = admissionFormRepo.findAll().stream()
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed()) // sort form theo ngày chỉnh sửa mới nhất
                .map(form -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", form.getId());
                            data.put("studentId", form.getStudent().getId());
                            data.put("studentName", form.getStudent().getName());
                            data.put("studentGender", form.getStudent().getGender());
                            data.put("studentDateOfBirth", form.getStudent().getDateOfBirth());
                            data.put("studentPlaceOfBirth", form.getStudent().getPlaceOfBirth());
                            data.put("profileImage", form.getProfileImage());
                            data.put("householdRegistrationAddress", form.getHouseholdRegistrationAddress());
                            data.put("householdRegistrationImg", form.getHouseholdRegistrationImg());
                            data.put("birthCertificateImg", form.getBirthCertificateImg());
                            data.put("commitmentImg", form.getCommitmentImg());
                            data.put("submittedDate", form.getSubmittedDate());
                            data.put("cancelReason", form.getCancelReason());
                            data.put("note", form.getNote());
                            data.put("status", form.getStatus());
                            return data;
                        }
                )
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(formList)
                        .build()
        );
    }

    //------------------------------\\ process admission form //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request) {
        String error = AdmissionTermValidation.processFormByManagerValidate(request, admissionFormRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if (form == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Form not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        if (request.isApproved()) {
            form.setStatus(Status.APPROVED.getValue());

            if(form.getStudent() != null) {
                Student student = form.getStudent();
                student.setStudent(true);
                studentRepo.save(student);
            }
        } else {
            form.setStatus(Status.REJECTED.getValue());
            form.setCancelReason(request.getReason());
        }

        admissionFormRepo.save(form);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message(request.isApproved() ? "Form Approved" : "Form Rejected")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    //----------------------------- Configuration Admission Fee -------------------------------//
    @Override
    public ResponseEntity<ResponseObject> updateAdmissionFee(UpdateAdmissionFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFee(int term) {
        return null;
    }
}
