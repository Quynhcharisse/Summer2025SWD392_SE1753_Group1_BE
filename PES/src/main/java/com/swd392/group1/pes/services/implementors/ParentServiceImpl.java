package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.AddChildRequest;
import com.swd392.group1.pes.dto.requests.CancelAdmissionForm;
import com.swd392.group1.pes.dto.requests.GetPaymentURLRequest;
import com.swd392.group1.pes.dto.requests.InitiateVNPayPaymentRequest;
import com.swd392.group1.pes.dto.requests.RefillFormRequest;
import com.swd392.group1.pes.dto.requests.RegisterEventRequest;
import com.swd392.group1.pes.dto.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateChildRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Fees;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.EventParticipate;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.models.Transaction;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.EventParticipateRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.repositories.TransactionRepo;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.services.ParentService;
import com.swd392.group1.pes.utils.InvoiceGenerator;
import com.swd392.group1.pes.utils.email.Format;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.swd392.group1.pes.services.implementors.EventServiceImpl.validateRegisterEvent;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final JWTService jwtService;

    private final AdmissionFormRepo admissionFormRepo;

    private final TermItemRepo termItemRepo;

    private final ParentRepo parentRepo;

    private final TransactionRepo transactionRepo;

    private final StudentRepo studentRepo;

    private final MailService mailService;

    @Value("${vnpay.return.url}")
    String vnpayReturnUrl;

    @Value("${vnpay.hash.key}")
    String hashKey;

    private final EventRepo eventRepo;
    private final EventParticipateRepo eventParticipateRepo;

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request) {

        //xac thuc nguoi dung
        Account account = jwtService.extractAccountFromCookie(request);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Map<String, Object>> admissionFormList = admissionFormRepo.findAll().stream()
                //kiểm tra, nếu form ko cos thì vẫn hiện, chứ ko bị crash, //Crash tại form.getParent().getId() ==> trước khi tạo form phải save parent
                .filter(form -> form.getParent() != null && form.getParent().getId().equals(account.getParent().getId()))
                .filter(form -> form.getStudent() != null) // bỏ qua các AdmissionForm không có học sinh ==> tránh bị lỗi null
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed()) // sort form theo ngày chỉnh sửa mới nhất
                .map(this::getFormDetail)
                .toList();


        List<Map<String, Object>> studentList = studentRepo.findAllByParent_Id(account.getParent().getId()).stream()
                .map(student -> {
                    Map<String, Object> studentDetail = new HashMap<>();
                    studentDetail.put("id", student.getId());
                    studentDetail.put("name", student.getName());
                    studentDetail.put("gender", student.getGender());
                    studentDetail.put("dateOfBirth", student.getDateOfBirth());
                    studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
                    studentDetail.put("profileImage", student.getProfileImage());
                    studentDetail.put("householdRegistrationImg", student.getHouseholdRegistrationImg());
                    studentDetail.put("birthCertificateImg", student.getBirthCertificateImg());
                    studentDetail.put("isStudent", student.isStudent());
                    studentDetail.put("hadForm", !student.getAdmissionFormList().isEmpty());//trong từng học sinh check đã tạo form chưa
                    studentDetail.put("admissionForms", getFormListByStudent(student));

                    return studentDetail;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("admissionFormList", admissionFormList);
        data.put("studentList", studentList);


        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(data)
                        .build()
        );
    }

    private long sumFee(Grade grade) {
        if (grade.equals(Grade.SEED)) {
            Fees fee = Fees.SEED;
            return fee.getLearningMaterial() + fee.getReservation() + fee.getService() + fee.getUniform() + fee.getFacility();
        } else if (grade.equals(Grade.BUD)) {
            Fees fee = Fees.BUD;
            return fee.getLearningMaterial() + fee.getReservation() + fee.getService() + fee.getUniform() + fee.getFacility();
        } else {
            Fees fee = Fees.LEAF;
            return fee.getLearningMaterial() + fee.getReservation() + fee.getService() + fee.getUniform() + fee.getFacility();
        }
    }

    private Map<String, Object> getFormDetail(AdmissionForm form) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        Map<String, Object> data = new HashMap<>();
        data.put("id", form.getId());
        data.put("studentId", form.getStudent().getId());
        data.put("studentName", form.getStudent().getName());
        data.put("studentGender", form.getStudent().getGender());
        data.put("studentDateOfBirth", form.getStudent().getDateOfBirth());
        data.put("studentPlaceOfBirth", form.getStudent().getPlaceOfBirth());
        data.put("profileImage", form.getStudent().getProfileImage());
        data.put("householdRegistrationImg", form.getStudent().getHouseholdRegistrationImg());
        data.put("birthCertificateImg", form.getStudent().getBirthCertificateImg());
        data.put("commitmentImg", form.getCommitmentImg());
        data.put("childCharacteristicsFormImg", form.getChildCharacteristicsFormImg());
        data.put("householdRegistrationAddress", form.getHouseholdRegistrationAddress());
        data.put("submittedDate", form.getSubmittedDate().format(formatter));
        data.put("cancelReason", form.getCancelReason());
        data.put("totalFees", sumFee(form.getTermItem().getGrade()));
        data.put("note", form.getNote());
        data.put("status", form.getStatus().getValue());
        return data;
    }

    private List<Map<String, Object>> getFormListByStudent (Student student) {
        return student.getAdmissionFormList().stream()
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed())
                .map(this::getFormDetail)
                .toList();
    }

    //submit form
    @Override
    public ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest) {
        //Xác thực người dùng
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //Validate input
        String error = submittedAdmissionFormValidation(request, studentRepo, termItemRepo, admissionFormRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //Lấy thông tin student
        Student student = studentRepo.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Grade grade = calculateAge(student.getDateOfBirth()) == 3 ? Grade.SEED : (calculateAge(student.getDateOfBirth()) == 4 ? Grade.BUD : Grade.LEAF);
        List<TermItem> activeTermItemList = termItemRepo.findAllByGradeAndStatusAndAdmissionTerm_Year(grade, Status.ACTIVE_TERM_ITEM, LocalDate.now().getYear());
        TermItem activeTermItem = activeTermItemList.get(0);

        // **LOẠI BỎ LOGIC KIỂM TRA FORM ĐÃ NỘP Ở ĐÂY!**
        // Logic này đã được chuyển hoàn toàn vào SubmittedAdmissionFormValidation.validate()
        // Nếu validation thành công, thì không có form nào đang hoạt động hoặc chờ duyệt
        // Lưu form mới
        AdmissionForm form = AdmissionForm.builder()
                .parent(account.getParent())
                .student(student)
                .termItem(activeTermItem)
                .householdRegistrationAddress(request.getHouseholdRegistrationAddress())
                .commitmentImg(request.getCommitmentImg())
                .childCharacteristicsFormImg(request.getChildCharacteristicsFormImg())
                .note(request.getNote())
                .submittedDate(LocalDateTime.now())
                .status(Status.PENDING_APPROVAL)
                .build();

        admissionFormRepo.save(form);

        // 8. Gửi email notification
        String subject = "[PES] Admission Form Submitted";
        String heading = "Admission Form Submitted";
        String bodyHtml = Format.getAdmissionSubmittedBody(
                account.getName(),
                form.getStudent().getName(),
                LocalDate.now()
        );

        try {
            mailService.sendMail(
                    account.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Successfully submitted")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private String submittedAdmissionFormValidation(SubmitAdmissionFormRequest request, StudentRepo studentRepo, TermItemRepo termItemRepo, AdmissionFormRepo admissionFormRepo) {
        Student student = studentRepo.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return "Student not found after successful validation. This indicates a logical error.";
        }

        if (!isAgeValidForGrade(student.getDateOfBirth())) {
            return "Student's age (" + calculateAge(student.getDateOfBirth()) + " years) does not meet the required age for admission (3-5 years).";
        }

        Grade grade = calculateAge(student.getDateOfBirth()) == 3 ? Grade.SEED : (calculateAge(student.getDateOfBirth()) == 4 ? Grade.BUD : Grade.LEAF);
        List<TermItem> activeTermItemList = termItemRepo.findAllByGradeAndStatusAndAdmissionTerm_Year(grade, Status.ACTIVE_TERM_ITEM, LocalDate.now().getYear());
        System.out.println("List ACTIVE TERM: " + activeTermItemList.size());
        if (activeTermItemList.isEmpty()) {
            return "No active term currently.";
        }

        TermItem activeTermItem = activeTermItemList.get(0);

        if (!activeTermItem.getStatus().equals(Status.ACTIVE_TERM_ITEM) || activeTermItem.getAdmissionTerm() == null || !activeTermItem.getAdmissionTerm().getStatus().equals(Status.ACTIVE_TERM)) {
            return "The admission term item is not currently open for new admissions or is invalid.";
        }

        List<Status> statusesToExcludeForNewSubmission = Arrays.asList(Status.REJECTED, Status.CANCELLED, Status.REFILLED);
        List<AdmissionForm> activeOrPendingForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusNotIn(
                student.getId(), activeTermItem.getId(), statusesToExcludeForNewSubmission
        );

        if (!activeOrPendingForms.isEmpty()) {
            boolean hasPendingForm = activeOrPendingForms.stream()
                    .anyMatch(form -> form.getStatus().equals(Status.PENDING_APPROVAL));
            if (hasPendingForm) {
                return "This student already has a pending admission form for the current term. New submission is not allowed.";
            }
            return "This student already has an active or processed admission form for the current term. New submission is not allowed.";
        }

        if (request.getHouseholdRegistrationAddress() == null || request.getHouseholdRegistrationAddress().trim().isEmpty()) {
            return "Household registration address is required.";
        }

        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        if (request.getCommitmentImg() == null) {
            return "Commitment image is required.";
        }

        if (isNotValidImage(request.getCommitmentImg())) {
            return "Commitment image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getChildCharacteristicsFormImg() == null) {
            return "Child characteristics form image is required.";
        }

        if (isNotValidImage(request.getChildCharacteristicsFormImg())) {
            return "Child characteristics form image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        if (request.getNote() != null && request.getNote().length() > 300) {
            return "Note must not exceed 300 characters.";
        }

        return "";
    }

    private boolean isNotValidImage(String fileName) {
        return fileName == null || fileName.trim().isEmpty() || !fileName.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    private boolean isAgeValidForGrade(LocalDate dob) {
        int age = calculateAge(dob);
        return age >= 3 && age <= 5;
    }

    @Override
    public ResponseEntity<ResponseObject> refillForm(RefillFormRequest request, HttpServletRequest httpRequest) {
        // Xác thực người dùng
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);

        if (form == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Form not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Kiểm tra quyền truy cập form
        if (!form.getParent().getId().equals(account.getParent().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("You do not have permission to access this form")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        form.setStatus(Status.REFILLED);
        admissionFormRepo.save(form);

        SubmitAdmissionFormRequest submitRequest = SubmitAdmissionFormRequest.builder()
                .studentId(request.getStudentId())
                .childCharacteristicsFormImg(request.getChildCharacteristicsFormImg())
                .householdRegistrationAddress(request.getHouseholdRegistrationAddress())
                .commitmentImg(request.getCommitmentImg())
                .note(request.getNote())
                .build();

        // Gửi email notification cho refill
        String subject = "[PES] Admission Form Refilled";
        String heading = "Admission Form Refilled";
        String bodyHtml = Format.getAdmissionRefilledBody(
                account.getName(),
                form.getStudent().getName(),
                LocalDate.now()
        );

        try {
            mailService.sendMail(
                    account.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            System.err.println("Failed to send refill email notification: " + e.getMessage());
        }

        return submitAdmissionForm(submitRequest, httpRequest);
    }


    // cancel form
    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionForm(CancelAdmissionForm request, HttpServletRequest
            httpRequest) {
        // 1. Lấy account từ cookie
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build()
            );
        }


        String error = canceledValidate(request, account, admissionFormRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);

        if (form == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Admission form not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        System.out.println(form.getStatus());

        if (!form.getStatus().equals(Status.PENDING_APPROVAL)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Only pending approval forms can be cancelled.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        form.setStatus(Status.CANCELLED);
        admissionFormRepo.save(form);

        //Gửi email thông báo hủy
        try {
            String subject = "[PES] Admission Form Cancelled";
            String heading = "Admission Form Cancelled";
            String bodyHtml = Format.getAdmissionCancelledBody(account.getName());
            mailService.sendMail(
                    account.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Successfully cancelled")
                        .success(true)
                        .data(null) //ko cần thiết trả về chỉ quan tâm là thông báo thành công hay thất bại
                        .build()
        );
    }

    private String canceledValidate(CancelAdmissionForm request, Account account, AdmissionFormRepo admissionFormRepo) {
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

    @Override
    public ResponseEntity<ResponseObject> viewChild(HttpServletRequest httpRequest) {
        //xac thuc nguoi dung
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Tìm parent dựa vào account ID
        Parent parent = parentRepo.findByAccount_Id(account.getId()).orElse(null);
        if (parent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Parent not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Lấy danh sách student của parent đó
        List<Map<String, Object>> studentList = parent.getStudentList().stream()
                .sorted(Comparator.comparing(Student::getModifiedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(student -> {
                    Map<String, Object> studentDetail = new HashMap<>();
                    studentDetail.put("id", student.getId());
                    studentDetail.put("name", student.getName());
                    studentDetail.put("gender", student.getGender());
                    studentDetail.put("dateOfBirth", student.getDateOfBirth());
                    studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
                    studentDetail.put("profileImage", student.getProfileImage());
                    studentDetail.put("birthCertificateImg", student.getBirthCertificateImg());
                    studentDetail.put("householdRegistrationImg", student.getHouseholdRegistrationImg());
                    studentDetail.put("modifiedDate", student.getModifiedDate());
                    studentDetail.put("isStudent", student.isStudent());
                    studentDetail.put("hadForm", !student.getAdmissionFormList().isEmpty());
                    return studentDetail;
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(studentList)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> addChild(AddChildRequest request, HttpServletRequest httpRequest) {
        Account acc = jwtService.extractAccountFromCookie(httpRequest);
        if (acc == null || !acc.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build());
        }

        String error = addChildValidate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        Parent parent = parentRepo.findByAccount_Id(acc.getId()).orElse(null);
        if (parent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Parent not found")
                            .success(false)
                            .data(null)
                            .build());
        }

        Student savedStudent = studentRepo.save(
                Student.builder()
                        .name(request.getName())
                        .gender(request.getGender())
                        .dateOfBirth(request.getDateOfBirth())
                        .placeOfBirth(request.getPlaceOfBirth())
                        .profileImage(request.getProfileImage())
                        .birthCertificateImg(request.getBirthCertificateImg())
                        .householdRegistrationImg(request.getHouseholdRegistrationImg())
                        .modifiedDate(LocalDate.now())
                        .isStudent(false)        
                        .parent(parent)           
                        .build());

        // Gửi email confirmation
        try {
            String subject = "[PES] Child Added Successfully";
            String heading = "Child Added to Your Account";
            String bodyHtml = Format.getChildAddedSuccessBody(
                    acc.getName(),
                    savedStudent.getName(),
                    savedStudent.getGender(),
                    savedStudent.getDateOfBirth().toString()
            );
            mailService.sendMail(
                    acc.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            System.err.println("Failed to send add child email notification: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Child added successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private String addChildValidate(AddChildRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (request.getName().length() < 2 || request.getName().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (!isValidGender(request.getGender())) {
            return "Gender must be Male or Female.";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        int age = calculateAge(request.getDateOfBirth());
        if (age < 3 || age >= 6) {
            return "Child's age must be between 3 and 5 years.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        String[] images = {
                request.getProfileImage(),
                request.getHouseholdRegistrationImg(),
                request.getBirthCertificateImg(),
        };
        String[] imageNames = {
                "Profile image",
                "Household registration image",
                "Birth certificate image",
                "Commitment image"
        };

        for (int i = 0; i < images.length; i++) {
            String error = validateImageField(imageNames[i], images[i]);
            if (!error.isEmpty()) return error;
        }

        return "";
    }


    @Override
    public ResponseEntity<ResponseObject> updateChild(UpdateChildRequest request, HttpServletRequest httpRequest) {
        Account acc = jwtService.extractAccountFromCookie(httpRequest);
        if (acc == null || !acc.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build());
        }

        String error = updateChildValidate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        // Tìm parent từ account
        Parent parent = parentRepo.findByAccount_Id(acc.getId()).orElse(null);
        if (parent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Parent not found")
                            .success(false)
                            .data(null)
                            .build());
        }

        // KHÔNG cho update nếu đã là học sinh chính thức
        Student student = studentRepo.findById(request.getId()).orElse(null);
        if (student == null || !student.getParent().getId().equals(parent.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Child not found or access denied")
                            .success(false)
                            .data(null)
                            .build());
        }

        boolean hasSubmittedForm = student.getAdmissionFormList()
                .stream()
                .anyMatch(
                        form -> !form.getStatus().equals(Status.DRAFT)
                );

        if (student.isStudent() || hasSubmittedForm) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot update child info after submitting admission form")
                            .success(false)
                            .data(null)
                            .build());
        }

        // Cập nhật thông tin nếu chưa là học sinh
        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setPlaceOfBirth(request.getPlaceOfBirth());
        student.setProfileImage(request.getProfileImage());
        student.setBirthCertificateImg(request.getBirthCertificateImg());
        student.setHouseholdRegistrationImg(request.getHouseholdRegistrationImg());
        student.setModifiedDate(LocalDate.now());
        Student updatedStudent = studentRepo.save(student);

        // Gửi email confirmation
        try {
            String subject = "[PES] Child Information Updated Successfully";
            String heading = "Child Information Updated";
            String bodyHtml = Format.getChildUpdatedSuccessBody(
                    acc.getName(),
                    updatedStudent.getName()
            );
            mailService.sendMail(
                    acc.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            System.err.println("Failed to send update child email notification: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Child information updated successfully.")
                        .success(true)
                        .data(null)
                        .build());
    }

    private String updateChildValidate(UpdateChildRequest request) {
        if (request.getId() <= 0) {
            return "Invalid child ID.";
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (request.getName().length() < 2 || request.getName().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (!isValidGender(request.getGender())) {
            return "Gender must be Male, Female";
        }

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isAfter(LocalDate.now())) {
            return "Date of birth must be in the past.";
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 3 || age > 5) {
            return "Child's age must be between 3 and 5 years.";
        }

        if (request.getPlaceOfBirth() == null || request.getPlaceOfBirth().trim().isEmpty()) {
            return "Place of birth is required.";
        }

        if (request.getPlaceOfBirth().length() > 100) {
            return "Place of birth must be less than 100 characters.";
        }

        if (request.getProfileImage() == null || request.getProfileImage().isEmpty()) {
            return "Profile image is required.";
        }

        if (request.getHouseholdRegistrationImg() == null || request.getHouseholdRegistrationImg().isEmpty()) {
            return "Household registration image is required.";
        }

        if (request.getBirthCertificateImg() == null || request.getBirthCertificateImg().isEmpty()) {
            return "Birth certificate image is required.";
        }

        String imgError;

        imgError = validateImageField("Profile image", request.getProfileImage());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Household registration image", request.getHouseholdRegistrationImg());
        if (!imgError.isEmpty()) return imgError;

        imgError = validateImageField("Birth certificate image", request.getBirthCertificateImg());
        if (!imgError.isEmpty()) return imgError;

        return "";
    }

   
    private boolean isValidGender(String gender) {
        return gender != null && (
                gender.equalsIgnoreCase("Male") ||
                        gender.equalsIgnoreCase("Female")
        );
    }

    private String validateImageField(String name, String value) {
        if (value == null || value.isEmpty()) {
            return name + " is required.";
        }
        if (!value.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            return name + " must be a valid image file (.jpg, .png, .jpeg, .gif, .bmp, .webp).";
        }
        return "";
    }

    private int calculateAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.YEARS.between(dob, today);
    }

    @Override
    public ResponseEntity<ResponseObject> registerEvent(RegisterEventRequest request, HttpServletRequest
            requestHttp) {

        Account account = jwtService.extractAccountFromCookie(requestHttp);

        // 1. Validate chung
        String validationError = validateRegisterEvent(request);
        if (!validationError.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ResponseObject.builder()
                            .success(false)
                            .message(validationError)
                            .data(null)
                            .build());
        }

        // 2. Lấy Event và check trạng thái / deadline
        Event event = eventRepo.findById(Integer.parseInt(request.getEventId()))
                .orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Event not found")
                            .data(null)
                            .build());
        }
        if (event.getStatus() != Status.EVENT_REGISTRATION_ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Cannot register: event not active")
                            .data(null)
                            .build());
        }
        LocalDateTime now = LocalDateTime.now();
        if (event.getRegistrationDeadline() != null
                && event.getRegistrationDeadline().isBefore(now)) {
            return ResponseEntity.badRequest()
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Registration deadline has passed")
                            .data(null)
                            .build());
        }

        List<Integer> studentIds = request.getStudentIds().stream()
                .map(Integer::parseInt)
                .distinct()
                .toList();
        List<Student> students = studentRepo.findAllById(studentIds);
        Map<Integer, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        // 4. Validate từng student, early-exit khi gặp lỗi
        List<EventParticipate> toSave = new ArrayList<>();
        List<String> registered = new ArrayList<>();

        for (Integer sid : studentIds) {
            Student stu = studentMap.get(sid);
            // a) tồn tại?
            if (stu == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseObject.builder()
                                .success(false)
                                .message("Student ID " + sid + " not found")
                                .data(null)
                                .build());
            }
            if (!stu.isStudent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseObject.builder()
                                .success(false)
                                .message("Cannot register " + stu.getName()
                                        + ": only SunShine School students can sign up for events").data(null)
                                .build());
            }

            LocalDate dob = stu.getDateOfBirth();
            int age = Period.between(dob, event.getRegistrationDeadline().toLocalDate()).getYears();
            if (age < 3 || age > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseObject.builder()
                                .success(false)
                                .message(stu.getName() + " must be between 3 and 5 years old (actual: " + age + ")")
                                .data(null)
                                .build());
            }

            // b) đã đăng ký chưa?
            boolean already = eventParticipateRepo
                    .findByStudentIdAndEventId(sid, event.getId())
                    .isPresent();
            if (already) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseObject.builder()
                                .success(false)
                                .message(stu.getName() + ": already registered this event")
                                .data(null)
                                .build());
            }

            // c) trùng giờ?
            Optional<Event> conflict = eventParticipateRepo
                    .findAllByStudentId(sid).stream()
                    .map(EventParticipate::getEvent)
                    .filter(e2 ->
                            e2.getStartTime().isBefore(event.getEndTime()) &&
                                    event.getStartTime().isBefore(e2.getEndTime())
                    )
                    .findFirst();
            if (conflict.isPresent()) {
                Event e2 = conflict.get();
                String msg = String.format(
                        "%s: conflict with [%s] from %s to %s",
                        stu.getName(),
                        e2.getName(),
                        e2.getStartTime(),
                        e2.getEndTime()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseObject.builder()
                                .success(false)
                                .message(msg)
                                .data(null)
                                .build());
            }

            toSave.add(EventParticipate.builder()
                    .student(stu)
                    .event(event)
                    .registeredAt(LocalDateTime.now())
                    .build());
            registered.add(stu.getName());
        }
        mailService.sendMail(
                account.getEmail(),
                "[PES] EVENT REGISTRATION CONFIRMATION",
                "Event Registration Confirmation",
                Format.getRegisterEventBody(account.getName(), event.getName(), event.getStartTime(), registered )
        );
        eventParticipateRepo.saveAll(toSave);
        String successMsg = "All students registered successfully: " + registered;
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .success(true)
                        .message(successMsg)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> getRegisteredEvents(HttpServletRequest request) {
        Account account = jwtService.extractAccountFromCookie(request);
        Parent parent = parentRepo.findByAccount_Id(account.getId())
                .orElse(null);
        if (parent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Parent profile not found")
                            .data(null)
                            .build());
        }
        List<Map<String, Object>> result = eventParticipateRepo
                .findByStudentParentIdOrderByRegisteredAtDesc(parent.getId())
                .stream()
                .map(this::buildEventDetail)
                .toList();
        return ResponseEntity.ok(
                new ResponseObject(
                        "Registered events for all your children",
                        true,
                        result
                )
        );
    }

    private Map<String, Object> buildEventDetail(EventParticipate eventParticipate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Map<String, Object> data = new HashMap<>();
        Event event = eventParticipate.getEvent();
        Student s = eventParticipate.getStudent();
        data.put("childName", s.getName());
        data.put("eventName", event.getName());
        data.put("startTime", event.getStartTime().format(fmt));
        data.put("endTime", event.getEndTime().format(fmt));
        data.put("location", event.getLocation());
        data.put("registeredAt", eventParticipate.getRegisteredAt());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> getPaymentURL(GetPaymentURLRequest request, HttpServletRequest httpRequest) {
        String version = "2.1.1";
        String command = "pay";
        String tmnCode = "NSLIVTOU";

        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);
        assert form != null;

        long amount = sumFee(form.getTermItem().getGrade()) * 100;
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        String currCode = "VND";
        String ipAddr = "127.0.0.1";
        String locale = "vn";

        String studentName = form.getStudent().getName();
        String orderInfo = String.format(
                "Thanh toan hoc phi dau vao nam hoc %d - %d cho hoc sinh %s",
                LocalDate.now().getYear(),
                LocalDate.now().getYear() + 1,
                studentName
        );
        String orderType = "education";
        String returnUrl = vnpayReturnUrl;
        cld.add(Calendar.MINUTE, 10);
        String expireDate = formatter.format(cld.getTime());


        String txnRef = getTxnRef(8);
        if (txnRef.isEmpty()) {
            // Đây là tình huống lỗi, vì initiateVNPayPayment đáng lẽ đã set nó
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .message("Internal Error: Missing transaction reference for payment URL generation.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // step 1: put all params into a sorted map
        SortedMap<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", version);
        vnpParams.put("vnp_Command", command);
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", amount + "");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", locale);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        // step 2: build the hash data string
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!hashData.isEmpty()) hashData.append('&');
            hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
            hashData.append('=');
            hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }

        // step 3: create secure hash
        String secureHash = getHashSecret(hashData.toString());

        // step 4: build full URL with all params + secure hash
        StringBuilder paymentUrl = new StringBuilder("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            paymentUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
            paymentUrl.append('=');
            paymentUrl.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            paymentUrl.append('&');
        }
        paymentUrl.append("vnp_SecureHash=").append(secureHash);

        Map<String, Object> data = new HashMap<>();
        data.put("paymentUrl", paymentUrl.toString());

        // step 5: return the result
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Successfully generated VNPay URL")
                        .success(true)
                        .data(data)
                        .build()
        );
    }

    private String getTxnRef(int length) {
        String randomCharacter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Etc/GMT+7"));
        String timestamp = formatter.format(new Date());
        StringBuilder suffix = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            suffix.append(randomCharacter.charAt(random.nextInt(randomCharacter.length())));
        }
        return timestamp + suffix.toString();
    }

    private String getHashSecret(String hashData) {
        String key = hashKey;
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
            mac.init(secretKey);
            byte[] hashBytes = mac.doFinal(hashData.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA512 hash.", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public ResponseEntity<ResponseObject> initiateVNPayPayment(InitiateVNPayPaymentRequest request, HttpServletRequest httpRequest) {
        Account acc = jwtService.extractAccountFromCookie(httpRequest);
        if (acc == null || !acc.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
                            .success(false)
                            .data(null)
                            .build());
        }

        String error = paymentValidation(request, admissionFormRepo, transactionRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        //Kiểm tra formId có được cung cấp không
        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);
        if (form == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Missing formId in payment request")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (form.getStatus() == Status.APPROVED_PAID) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("This admission form has already been paid.")
                            .success(false)
                            .data(null)
                            .build());
        }


        // Không cần kiểm tra transaction WAITING_PAYMENT nếu bạn chỉ cho thanh toán 1 lần
        if (!"00".equals(request.getResponseCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("VNPay payment failed or canceled.")
                            .success(false)
                            .data(null)
                            .build());
        }

        String txnRef = getTxnRef(8);

        try {
            form.setStatus(Status.APPROVED_PAID);
            admissionFormRepo.save(form);

            Student student = form.getStudent();
            if (student != null && !student.isStudent()) {
                student.setStudent(true);
                studentRepo.save(student);
            }

            Transaction transaction = Transaction.builder()
                    .admissionForm(form)
                    .amount(sumFee(form.getTermItem().getGrade()))
                    .vnpTransactionNo(request.getTransactionInfo())
                    .description(request.getDescription())
                    .status(Status.APPROVED_PAID)
                    .paymentDate(LocalDate.now())
                    .txnRef(txnRef)
                    .build();

            transactionRepo.save(transaction);

            assert student != null;
            byte[] pdf = InvoiceGenerator.generateInvoicePdf(
                    form.getParent().getAccount().getName(),
                    student.getName(),
                    txnRef,
                    transaction.getAmount(),
                    transaction.getPaymentDate().atStartOfDay()
            );

            String htmlBody = Format.getPaymentSuccessBody(
                    form.getParent().getAccount().getName(),
                    student.getName(),
                    txnRef,
                    transaction.getAmount(),
                    transaction.getPaymentDate().atStartOfDay()
            );

            mailService.sendInvoiceEmail(
                    form.getParent().getAccount().getEmail(),
                    "Xác nhận thanh toán học phí đầu vào",
                    htmlBody,
                    pdf,
                    "Sunshine-Invoice-" + txnRef + ".pdf"
            );

            Map<String, Object> data = new HashMap<>();
            data.put("txnRef", txnRef);
            data.put("transactionId", transaction.getId());
            data.put("amount", transaction.getAmount());

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message("Thanh toán thành công.")
                            .success(true)
                            .data(data)
                            .build());

        } catch (Exception e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .message("Failed to process payment. Please try again.")
                            .success(false)
                            .data(null)
                            .build());
        }
    }

    private String paymentValidation(InitiateVNPayPaymentRequest request, AdmissionFormRepo admissionFormRepo, TransactionRepo transactionRepo) {

        //Kiểm tra formId có được cung cấp không
        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);
        if (form == null) {
            return "Missing formId in payment request";
        }

        Transaction existedTransaction = transactionRepo.findByAdmissionFormIdAndStatus(form.getId(), Status.WAITING_PAYMENT);
        if (existedTransaction != null) {
            return "Transaction already exists for this form";
        }
        return "";
    }
}
