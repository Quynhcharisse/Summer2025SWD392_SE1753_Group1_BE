package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Action;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.requests.RenewPasswordRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AccountService;
import com.swd392.group1.pes.validations.AccountValidation.ProcessAccountValidation;
import com.swd392.group1.pes.validations.AccountValidation.RenewPasswordValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> renewPassword(RenewPasswordRequest request) {
        String error = RenewPasswordValidation.validate(request, accountRepo);
        if(!error.isEmpty()){
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmailAndPassword(request.getEmail(), request.getOldPassword()).orElse(null);

        assert account != null;
        account.setPassword(request.getNewPassword());
        account.setConfirmPassword(request.getConfirmPassword());
        accountRepo.save(account);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Renew Password Successfully")
                        .success(true)
                        .data(account)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewProfile() {
        List<Map<String, Object>> userList = accountRepo.findAll().stream()
                .map(this::buildProfileBodyDetail)
                .toList();


        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Profile retrieved successfully")
                        .success(true)
                        .data(userList)
                        .build()
        );
    }

    private Map<String, Object> buildProfileBodyDetail(Account account) {
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

//    @Override
//    public ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request) {
//        String error = UpdateProfileValidation.validate(request, accountRepo);
//        if(!error.isEmpty()){
//            return ResponseEntity.ok().body(
//                    ResponseObject.builder()
//                            .message(error)
//                            .success(false)
//                            .data(null)
//                            .build()
//            );
//        }
//
//        Account account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
//
//
//
//        account.setName(request.getName());
//        account.setPhone(request.getPhone());
//        account.setGender(request.getGender());
//        account.setIdentityNumber(request.getIdentityNumber());
//
//        account = accountRepo.save(account);
//
//        return ResponseEntity.ok().body(
//                ResponseObject.builder()
//                        .message("Update Profile Successfully")
//                        .success(true)
//                        .data(null)
//                        .build()
//        );
//    }

    @Override
    public ResponseEntity<ResponseObject> processAccount(ProcessAccountRequest request) {
        String error = ProcessAccountValidation.processAccountValidate(request, accountRepo);
        if(!error.isEmpty()){
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if(account == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Account not found or already banned")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String newStatus = Action.getNewStatus(request.getAction());

        if(newStatus == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Invalid action")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setStatus(newStatus);
        accountRepo.save(account);

        String message = request.getAction().equalsIgnoreCase("ban") ? "Account banned successfully" : "Account unbanned successfully";

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message(message)
                        .success(true)
                        .data(null)
                        .build()
        );
    }


}
