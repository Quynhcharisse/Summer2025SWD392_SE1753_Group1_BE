package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.CreateTeacherRequest;
import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.requests.UpdateTeacherRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.HRService;
import com.swd392.group1.pes.validations.AccountValidation.ProcessAccountValidation;
import com.swd392.group1.pes.validations.HRValidation.CreateTeacherValidation;
import com.swd392.group1.pes.validations.HRValidation.UpdateTeacherValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HRServiceImpl implements HRService {

    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> processAccount(ProcessAccountRequest request, String action) {

        String error = ProcessAccountValidation.processAccountValidate(request, action, accountRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = null;
        if (action.equalsIgnoreCase("ban")) {
            account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        } else if (action.equalsIgnoreCase("unban")) {
            account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_BAN.getValue()).orElse(null);
        }

        String newStatus;
        if (action.equalsIgnoreCase("ban")) {
            newStatus = Status.ACCOUNT_BAN.getValue();
        } else if (action.equalsIgnoreCase("unban")) {
            newStatus = Status.ACCOUNT_UNBAN.getValue();
        } else {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Invalid action")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (account == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Account not found or in invalid state")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setStatus(newStatus);
        accountRepo.save(account);

        String message = action.equalsIgnoreCase("ban") ?
                "Account banned successfully" : "Account unbanned successfully";

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message(message)
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> createTeacher(CreateTeacherRequest request) {
        String error = CreateTeacherValidation.validate(request, accountRepo);
        if(!error.isEmpty()){
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword("123456");
        account.setName(request.getName());
        account.setPhone(request.getPhone());
        account.setGender(request.getGender());
        account.setIdentityNumber(request.getIdentityNumber());
        account.setRole(Role.TEACHER);
        account.setStatus(Status.ACCOUNT_ACTIVE.getValue());
        account.getManager().setPasswordChanged(false);

        accountRepo.save(account);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Create Teacher Successfully")
                        .success(true)
                        .data(account)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewTeacherList() {
        List<Account> teachers = accountRepo.findByRole(Role.TEACHER);
        if (teachers == null || teachers.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No teachers found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Map<String, Object>> teacherList = teachers.stream()
                .map(this::buildAccountBodyDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Teacher list retrieved successfully")
                        .success(true)
                        .data(teacherList)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateTeacher(UpdateTeacherRequest request) {
        String error = UpdateTeacherValidation.validate(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmail(request.getEmail()).orElse(null);
        if (account == null || !Role.TEACHER.equals(account.getRole())) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Teacher not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setName(request.getName());
        account.setPhone(request.getPhone());
        account.setGender(request.getGender());
        account.setIdentityNumber(request.getIdentityNumber());

        accountRepo.save(account);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Update Teacher Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> removeTeacher(ProcessAccountRequest request) {
        String error = ProcessAccountValidation.validateRemove(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        if (account == null || !Role.TEACHER.equals(account.getRole())) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Teacher not found or already removed")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        accountRepo.deleteById(account.getId());

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Remove Teacher Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewParentList() {
        List<Account> parents = accountRepo.findByRole(Role.PARENT);
        if (parents == null || parents.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No parents found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Map<String, Object>> parentList = parents.stream()
                .map(this::buildAccountBodyDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Parent list retrieved successfully")
                        .success(true)
                        .data(parentList)
                        .build()
        );
    }

    private Map<String, Object> buildAccountBodyDetail(Account account) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", account.getEmail());
        body.put("name", account.getName());
        body.put("phone", account.getPhone());
        body.put("gender", account.getGender());
        body.put("identityNumber", account.getIdentityNumber());
        body.put("createdAt", account.getCreatedAt());
        body.put("avatarUrl", account.getAvatarUrl());
        body.put("role", account.getRole());
        body.put("status", account.getStatus());
        return body;
    }
}
