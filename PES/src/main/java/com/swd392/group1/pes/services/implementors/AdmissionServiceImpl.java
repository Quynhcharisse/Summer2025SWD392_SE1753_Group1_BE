package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.email.Format;
import com.swd392.group1.pes.enums.Fees;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.CreateExtraTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.validations.AdmissionValidation.AdmissionFormValidation;
import com.swd392.group1.pes.validations.AdmissionValidation.AdmissionTermValidation;
import com.swd392.group1.pes.validations.AdmissionValidation.ExtraTermValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Override
    public ResponseEntity<ResponseObject> createAdmissionTerm(CreateAdmissionTermRequest request) {
        // 1. Validate các field cơ bản (ngày, số lượng, grade rỗng...)
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
        // 2. Chuyển đổi grade và tính năm hiện tại
        Grade grade = Grade.valueOf(request.getGrade().toUpperCase());
        int currentYear = LocalDate.now().getYear();
        String name = "Admission Term " + grade.getName() + " " + currentYear;

        // 3. Mỗi năm, mỗi grade chỉ được phép có 1 đợt tuyển sinh
        long termCountThisYear = admissionTermRepo.countByYearAndGrade(currentYear, grade);
        if (termCountThisYear >= 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Admission term already exists for grade " + grade + " in year " + currentYear)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // 4. Kiểm tra trùng thời gian với cùng grade
        List<AdmissionTerm> termsWithSameGrade = admissionTermRepo.findByGrade(grade);
        for (AdmissionTerm t : termsWithSameGrade) {
            if (datesOverlap(request.getStartDate(), request.getEndDate(), t.getStartDate(), t.getEndDate())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseObject.builder()
                                .message("Time period overlaps with another term of the same grade")
                                .success(false)
                                .data(null)
                                .build()
                );
            }
        }

        // Nếu hợp lệ, tiếp tục tạo term
        admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name(name)
                        .grade(Grade.valueOf(request.getGrade().toUpperCase()))
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .year(LocalDateTime.now().getYear())
                        .studentsPerClass(20)
                        .expectedClasses(request.getExpectedClasses())
                        .maxNumberRegistration(calculateMaxRegistration(request.getExpectedClasses()))
                        .status(Status.INACTIVE_TERM.getValue())
                        .build()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Create term successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateTermStatus(UpdateAdmissionTermRequest request) {
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

        // 1. Kiểm tra term tồn tại
        AdmissionTerm term = admissionTermRepo.findById(request.getTermId()).orElse(null);
        if (term == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Term not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (!term.getStatus().equals(Status.ACTIVE_TERM.getValue())) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Only active terms can be locked")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        term.setStatus(Status.LOCKED_TERM.getValue());
        admissionTermRepo.save(term);

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Term locked successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private int calculateMaxRegistration(int expectedClasses) {
        return 20 * expectedClasses;
    }

    private boolean datesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    public boolean isValidGrade(String grade) {
        for (Grade g : Grade.values()) {
            if (g.name().equalsIgnoreCase(grade)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade) {
        if (!isValidGrade(grade)) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Invalid grade: " + grade)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Grade g = Grade.valueOf(grade.toUpperCase());
        Map<String, Double> feeData = getFeeMapByGrade(g); // dùng chung
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Default fee fetched successfully")
                        .success(true)
                        .data(feeData)
                        .build()
        );
    }

    private Map<String, Double> getFeeMapByGrade(Grade grade) {
        Fees fee = Fees.valueOf(grade.name());
        return Map.of(
                "learningMaterialFee", fee.getLearningMaterial(),
                "reservationFee", fee.getReservation(),
                "serviceFee", fee.getService(),
                "uniformFee", fee.getUniform(),
                "facilityFee", fee.getFacility()
        );
    }


    @Override
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {

        List<AdmissionTerm> terms = admissionTermRepo.findAllByParentTermIsNull();

        for (AdmissionTerm term : terms) {
            String timeStatus = updateTermStatus(term);

            //if đủ → cần "khóa" lại dù chưa hết hạn
            boolean isFull = countApprovedFormByTerm(term) >= term.getMaxNumberRegistration();

            //term đang ACTIVE nhưng đã đủ số lượng → chuyển sang LOCKED_TERM
            //trường hợp khác giữ nguyên status tính từ thời gian
            String finalStatus = (timeStatus.equals(Status.ACTIVE_TERM.getValue()) && isFull)
                    ? Status.LOCKED_TERM.getValue()
                    : timeStatus;

            if (!term.getStatus().equals(finalStatus)) {
                term.setStatus(finalStatus);
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
                            data.put("year", term.getYear() + "-" + (term.getYear() + 1));
                            data.put("studentsPerClass", term.getStudentsPerClass());
                            data.put("expectedClasses", term.getExpectedClasses());
                            data.put("maxNumberRegistration", term.getMaxNumberRegistration());
                            data.put("approvedForm", countApprovedFormByTerm(term));
                            data.put("grade", term.getGrade());
                            data.put("status", term.getStatus());


                            Map<String, Double> feeList = getFeeMapByGrade(term.getGrade());
                            data.putAll(feeList);

                            //gọi lai extra term
                            if (!admissionTermRepo.findAllByParentTerm_Id(term.getId()).isEmpty()) {
                                data.put("extraTerms", viewExtraTerm(term));
                            }
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

    private String updateTermStatus(AdmissionTerm term) {
        LocalDateTime today = LocalDateTime.now();
        if (!term.getStatus().equals(Status.LOCKED_TERM.getValue())) {
            if (today.isBefore(term.getStartDate())) {
                return Status.INACTIVE_TERM.getValue();
            } else if (!today.isAfter(term.getEndDate())) {
                return Status.ACTIVE_TERM.getValue();
            } else {
                return Status.LOCKED_TERM.getValue();
            }
        }
        return Status.LOCKED_TERM.getValue();
    }

    private List<Map<String, Object>> viewExtraTerm(AdmissionTerm parentTerm) {
        List<Map<String, Object>> extraTermList = admissionTermRepo.findAllByParentTerm_Id(parentTerm.getId()).stream()
                .map(extraTerm -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", extraTerm.getId());
                    data.put("name", extraTerm.getName());
                    data.put("startDate", extraTerm.getStartDate());
                    data.put("endDate", extraTerm.getEndDate());
                    data.put("maxNumberRegistration", extraTerm.getMaxNumberRegistration());
                    data.put("approvedForm", countApprovedFormByTerm(extraTerm));
                    data.put("status", extraTerm.getStatus());
                    return data;
                })
                .toList();

        return extraTermList;
    }


        @Override
        public ResponseEntity<ResponseObject> createExtraTerm(CreateExtraTermRequest request) {

            // 1. Validate các field cơ bản (ngày, số lượng, grade rỗng...)
            String error = ExtraTermValidation.createExtraTerm(request);
            if (!error.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message(error)
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            // 2. Kiểm tra AdmissionTerm tồn tại
            AdmissionTerm term = admissionTermRepo.findById(request.getAdmissionTermId()).orElse(null);
            if (term == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseObject.builder()
                                .message("Admission term not found")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            List<AdmissionTerm> existingExtraTerms = admissionTermRepo.findAllByParentTerm_Id(term.getId())
                    .stream()
                    .filter(et -> !et.getStatus().equals(Status.LOCKED_TERM.getValue()))
                    .toList();

            if (!existingExtraTerms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("Only one extra term can exist at a time")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            // 3. Kiểm tra status và chỉ tiêu
            if (!term.getStatus().equals(Status.LOCKED_TERM.getValue())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("Only locked terms can have extra requests")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            if (countApprovedFormByTerm(term) >= term.getMaxNumberRegistration()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("Term has already reached maximum registration")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            // 4. Tạo extra RequestTerm
            AdmissionTerm extraTerm = admissionTermRepo.save(AdmissionTerm.builder()
                    .name("Extra Term - " + term.getGrade().getName() + " " + term.getYear())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .maxNumberRegistration(countMissingFormAmountByTerm(term))
                    .year(term.getYear())
                    .grade(term.getGrade())
                    .parentTerm(term)
                    .status(Status.INACTIVE_TERM.getValue())
                    .build());

            extraTerm.setStatus(updateTermStatus(extraTerm));
            admissionTermRepo.save(extraTerm);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseObject.builder()
                            .message("Extra term created successfully")
                            .success(true)
                            .data(null)
                            .build()
            );
        }

        private int countApprovedFormByTerm(AdmissionTerm term) {
            return (int) term.getAdmissionFormList().stream().filter(form -> form.getStatus().equals(Status.APPROVED.getValue())).count();
        }

        private int countMissingFormAmountByTerm(AdmissionTerm term) {
            return term.getMaxNumberRegistration() - countApprovedFormByTerm(term);
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
                            data.put("profileImage", form.getStudent().getProfileImage());
                            data.put("householdRegistrationImg", form.getStudent().getHouseholdRegistrationImg());
                            data.put("householdRegistrationAddress", form.getHouseholdRegistrationAddress());
                            data.put("birthCertificateImg", form.getStudent().getBirthCertificateImg());
                            data.put("commitmentImg", form.getCommitmentImg());
                            data.put("childCharacteristicsFormImg", form.getChildCharacteristicsFormImg());
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

        Student student = form.getStudent();
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Form has no associated student.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String parentEmail = form.getParent().getAccount().getEmail();

        if (request.isApproved()) {
            form.setStatus(Status.APPROVED.getValue());
            student.setStudent(true);
            studentRepo.save(student);
            String subject = "[PES] Admission Approved";
            String heading = "🎉 Admission Approved";
            String bodyHtml = Format.getAdmissionApprovedBody(student.getName());
            mailService.sendMail(parentEmail, subject, heading, bodyHtml);

        } else {
            form.setStatus(Status.REJECTED.getValue());
            form.setCancelReason(request.getReason());
            String subject = "[PES] Admission Rejected";
            String heading = "❌ Admission Rejected";
            String bodyHtml = Format.getAdmissionRejectedBody(student.getName(), request.getReason());
            mailService.sendMail(parentEmail, subject, heading, bodyHtml);
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

    @Override
    public ResponseEntity<ResponseObject> getAllYear() {
        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(years)
                        .build()
        );
    }
}
