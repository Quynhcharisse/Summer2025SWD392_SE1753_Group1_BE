package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.requests.CancelAdmissionForm;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.ParentService;
import com.swd392.group1.pes.validations.AdmissionValidation.FormByParentValidation;
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

    private final AdmissionTermRepo admissionTermRepo;

    private final ParentRepo parentRepo;

    private final StudentRepo studentRepo;

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request) {

        Account acc = jwtService.extractAccountFromCookie(request);

        if (acc == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Get list failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Map<String, Object>> admissionForms = admissionFormRepo.findAll().stream()
                .filter(form -> form.getParent() != null && acc.getParent() != null &&
                        form.getParent().getId().equals(acc.getParent().getId()))
                .sorted(Comparator.comparing(AdmissionForm::getSubmittedDate).reversed()) // sort form theo ngày chỉnh sửa mới nhất
                .map(this::getFormDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(admissionForms)
                        .build()
        );
    }

    private Map<String, Object> getFormDetail(AdmissionForm form) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", form.getId());
        data.put("childName", form.getChildName());
        data.put("childGender", form.getChildGender());
        data.put("dateOfBirth", form.getDateOfBirth());
        data.put("placeOfBirth", form.getPlaceOfBirth());
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

    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionForm(CancelAdmissionForm request, HttpServletRequest httpRequest) {

        Account acc = jwtService.extractAccountFromCookie(httpRequest);
        if (acc == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Cancelled admission form failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = FormByParentValidation.canceledValidate(request, acc, admissionFormRepo);

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
        assert form != null;

        form.setStatus(Status.CANCELLED.getValue());
        admissionFormRepo.save(form);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Successfully cancelled")
                        .success(true)
                        .data(getIdOfForm(form))
                        .build()
        );
    }

    private Map<String, Object> getIdOfForm(AdmissionForm form) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", form.getId());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest) {

        Account account = jwtService.extractAccountFromCookie(httpRequest);
        if (account == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Submitted admission form failed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = FormByParentValidation.submittedForm(request);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<AdmissionForm> formList = admissionFormRepo.findAllByParent_IdAndChildName(account.getParent().getId(), request.getName());

        if (!formList.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("This student was already registered")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.findByYear(LocalDate.now().getYear()).orElse(null);

        if (term == null || !(term.getStatus().equalsIgnoreCase(Status.ACTIVE_TERM.getValue()))) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Invalid term")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        admissionFormRepo.save(
                AdmissionForm.builder()
                        .childName(request.getName())
                        .childGender(request.getGender())
                        .dateOfBirth(request.getDateOfBirth())
                        .placeOfBirth(request.getPlaceOfBirth())
                        .householdRegistrationAddress(request.getHouseholdRegistrationAddress())
                        .profileImage(request.getProfileImage())
                        .birthCertificateImg(request.getBirthCertificateImg())
                        .householdRegistrationImg(request.getHouseholdRegistrationImg())
                        .commitmentImg(request.getCommitmentImg())
                        .note(request.getNote())
                        .submittedDate(LocalDate.now())
                        .status(Status.PENDING_APPROVAL.getValue())
                        .admissionTerm(term)
                        .parent(account.getParent()) //gán parent khi tạo form đăng kí
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

    @Override
    public ResponseEntity<ResponseObject> getChildren(HttpServletRequest httpRequest) {
        Account acc = jwtService.extractAccountFromCookie(httpRequest);

        if (acc == null || !acc.getRole().equals(Role.PARENT)) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Only parents can access the list of children.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Parent parent = parentRepo.findByAccount_Id(acc.getId()).orElse(null);
        if (parent == null) {
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message("Access denied: You are not allowed to view children of this parent.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Student> children = studentRepo.findAllByParent_Id(parent.getId());

        if (children == null) {
            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message("No children found for this parent.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Map<String, Object>> childrenData = children.stream().map(
                        child -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", child.getId());
                            data.put("name", child.getName());
                            data.put("gender", child.getGender());
                            data.put("dateOfBirth", child.getDateOfBirth());
                            data.put("placeOfBirth", child.getPlaceOfBirth());
                            return data;
                        }
                )
                .toList();


        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Children retrieved successfully.")
                        .success(true)
                        .data(childrenData)
                        .build()
        );
    }

}
