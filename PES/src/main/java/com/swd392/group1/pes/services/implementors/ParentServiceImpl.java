package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.email.Format;
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
import com.swd392.group1.pes.requests.AddChildRequest;
import com.swd392.group1.pes.requests.CancelAdmissionForm;
import com.swd392.group1.pes.requests.GetPaymentURLRequest;
import com.swd392.group1.pes.requests.RefillFormRequest;
import com.swd392.group1.pes.requests.RegisterEventRequest;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateChildRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.services.ParentService;
import com.swd392.group1.pes.validations.EducationValidation.EventValidation;
import com.swd392.group1.pes.validations.ParentValidation.ChildValidation;
import com.swd392.group1.pes.validations.ParentValidation.EditAdmissionFormValidation;
import com.swd392.group1.pes.validations.ParentValidation.RefillFormValidation;
import com.swd392.group1.pes.validations.ParentValidation.SubmittedAdmissionFormValidation;
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

    @Value("${vnpay.ipn.url}")
    String vnpayIpnUrl;

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

    private Map<String, Object> getFormDetail(AdmissionForm form) {
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
        data.put("submittedDate", form.getSubmittedDate());
        data.put("cancelReason", form.getCancelReason());
        data.put("note", form.getNote());
        data.put("status", form.getStatus());
        return data;
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
        String error = SubmittedAdmissionFormValidation.validate(request, studentRepo, termItemRepo, admissionFormRepo);
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

        //Tim ky item term trong ki tuyen sinh ACTIVE
        TermItem activeTermItem = termItemRepo.findById(request.getTermItemId()).orElse(null);
        if (activeTermItem == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

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
        String heading = "📨 Admission Form Submitted";
        String bodyHtml = Format.getAdmissionSubmittedBody(
                account.getName(),
                LocalDate.now().toString()
        );
        //Gửi email
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

    //view refill form
    @Override
    public ResponseEntity<ResponseObject> viewRefillFormList(HttpServletRequest request) {
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
//
//        //Tìm kỳ tuyển sinh đang ACTIVE
//        TermItem activeTermItem = termItemRepo.findByStatusAndAdmissionTerm_Status(Status.ACTIVE_TERM_ITEM, Status.ACTIVE_TERM).orElse(null);

        List<Status> statusesIncluded = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<Map<String, Object>> admissionFormList = admissionFormRepo.findAllByStudentNotNullAndParent_IdAndStatusIn(account.getParent().getId(), statusesIncluded).stream()
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed())// sort form theo ngày chỉnh sửa mới nhất
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

    //refill form
    @Override
    public ResponseEntity<ResponseObject> refillForm(RefillFormRequest request, HttpServletRequest httpRequest) {
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
        String error = RefillFormValidation.validate(request, studentRepo, termItemRepo, admissionFormRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // 3. Lấy thông tin student
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

        //Tìm term item cua ki tuyen sinh đang ACTIVE
        TermItem activeTermItem = termItemRepo.findById(request.getTermItemId()).orElse(null);
        if (activeTermItem == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //Tìm form REJECTED hoặc CANCELLED để cập nhật
        // Validation đã đảm bảo rằng chỉ có ĐÚNG MỘT form
        // Sử dụng findAllByStudent_IdAndTermItem_IdAndStatusIn để lấy trực tiếp từ DB
        List<Status> rejectedOrCancelledStatuses = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        Optional<AdmissionForm> rejectedOrCancelledFormOpt = admissionFormRepo
                .findAllByStudent_IdAndTermItem_IdAndStatusIn(student.getId(), activeTermItem.getId(), rejectedOrCancelledStatuses)
                .stream()
                .findFirst(); //lấy cái đầu tiên đảm bảo chỉ có 1

        // Đây là một kiểm tra an toàn, trên lý thuyết không bao giờ xảy ra nếu validation đúng
        if (rejectedOrCancelledFormOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( // Internal Server Error vì validation đã lỗi
                    ResponseObject.builder()
                            .message("Failed to find a suitable rejected or cancelled form to refill. Please contact support.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //Nếu có form bị cancel hoặc reject thì cập nhật form đó, nếu không thì tạo mới
        AdmissionForm formToUpdate = rejectedOrCancelledFormOpt.get();
        formToUpdate.setHouseholdRegistrationAddress(request.getHouseholdRegistrationAddress());
        formToUpdate.setCommitmentImg(request.getCommitmentImg());
        formToUpdate.setChildCharacteristicsFormImg(request.getChildCharacteristicsFormImg());
        formToUpdate.setNote(request.getNote());
        formToUpdate.setSubmittedDate(LocalDateTime.now());
        formToUpdate.setStatus(Status.PENDING_APPROVAL);

        admissionFormRepo.save(formToUpdate);

        //Gửi email notification
        String subject = "[PES] Admission Form Resubmitted";
        String heading = "🔄 Admission Form Resubmitted";
        String bodyHtml = Format.getAdmissionRefilledBody(
                account.getName(),
                LocalDate.now().toString()
        );

        // 8. Gửi email xác nhận
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

        // 9. Trả về kết quả thành công
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Successfully resubmitted")
                        .success(true)
                        .data(null)
                        .build()
        );
    }


    // cancel form
    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionForm(CancelAdmissionForm request, HttpServletRequest httpRequest) {
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


        String error = EditAdmissionFormValidation.canceledValidate(request, account, admissionFormRepo);

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
            String heading = "❌ Admission Form Cancelled";
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

        String error = ChildValidation.addChildValidate(request);
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

        studentRepo.save(
                Student.builder()
                        .name(request.getName())
                        .gender(request.getGender())
                        .dateOfBirth(request.getDateOfBirth())
                        .placeOfBirth(request.getPlaceOfBirth())
                        .profileImage(request.getProfileImage())
                        .birthCertificateImg(request.getBirthCertificateImg())
                        .householdRegistrationImg(request.getHouseholdRegistrationImg())
                        .modifiedDate(LocalDate.now())
                        .isStudent(false)         // mặc định là chưa chính thức
                        .parent(parent)           // gán cha mẹ
                        .build());


        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Child added successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
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

        String error = ChildValidation.updateChildValidate(request);
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

        // tránh bị null
        int count = student.getUpdateCount() == null ? 0 : student.getUpdateCount();
        // Deny if update limit exceeded
        if (count >= 5) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("This is critical information and can only be updated 5 times. You have reached the limit.")
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
        student.setUpdateCount(count + 1); // safe increment // Tăng số lần cập nhật
        studentRepo.save(student);

        int remaining = 5 - student.getUpdateCount();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Update successful. You have " + remaining + " update(s) remaining.")
                        .success(true)
                        .data(null)
                        .build());
    }

    @Override
    public ResponseEntity<ResponseObject> registerEvent(RegisterEventRequest request, HttpServletRequest requestHttp) {

        Account account = jwtService.extractAccountFromCookie(requestHttp);

        // 1. Validate chung
        String validationError = EventValidation.validateRegisterEvent(request);
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
            int age = Period.between(dob, now.toLocalDate()).getYears();
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

        try {
            mailService.sendMail(
                    account.getEmail(),
                    "[PES] EVENT REGISTRATION CONFIRMATION",
                    "Event Registration Confirmation",
                    "Dear " + account.getName() + ",\n\n" +
                            "You have successfully registered the following students for \"" +
                            event.getName() + "\":\n- " +
                            String.join("\n- ", registered) +
                            "\n\nThank you,\nSunShine Preschool"
            );
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }

        // 5. Lưu tất cả và trả về kết quả
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
        long amount = request.getAmount() * 100;
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        String currCode = "VND";
        String ipAddr = "127.0.0.1";
        String locale = "vn";
        String orderInfo = request.getPaymentInfo();
        String orderType = "education";
        String returnUrl = vnpayReturnUrl;
        cld.add(Calendar.MINUTE, 10);
        String expireDate = formatter.format(cld.getTime());


        //String txnRef = getTxnRef(8);
        String txnRef = request.getTxnRef(); // LẤY txnRef TỪ REQUEST
        if (txnRef == null || txnRef.isEmpty()) {
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
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", locale);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);
        vnpParams.put("vnp_IpnUrl", vnpayIpnUrl);

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
    public ResponseEntity<ResponseObject> initiateVNPayPayment(GetPaymentURLRequest request, HttpServletRequest httpRequest) {
        Account acc = jwtService.extractAccountFromCookie(httpRequest);
        if (acc == null || !acc.getRole().equals(Role.PARENT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ResponseObject.builder()
                            .message("Forbidden: Only parents can access this resource")
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

        //Tìm AdmissionForm liên quan
        Optional<AdmissionForm> optionalAdmissionForm = admissionFormRepo.findById(request.getFormId());
        if (optionalAdmissionForm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Admission Form not found with ID: " + request.getFormId())
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        AdmissionForm admissionForm = optionalAdmissionForm.get();

        //Tạo txnRef duy nhất cho giao dịch này
        String newTxnRef = getTxnRef(8); // Sử dụng hàm getTxnRef đã có của bạn

        //Tạo bản ghi Transaction MỚI và lưu vào DB với trạng thái PENDING
        try {
            Transaction newTransaction = Transaction.builder()
                    .admissionForm(admissionForm) // Liên kết giao dịch với AdmissionForm
                    .amount(request.getAmount())
                    .description(request.getPaymentInfo())
                    .status(Status.TRANSACTION_PENDING) // Đặt trạng thái ban đầu là PENDING (từ enum Status của bạn)
                    .txnRef(newTxnRef) // Gán txnRef đã tạo
                    .build();
            transactionRepo.save(newTransaction);
        } catch (Exception e) {
            // Xử lý lỗi nếu không thể lưu Transaction
            System.err.println("Error saving transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseObject.builder()
                            .message("Failed to create payment transaction. Please try again. Error: " + e.getMessage())
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //Gán txnRef đã tạo vào request để truyền cho hàm getPaymentURL
        request.setTxnRef(newTxnRef); // Đảm bảo GetPaymentURLRequest có setter cho txnRef

        //Gọi hàm getPaymentURL để lấy URL thanh toán.
        // Hàm này giờ đây sẽ sử dụng txnRef mà chúng ta vừa tạo và lưu.
        return getPaymentURL(request, httpRequest);
    }
}
