package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.RestPasswordRequest;
import com.swd392.group1.pes.requests.UpdateProfileRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AccountService;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.validations.AccountValidation.ResetPasswordValidation;
import com.swd392.group1.pes.validations.AccountValidation.UpdateProfileValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;

    private final JWTService jwtService;

    @Override
    public ResponseEntity<ResponseObject> resetPassword(RestPasswordRequest request) {
        String error = ResetPasswordValidation.validateResetPassword(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmailAndPassword(request.getEmail(), request.getOldPassword()).orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Invalid email or password.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setPassword(request.getNewPassword());

        // Nếu là role không cần reset lần đầu (PARENT, HR)
        if (account.getRole().equals(Role.HR) || account.getRole().equals(Role.PARENT)) {
            account.setFirstLogin(false);
        }

        // Nếu là các role cần reset lần đầu → mark đã đổi pass nếu đang trong lần đầu
        if (request.isFirstLogin()) {
            if (account.getRole().equals(Role.ADMISSION)
                    || account.getRole().equals(Role.EDUCATION)
                    || account.getRole().equals(Role.TEACHER)) {
                account.setFirstLogin(false);
            }
        }

        accountRepo.save(account);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Password reset successfully.")
                        .success(true)
                        .data(account)
                        .build()
        );
    }


    @Override
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request) {

        Account account = jwtService.extractAccountFromCookie(request);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .message("Cannot retrieve profile. Authentication token is missing or invalid.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", account.getEmail());
        body.put("name", account.getName());
        body.put("phone", account.getPhone());
        body.put("gender", account.getGender());
        body.put("identityNumber", account.getIdentityNumber());
        body.put("avatarUrl", account.getAvatarUrl());
        body.put("role", account.getRole());
        body.put("createdAt", account.getCreatedAt());
        body.put("status", account.getStatus());

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(body)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest) {

        Account account = jwtService.extractAccountFromCookie(httpRequest);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .message("Cannot retrieve profile. Authentication token is missing or invalid.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = UpdateProfileValidation.validate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setName(request.getName());
        account.setPhone(request.getPhone());
        account.setGender(request.getGender());
        account.setAvatarUrl(request.getAvatarUrl());

        accountRepo.save(account);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Update Profile Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
