package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionFee;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.repositories.AdmissionFeeRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.validations.AdmissionValidation.AdmissionFormValidation;
import com.swd392.group1.pes.validations.AdmissionValidation.AdmissionTermValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {
    private final StudentRepo studentRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final MailService mailService;
    private final AdmissionFeeRepo admissionFeeRepo;

    @Override
    public ResponseEntity<ResponseObject> createAdmissionTerm(CreateAdmissionTermRequest request) {

        String error = AdmissionTermValidation.createTermValidate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name(request.getName())
                        .grade(Grade.valueOf(request.getGrade()))
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .year(request.getYear())
                        .maxNumberRegistration(request.getMaxNumberRegistration())
                        .status(Status.INACTIVE_TERM.getValue())
                        .build()
        );


        admissionFeeRepo.save(AdmissionFee.builder()
                .admissionTerm(term)
                .reservationFee(request.getReservationFee())
                .serviceFee(request.getServiceFee())
                .uniformFee(request.getUniformFee())
                .learningMaterialFee(request.getLearningMaterialFee())
                .facilityFee(request.getFacilityFee())
                .build()
        );


        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Create term and fee successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }


    @Override
    public ResponseEntity<ResponseObject> viewAdmissionTerm(int year) {

        List<AdmissionTerm> terms = admissionTermRepo.findAll();

        LocalDateTime today = LocalDateTime.now();

        for (AdmissionTerm term : terms) {
            String updateStatus = updateTermStatus(term, today);
            if (!term.getStatus().equals(updateStatus)) {
                term.setStatus(updateStatus);
                admissionTermRepo.save(term);
            }
        }

        List<Map<String, Object>> termList = terms.stream()
                .map(term -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", term.getId());
                            data.put("name", term.getName());
                            data.put("startDate", term.getStartDate());
                            data.put("endDate", term.getEndDate());
                            data.put("year", term.getYear());
                            data.put("maxNumberRegistration", term.getMaxNumberRegistration());
                            data.put("grade", term.getGrade());
                            data.put("status", term.getStatus());
                            data.put("formList", getFormListByTerm(term));

                            AdmissionFee fee = admissionFeeRepo.findByAdmissionTerm_Id(term.getId()).orElse(null);
                            data.put("fee", fee != null ? buildFeeMap(fee) : null);

                            return data;
                        }
                )
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(termList)
                        .build()
        );
    }

    private String updateTermStatus(AdmissionTerm term, LocalDateTime today) {
        if (today.isBefore(term.getStartDate())) {
            return Status.INACTIVE_TERM.getValue();
        } else if (!today.isAfter(term.getEndDate())) {
            return Status.ACTIVE_TERM.getValue();
        } else {
            return Status.LOCKED_TERM.getValue();
        }
    }

    private List<Map<String, Object>> getFormListByTerm(AdmissionTerm term) {
        return term.getAdmissionFormList().stream()
                .map(form -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", form.getId());
                            data.put("householdRegistrationAddress", form.getHouseholdRegistrationAddress());
                            data.put("profileImage", form.getProfileImage());
                            data.put("birthCertificateImg", form.getBirthCertificateImg());
                            data.put("householdRegistrationImg", form.getHouseholdRegistrationImg());
                            data.put("commitmentImg", form.getCommitmentImg());
                            data.put("cancelReason", form.getCancelReason());
                            data.put("submittedDate", form.getSubmittedDate());
                            data.put("note", form.getNote());
                            data.put("status", form.getStatus());
                            data.put("studentName", form.getStudent().getName());
                            return data;
                        }
                )
                .toList();
    }

    private Map<String, Object> buildFeeMap(AdmissionFee fee) {
        Map<String, Object> map = new HashMap<>();
        map.put("reservationFee", fee.getReservationFee());
        map.put("serviceFee", fee.getServiceFee());
        map.put("uniformFee", fee.getUniformFee());
        map.put("learningMaterialFee", fee.getLearningMaterialFee());
        map.put("facilityFee", fee.getFacilityFee());
        return map;
    }


    @Override
    public ResponseEntity<ResponseObject> updateAdmissionTerm(UpdateAdmissionTermRequest request) {

        String error = AdmissionTermValidation.updateTermValidate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.findById(request.getId()).orElse(null);

        if (term == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Term not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (!term.getStatus().equals(Status.INACTIVE_TERM.getValue())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Only inactive terms can be updated")
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


        AdmissionFee fee = admissionFeeRepo.findByAdmissionTerm_Id(request.getId()).orElse(null);
        if (fee == null) {
            fee = AdmissionFee.builder()
                    .admissionTerm(term)
                    .build();
        }
        fee.setReservationFee(request.getReservationFee());
        fee.setServiceFee(request.getServiceFee());
        fee.setUniformFee(request.getUniformFee());
        fee.setLearningMaterialFee(request.getLearningMaterialFee());
        fee.setFacilityFee(request.getFacilityFee());
        admissionFeeRepo.save(fee);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Update term and fee successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }


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

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(formList)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request) {
        String error = AdmissionFormValidation.processFormByManagerValidate(request, admissionFormRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if (form == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Form not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //lấy email ph từ account
        String parentEmail = form.getParent().getAccount().getEmail();//account phải có email

        if (form.getStudent() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Form has no associated student.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (request.isApproved()) {
            form.setStatus(Status.APPROVED.getValue());

            Student student = form.getStudent();
            student.setStudent(true);// Đánh dấu đã trở thành học sinh chính thức
            studentRepo.save(student);

            //gửi email thành công
            mailService.sendMail(
                    parentEmail,
                    "Admission Approved",
                    "Congratulations!\n\nThe admission form for " + form.getStudent().getName() +
                            " has been approved.\nWe look forward to seeing you at our school!"
            );
        } else {
            form.setStatus(Status.REJECTED.getValue());
            form.setCancelReason(request.getReason());

            //gửi email từ chối
            mailService.sendMail(
                    parentEmail,
                    "Admission Rejected",
                    "We're sorry.\n\nThe admission form for " + form.getStudent().getName() +
                            " has been rejected.\nReason: " + request.getReason()
            );
        }

        admissionFormRepo.save(form);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message(request.isApproved() ? "Form Approved" : "Form Rejected")
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
