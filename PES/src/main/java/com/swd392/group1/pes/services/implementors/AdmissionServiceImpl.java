package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.email.Format;
import com.swd392.group1.pes.enums.Fees;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.enums.StudentPerClass;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {
    private final StudentRepo studentRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final TermItemRepo termItemRepo;
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

        int currentYear = LocalDate.now().getYear();
        String name = "Admission Term for " + currentYear;

        // --- Bổ sung: Kiểm tra xem term cho năm hiện tại đã tồn tại chưa ---
        // Sử dụng findByYear thay vì existsByYear để có thể trả về thông tin term nếu cần
        Optional<AdmissionTerm> existingTerm = admissionTermRepo.findByYear(currentYear);
        if (existingTerm.isPresent()) {
            // Nếu term cho năm hiện tại đã tồn tại, trả về lỗi Conflict (409)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Admission Term for the year " + currentYear + " already exists. Only one term can be created per year.")
                            .success(false)
                            .data(existingTerm.get().getId()) // Có thể trả về ID của term đã tồn tại
                            .build()
            );
        }

        // Nếu hợp lệ, tiếp tục tạo term
        AdmissionTerm term = admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name(name)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .year(LocalDateTime.now().getYear())
                        .status(Status.INACTIVE_TERM)
                        .build()
        );
        return createTermItem(request.getTermItemList(), term);
    }


    private ResponseEntity<ResponseObject> createTermItem(List<CreateAdmissionTermRequest.TermItem> termItemList, AdmissionTerm term) {

        for (CreateAdmissionTermRequest.TermItem termItem : termItemList) {
            termItemRepo.save(
                    TermItem.builder()
                            .grade(Grade.valueOf(termItem.getGrade()))
                            .studentsPerClass(StudentPerClass.MAX.getValue())
                            .expectedClasses(termItem.getExpectedClasses())
                            .maxNumberRegistration(calculateMaxRegistration(termItem.getExpectedClasses()))
                            .admissionTerm(term)
                            .status(Status.INACTIVE_TERM_ITEM)
                            .currentRegisteredStudents(0)
                            .build()
            );
        }

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

        if (!term.getStatus().equals(Status.ACTIVE_TERM)) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Only active terms can be locked")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        term.setStatus(Status.LOCKED_TERM);

        for (TermItem termItem : term.getTermItemList()) {
            termItem.setStatus(Status.LOCKED_TERM_ITEM);
        }

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
        return StudentPerClass.MAX.getValue() * expectedClasses;
    }

    private boolean datesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    @Override
    public ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade) {
        try {
            Grade g = Grade.valueOf(grade.toUpperCase());
            Map<String, Double> feeData = getFeeMapByGrade(g); // dùng chung
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message("Default fee fetched successfully")
                            .success(true)
                            .data(feeData)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message("Invalid grade: " + grade)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

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
            Status timeStatus = updateTermStatus(term);

            for (TermItem termItem : term.getTermItemList()) {
                //if đủ → cần "khóa" lại dù chưa hết hạn
                int approvedForm = countApprovedFormByTermItem(termItem);
//            term đang ACTIVE nhưng đã đủ số lượng → chuyển sang LOCKED_TERM
//            trường hợp khác giữ nguyên status tính từ thời gian
                Status finalStatus = (timeStatus.equals(Status.ACTIVE_TERM) && approvedForm == termItem.getMaxNumberRegistration())
                        ? Status.LOCKED_TERM
                        : timeStatus;

                if (!termItem.getStatus().equals(finalStatus)) {
                    termItem.setStatus(finalStatus);
                    termItemRepo.save(termItem);
                }
            }
        }

        List<Map<String, Object>> termList = viewTermList(terms);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(termList)
                        .build()
        );
    }

    private List<Map<String, Object>> viewTermList(List<AdmissionTerm> termList) {
        return termList.stream()
                .map(term -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", term.getId());
                            data.put("name", term.getName());
                            data.put("startDate", term.getStartDate());
                            data.put("endDate", term.getEndDate());
                            data.put("year", term.getYear() + "-" + (term.getYear() + 1));
                            data.put("termItemList", viewTermItemList(term));

                            //gọi lai extra term
                            if (!admissionTermRepo.findAllByParentTerm_Id(term.getId()).isEmpty()) {
                                data.put("extraTerms", viewExtraTerm(term));
                            }
                            return data;
                        }
                )
                .toList();
    }

    private List<Map<String, Object>> viewTermItemList(AdmissionTerm term) {
        return term.getTermItemList().stream()
                .map(termItem -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", termItem.getId());
                            data.put("studentsPerClass", termItem.getStudentsPerClass());
                            data.put("expectedClasses", termItem.getExpectedClasses());
                            data.put("maxNumberRegistration", termItem.getMaxNumberRegistration());
                            data.put("approvedForm", countApprovedFormByTermItem(termItem));
                            data.put("grade", termItem.getGrade().name().toLowerCase());
                            data.put("status", termItem.getStatus().getValue().toLowerCase());
                            data.put("feeList", getFeeMapByGrade(termItem.getGrade()));
                            return data;
                        }
                )
                .toList();
    }

    private Status updateTermStatus(AdmissionTerm term) {
        LocalDateTime today = LocalDateTime.now();
        if (!term.getStatus().equals(Status.LOCKED_TERM)) {
            if (today.isBefore(term.getStartDate())) {
                return Status.INACTIVE_TERM;
            } else if (!today.isAfter(term.getEndDate())) {
                return Status.ACTIVE_TERM;
            } else {
                return Status.LOCKED_TERM;
            }
        }
        return Status.LOCKED_TERM;
    }

    private List<Map<String, Object>> viewExtraTerm(AdmissionTerm parentTerm) {
        return viewTermList(
                admissionTermRepo.findAllByParentTerm_Id(parentTerm.getId())
        );
    }


    @Override
    public ResponseEntity<ResponseObject> createExtraTerm(CreateExtraTermRequest request) {
        //Validate các field cơ bản (ngày, số lượng, grade rỗng...)
        String error = ExtraTermValidation.createExtraTerm(request, admissionTermRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        //Kiểm tra parent term tồn tại
        AdmissionTerm parentTerm = admissionTermRepo.findById(request.getParentTermId()).orElse(null);
        assert parentTerm != null;

        AdmissionTerm extraTerm = admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name("Extra Term - " + " " + parentTerm.getYear())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .year(parentTerm.getYear())
                        .parentTerm(parentTerm)
                        .status(Status.INACTIVE_TERM)
                        .build()
        );

        for (TermItem termItem : parentTerm.getTermItemList()) {
            if (countMissingFormAmountByTermItem(termItem) > 0) { //dang missing
                TermItem t = termItemRepo.save(TermItem.builder()
                        .grade(termItem.getGrade())
                        .expectedClasses(0)
                        .studentsPerClass(StudentPerClass.MAX.getValue())
                        .maxNumberRegistration(countMissingFormAmountByTermItem(termItem))
                        .admissionTerm(extraTerm)
                        .status(Status.INACTIVE_TERM_ITEM)
                        .currentRegisteredStudents(0)
                        .build());

                //
                t.setExpectedClasses(countExpectedClass(t, termItem));
                termItemRepo.save(t);
            }

            if (countApprovedFormByTermItem(termItem) >= termItem.getMaxNumberRegistration()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("Term has already reached maximum registration")
                                .success(false)
                                .data(null)
                                .build()
                );
            }
        }

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

    private int countApprovedFormByTermItem(TermItem termItem) {
        return (int) termItem.getAdmissionFormList().stream().filter(form -> form.getStatus().equals(Status.APPROVED)).count();
    }
    //--> tính ra số lượng lớp của từng khôis
    //        dự kiến 40 --> 2 lớp
    //        thực tế approve 20 --> 1 lớp

    private int countMissingFormAmountByTermItem(TermItem termItem) {
        return termItem.getMaxNumberRegistration() - countApprovedFormByTermItem(termItem);
    }

    private int countExpectedClass(TermItem termItem, TermItem parentTermItem) {
        if (parentTermItem == null) {
            return -1;
        }

        double createdClassParentTermItem = Math.ceil((double) countApprovedFormByTermItem(parentTermItem) / StudentPerClass.MAX.getValue());
        double expectedClassTermItem = (double) termItem.getMaxNumberRegistration() / StudentPerClass.MAX.getValue();
//        100 = 5 lop mong doi co
//                30 dua approved  = 2 lop
//                70 tuyen them = 3.5 lop
//                = 5.5 > 5
//                ==> 3.5 phai tro thanh 3
        if (createdClassParentTermItem + expectedClassTermItem > termItem.getMaxNumberRegistration()) {
            expectedClassTermItem = Math.floor(expectedClassTermItem);
        }

        return (int) expectedClassTermItem;
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
            // Cập nhật trạng thái thành APPROVED_WAITING_PAYMENT
            form.setStatus(Status.APPROVED);
            // Lưu thời gian duyệt form
            form.setApprovedDate(LocalDateTime.now());
            // Đặt thời gian hết hạn thanh toán (ví dụ: 2 ngày sau)
            form.setPaymentExpiryDate(LocalDateTime.now().plusDays(2));

            student.setStudent(true); // Đặt student là true khi form được duyệt
            studentRepo.save(student); // Lưu thay đổi cho student

            String subject = "[PES] Admission Approved";
            String heading = "🎉 Admission Approved";
            String bodyHtml = Format.getAdmissionApprovedBody(student.getName());
            mailService.sendMail(parentEmail, subject, heading, bodyHtml);

        } else {
            form.setStatus(Status.REJECTED);
            form.setCancelReason(request.getReason());

            String subject = "[PES] Admission Rejected";
            String heading = "❌ Admission Rejected";
            String bodyHtml = Format.getAdmissionRejectedBody(student.getName(), request.getReason());
            mailService.sendMail(parentEmail, subject, heading, bodyHtml);
        }

        admissionFormRepo.save(form);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message(request.isApproved() ? "Form Approved and awaiting payment" : "Form Rejected")
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
