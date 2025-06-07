package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.ParentService;
import com.swd392.group1.pes.validations.ParentValidation.EditAdmissionFormValidation;
import com.swd392.group1.pes.validations.ParentValidation.SubmittedAdmissionFormValidation;
import jakarta.servlet.http.HttpServletRequest;
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
public class ParentServiceImpl implements ParentService {

    private final JWTService jwtService;

    private final AdmissionFormRepo admissionFormRepo;

    private final StudentRepo studentRepo;

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request) {

        //xac thuc nguoi dung
        Account account = jwtService.extractAccountFromCookie(request);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Get list failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        //loc danh sách form phụ thuộc vào parent này
        List<Map<String, Object>> admissionForms = admissionFormRepo.findAll().stream()
                .filter(form -> form.getParent().getId().equals(account.getParent().getId())) //Crash tại form.getParent().getId() ==> trước khi tạo form phải save parent
                .filter(form -> form.getStudent() != null) // bỏ qua các AdmissionForm không có học sinh ==> tránh bị lỗi null
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed()) // sort form theo ngày chỉnh sửa mới nhất
                .map(this::getFormDetail)
                .toList();

        //lấy danh sách học sinh của phụ huynh
        List<Map<String, Object>> studentList = studentRepo.findAllByParent_Id(account.getParent().getId()).stream()
                .map(student -> {
                    Map<String, Object> studentDetail = new HashMap<>();
                    studentDetail.put("id", student.getId());
                    studentDetail.put("name", student.getName());
                    studentDetail.put("gender", student.getGender());
                    studentDetail.put("dateOfBirth", student.getDateOfBirth());
                    studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
                    studentDetail.put("isStudent", student.isStudent());
                    studentDetail.put("hadForm", !student.getAdmissionFormList().isEmpty());//trong từng học sinh check đã tạo form chưa
                    return studentDetail;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("admissionForms", admissionForms);
        data.put("students", studentList);

        return ResponseEntity.ok().body(
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

    //submit form
    @Override
    public ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest) {

        // 1. Lấy account từ cookie
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Submitted admission form failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = SubmittedAdmissionFormValidation.validate(request);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<AdmissionForm> formList = admissionFormRepo.findAllByParent_IdAndStudent_Id(account.getParent().getId(), request.getStudentId());


        if (!formList.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("This child was already registered")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        admissionFormRepo.save(
                AdmissionForm.builder()
                        .parent(account.getParent()) // tránh lỗi bị crash null
                        .student(studentRepo.findById(request.getStudentId()).orElse(null)) //mỗi form phải gắn với đúng Student để biết phiếu đó là của ai
                        .householdRegistrationAddress(request.getHouseholdRegistrationAddress())
                        .profileImage(request.getProfileImage())
                        .householdRegistrationImg(request.getHouseholdRegistrationImg())
                        .birthCertificateImg(request.getBirthCertificateImg())
                        .commitmentImg(request.getCommitmentImg())
                        .note(request.getNote())
                        .submittedDate(LocalDate.now())
                        .status(Status.PENDING_APPROVAL.getValue())
                        .build()
        );

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Successfully submitted")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    //cancel form
    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionForm(int id, HttpServletRequest httpRequest) {
// 1. Lấy account từ cookie
        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null || !account.getRole().equals(Role.PARENT)) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Cancelled admission form failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = EditAdmissionFormValidation.canceledValidate(id, account, admissionFormRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionForm form = admissionFormRepo.findById(id).orElse(null);

        if (form == null) {
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .success(false)
                            .message("Admission form not found")
                            .data(null)
                            .build()
            );
        }

        form.setStatus(Status.CANCELLED.getValue());
        admissionFormRepo.save(form);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Successfully cancelled")
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
