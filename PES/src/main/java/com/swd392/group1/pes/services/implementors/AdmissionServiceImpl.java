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
import com.swd392.group1.pes.models.Transaction;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.repositories.TransactionRepo;
import com.swd392.group1.pes.dto.requests.DailyTotalTransactionRequest;
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {
    private final StudentRepo studentRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final TermItemRepo termItemRepo;
    private final MailService mailService;
    private final TransactionRepo transactionRepo;

    @Value("${vnpay.hash.key}")
    String hashKey;

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

        String name = "Admission Term for " + request.getStartDate().getYear();

        int year = request.getStartDate().getMonthValue() >= 6
                ? request.getStartDate().getYear()
                : request.getStartDate().getYear() - 1;

        // Nếu hợp lệ, tiếp tục tạo term
        AdmissionTerm term = admissionTermRepo.save(
                AdmissionTerm.builder()
                        .name(name)
                        .startDate((request.getStartDate().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))).toLocalDateTime())
                        .endDate((request.getEndDate().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))).toLocalDateTime())
                        .year(year)
                        .status(Status.INACTIVE_TERM)
                        .build()
        );
        return createTermItem(request.getTermItemList(), term);
    }


    private ResponseEntity<ResponseObject> createTermItem(List<CreateAdmissionTermRequest.TermItem> termItemList, AdmissionTerm term) {
        for (CreateAdmissionTermRequest.TermItem termItem : termItemList) {
            termItemRepo.save(
                    TermItem.builder()
                            .grade(termItem.getGrade())
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

    private String createTermValidate(CreateAdmissionTermRequest request, AdmissionTermRepo admissionTermRepo) {
        LocalDateTime now = LocalDateTime.now();

        if (request.getStartDate() == null) {
            return "Please select a start date for the admission term.";
        }

        if (request.getEndDate() == null) {
            return "Please select an end date for the admission term.";
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "The start date must be before or equal to the end date.";
        }

        if (request.getStartDate().isBefore(now)) {
            return "The start date must be in the future (after today).";
        }

        if (request.getStartDate().getYear() != request.getEndDate().getYear()) {
            return "Start and end dates must be in the same calendar year.";
        }

        List<CreateAdmissionTermRequest.TermItem> termItemList = request.getTermItemList();
        if (termItemList == null || termItemList.isEmpty()) {
            return "Please select at least one grade for the admission term.";
        }

        int calculatedYear = request.getStartDate().getMonthValue() >= 6
                ? request.getStartDate().getYear()
                : request.getStartDate().getYear() - 1;

        Set<Grade> gradeSet = new HashSet<>();

        for (CreateAdmissionTermRequest.TermItem termItem : termItemList) {
            Grade grade = termItem.getGrade();

            if (admissionTermRepo.existsByYearAndTermItemList_Grade(calculatedYear, grade)) {
                String academicYear = calculatedYear + "–" + (calculatedYear + 1);
                return "An admission term already exists for academic year " + academicYear +
                        " and grade " + getGradeLabel(grade) + ".";
            }

            if (termItem.getExpectedClasses() <= 0) {
                return "Expected classes for " + getGradeLabel(grade) + " must be greater than 0.";
            }

            if (termItem.getExpectedClasses() > 1000) {
                return "Expected classes for " + getGradeLabel(grade) + " is too large (maximum is 1000).";
            }

            if (!gradeSet.add(grade)) {
                return "Duplicate grade detected: " + getGradeLabel(grade) + ".";
            }
        }

        return ""; // valid
    }

    private String getGradeLabel(Grade grade) {
        return switch (grade) {
            case SEED -> "Seed Class (3 years old)";
            case BUD -> "Bud Class (4 years old)";
            case LEAF -> "Leaf Class (5 years old)";
        };
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


    private String updateTermValidate(UpdateAdmissionTermRequest request) {

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
        public ResponseEntity<ResponseObject> getDefaultFeeByGrade(Grade grade) {
            try {
                Map<String, Long> feeData = getFeeMapByGrade(grade);
                return ResponseEntity.ok(
                        ResponseObject.builder()
                                .message("Default fee fetched successfully")
                                .success(true)
                                .data(feeData)
                                .build()
                );
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("Invalid grade: " + grade)
                                .success(false)
                                .data(null)
                                .build()
                );
            }
        }

        private static final Map<Grade, Fees> gradeToFeesMap = Map.of(
                Grade.SEED, Fees.SEED,
                Grade.BUD, Fees.BUD,
                Grade.LEAF, Fees.LEAF
        );

        private Map<String, Long> getFeeMapByGrade(Grade grade) {
            Fees fee = gradeToFeesMap.get(grade);
            if (fee == null) {
                throw new IllegalArgumentException("No fee defined for grade: " + grade);
            }
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

    private String createExtraTerm(CreateExtraTermRequest request, AdmissionTermRepo admissionTermRepo) {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
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
                            data.put("submittedDate", form.getSubmittedDate().format(formatter));
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
            form.setStatus(Status.APPROVED);
            form.setApprovedDate(LocalDateTime.now());
            form.setPaymentExpiryDate(LocalDateTime.now().plusDays(2));

            student.setStudent(true);
            studentRepo.save(student);

            String subject = "[PES] Admission Approved";
            String heading = "Admission Approved";
            String bodyHtml = Format.getAdmissionApprovedBody(student.getName());
            mailService.sendMail(parentEmail, subject, heading, bodyHtml);

        } else {
            form.setStatus(Status.REJECTED);
            form.setCancelReason(request.getReason());

            String subject = "[PES] Admission Rejected";
            String heading = "Admission Rejected";
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

    private String processFormByManagerValidate(ProcessAdmissionFormRequest request, AdmissionFormRepo admissionFormRepo) {
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
        List<String> years = admissionTermRepo.findAllByParentTermIsNull().stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .map(year -> year + "–" + (year + 1))
                .toList();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Fetched academic years successfully")
                        .success(true)
                        .data(years)
                        .build()
        );
    }


    @Override
    public Map<String, Long> getAdmissionFormStatusSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("pendingApprovalCount", admissionFormRepo.countByStatus(Status.PENDING_APPROVAL));
        summary.put("refilledCount", admissionFormRepo.countByStatus(Status.REFILLED));
        summary.put("approvedCount", admissionFormRepo.countByStatus(Status.APPROVED));
        summary.put("rejectedCount", admissionFormRepo.countByStatus(Status.REJECTED));
        summary.put("paymentCount", admissionFormRepo.countByStatus(Status.APPROVED_PAID));
        return summary;
    }

    @Override
    public ResponseEntity<ByteArrayResource> exportTransactionsToExcel() {
        List<Transaction> transactionList = transactionRepo
                .findAllByPaymentDateAndStatus(LocalDate.now(), Status.APPROVED_PAID)
                .stream()
                .sorted(Comparator.comparing(Transaction::getPaymentDate).reversed())
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Header
            String[] headers = {"ID", "Student Name", "Parent Name", "Txn Ref", "Description", "Amount", "Status", "Payment Date"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Transaction t : transactionList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getAdmissionForm().getStudent().getName());
                row.createCell(2).setCellValue(t.getAdmissionForm().getParent().getAccount().getName());
                row.createCell(3).setCellValue(t.getTxnRef());
                row.createCell(4).setCellValue(t.getDescription());
                row.createCell(5).setCellValue(t.getAmount());
                row.createCell(6).setCellValue(t.getStatus().getValue());
                row.createCell(7).setCellValue(t.getPaymentDate() != null ? t.getPaymentDate().toString() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=today-transactions.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getTransactionList() {

        List<Map<String, Object>> transactionList = transactionRepo.findAllByPaymentDateAndStatus(LocalDate.now(), Status.APPROVED_PAID).stream()
                .sorted(Comparator.comparing(Transaction::getPaymentDate).reversed())
                .map(this::getTransactionDetail)
                .toList();


        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Successfully queried transaction from VNPAY")
                        .success(true)
                        .data(transactionList)
                        .build()
        );
    }

    private Map<String, Object> getTransactionDetail(Transaction transaction) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", transaction.getId());
        data.put("description", transaction.getDescription());
        data.put("status", transaction.getStatus());
        data.put("amount", transaction.getAmount());
        data.put("vnpTransactionNo", transaction.getPaymentDate());
        data.put("paymentDate", transaction.getPaymentDate());
        data.put("txnRef", transaction.getTxnRef());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> getDailyTotal(DailyTotalTransactionRequest request) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Map<String, Object>> dailyTotals = new ArrayList<>();
        long totalSum = 0;

        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            long dailyTotal = transactionRepo
                    .findAllByPaymentDateAndStatus(currentDate, Status.APPROVED_PAID)
                    .stream()
                    .mapToLong(Transaction::getAmount)
                    .sum();

            totalSum += dailyTotal;

            Map<String, Object> data = new HashMap<>();
            data.put("date", currentDate.format(DateTimeFormatter.ofPattern("dd/MM")));
            data.put("amount", dailyTotal);
            data.put("totalAmount", totalSum);

            dailyTotals.add(data);
        }

        int transactionCount = transactionRepo.countByStatusAndPaymentDateBetween(
                Status.APPROVED_PAID, startDate, endDate
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("dailyData", dailyTotals);
        responseData.put("transactionCount", transactionCount);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Success")
                        .success(true)
                        .data(responseData)
                        .build()
        );
    }
}
