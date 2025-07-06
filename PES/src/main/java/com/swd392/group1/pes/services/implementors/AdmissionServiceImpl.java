package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.dto.requests.CreateExtraTermRequest;
import com.swd392.group1.pes.dto.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
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
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        String error = createTermValidate(request, admissionTermRepo);
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

        // Nếu hợp lệ, tiếp tục tạo term
        AdmissionTerm term = admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name(name)
                        .startDate((request.getStartDate().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))).toLocalDateTime())
                        .endDate((request.getEndDate().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))).toLocalDateTime())
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

    public static String createTermValidate(CreateAdmissionTermRequest request, AdmissionTermRepo admissionTermRepo) {
        if (request.getStartDate() == null) {
            return "Start date is required";
        }

        if (request.getEndDate() == null) {
            return "End date is required";
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Start date must be before end date";
        }

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            return "Start date must be in the future";
        }

        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            return "Start date and end date must be in the same year";
        }

        int year = request.getStartDate().getYear();
        if (admissionTermRepo.existsByYear(year)) {
            return "Admission term for year " + year + " already exists.";
        }

        //trong 1 term phai it nhat 1 grade trong create term do
        if (request.getTermItemList() == null || request.getTermItemList().isEmpty()) {
            return "At least one grade must be included in the term.";
        }

        Set<String> grades = new HashSet<>();

        for (CreateAdmissionTermRequest.TermItem termItem : request.getTermItemList()) {
            //expectedClasses > 0
            if (termItem.getExpectedClasses() <= 0) {
                return "Expected classes must be greater than 0 for grade: " + termItem.getGrade();
            }

            //hợp lệ enum
            try {
                Grade.valueOf(termItem.getGrade());
            } catch (IllegalArgumentException e) {
                return "Invalid grade: " + termItem.getGrade();
            }

            if (!grades.add(termItem.getGrade())) {
                return "Duplicate grade found: " + termItem.getGrade();
            }
        }
        return "";
    }


    @Override
    public ResponseEntity<ResponseObject> updateTermStatus(UpdateAdmissionTermRequest request) {
        String error = updateTermValidate(request);
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


    public static String updateTermValidate(UpdateAdmissionTermRequest request) {

        System.out.println("Term ID: " + request.getTermId());
        if (request.getTermId() <= 0) {
            return "Term ID must be a positive number";
        }
        return "";
    }


    private int calculateMaxRegistration(int expectedClasses) {
        return StudentPerClass.MAX.getValue() * expectedClasses;
    }

    @Override
    public ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade) {
        try {
            Grade g = Grade.valueOf(grade.toUpperCase());
            Map<String, Long> feeData = getFeeMapByGrade(g); // dùng chung
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

    private Map<String, Long> getFeeMapByGrade(Grade grade) {
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
            Status termStatus = updateTermStatus(term);

            // Cập nhật status cho term nếu cần
            if (!term.getStatus().equals(termStatus)) {
                term.setStatus(termStatus);
                admissionTermRepo.save(term);
            }

            // Cập nhật status cho từng termItem
            for (TermItem termItem : term.getTermItemList()) {
                Status itemStatus = updateTermItemStatus(term, termItem);
                if (!termItem.getStatus().equals(itemStatus)) {
                    termItem.setStatus(itemStatus);
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
                            data.put("status", term.getStatus().getValue());
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

    private Status updateTermItemStatus(AdmissionTerm term, TermItem termItem) {
        int approvedForm = countApprovedFormByTermItem(termItem);
        LocalDateTime now = LocalDateTime.now();

        if (term.getStatus().equals(Status.LOCKED_TERM)) {
            return Status.LOCKED_TERM_ITEM;
        }
        if (now.isBefore(term.getStartDate())) {
            return Status.INACTIVE_TERM_ITEM;
        }
        if (!now.isAfter(term.getEndDate())) {
            if (approvedForm >= termItem.getMaxNumberRegistration()) {
                return Status.LOCKED_TERM_ITEM;
            }
            return Status.ACTIVE_TERM_ITEM;
        }
        return Status.LOCKED_TERM_ITEM;
    }

    private List<Map<String, Object>> viewExtraTerm(AdmissionTerm parentTerm) {
        return viewTermList(
                admissionTermRepo.findAllByParentTerm_Id(parentTerm.getId())
        );
    }


    @Override
    public ResponseEntity<ResponseObject> createExtraTerm(CreateExtraTermRequest request) {
        //Validate các field cơ bản (ngày, số lượng, grade rỗng...)
        String error = createExtraTerm(request, admissionTermRepo);
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

        //duyệt từng term item trong parentTerm để tạo bản sao thiếu
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

                // tính lại expected class dựa trên số thiếu
                t.setExpectedClasses(countExpectedClass(t, termItem));

                //nếu đủ số lượng --> lock luôn
                t.setStatus(countMissingFormAmountByTermItem(termItem) == 0
                        ? Status.LOCKED_TERM_ITEM
                        : Status.INACTIVE_TERM_ITEM);

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
                            data.put("status", form.getStatus().getValue());
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
        String error = processFormByManagerValidate(request, admissionFormRepo);
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

    public static String processFormByManagerValidate(ProcessAdmissionFormRequest request, AdmissionFormRepo admissionFormRepo) {
        AdmissionForm form = admissionFormRepo.findById(request.getId()).orElse(null);

        if (form == null) {
            return "Form not found";
        }

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

    @Override
    public Map<String, Long> getAdmissionFormStatusSummary() {
        Map<String, Long> summary = new HashMap<>();
        for (Status status : List.of(
                Status.PENDING_APPROVAL,
                Status.REFILLED,
                Status.APPROVED,
                Status.APPROVED_PAID,
                Status.REJECTED)) {

            Long count = admissionFormRepo.countByStatus(status);
            summary.put(status.name(), count);
        }

        summary.put("pendingApprovalCount", admissionFormRepo.countByStatus(Status.PENDING_APPROVAL));
        summary.put("refilledCount", admissionFormRepo.countByStatus(Status.REFILLED));
        summary.put("approvedCount", admissionFormRepo.countByStatus(Status.APPROVED));
        summary.put("rejectedCount", admissionFormRepo.countByStatus(Status.REJECTED));
        summary.put("paymentCount", admissionFormRepo.countByStatus(Status.APPROVED_PAID));

        return summary;
    }
}
