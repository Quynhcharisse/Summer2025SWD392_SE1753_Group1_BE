package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.utils.email.Format;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.dto.requests.RestPasswordRequest;
import com.swd392.group1.pes.dto.requests.UpdateProfileRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.AccountService;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.MailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final JWTService jwtService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<ResponseObject> resetPassword(RestPasswordRequest request) {
        String error = validateResetPassword(request, accountRepo);
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

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));

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

        mailService.sendMail(
                account.getEmail(),
                "[PES] Password Renewed Successfully",
                "✅ Password Renewed",
                Format.getRenewPasswordSuccessBody(account.getName())
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Password reset successfully.")
                        .success(true)
                        .data(account)
                        .build()
        );
    }

    public static String validateResetPassword(RestPasswordRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        if (acc == null) {
            return "No active account found with this email.";
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return "New password is required.";
        }

        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            return "Old password is required.";
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            return "New password must be different from the old password.";
        }

        if (request.getNewPassword().length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one digit.";
        }
        if (!lowerCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!upperCasePattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!specialPattern.matcher(request.getNewPassword()).matches()) {
            return "Password must contain at least one special character.";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            return "Confirm password does not match the new password.";
        }

        return "";
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
        body.put("identityNumber", maskIdentityNumber(account.getIdentityNumber()));
        body.put("avatarUrl", account.getAvatarUrl());
        body.put("role", account.getRole());
        body.put("createdAt", account.getCreatedAt());
        body.put("address", account.getAddress());
        body.put("status", account.getStatus());

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(body)
                        .build()
        );
    }

    private String maskIdentityNumber(String identityNumber) {
        if (identityNumber == null || identityNumber.length() < 4) {
            return "****"; // hoặc xử lý phù hợp nếu quá ngắn
        }

        int visibleDigits = 4;
        String masked = "*".repeat(identityNumber.length() - visibleDigits);
        String last4 = identityNumber.substring(identityNumber.length() - visibleDigits);
        return masked + last4;
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

        String error = updateProfileValidate(request);
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
        account.setAddress(request.getAddress());

        accountRepo.save(account);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Update Profile Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String updateProfileValidate(UpdateProfileRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return "Name is required.";
        }

        if (!request.getName().trim().matches("^[A-Za-z\\s]+$")) {
            return "Name must only contain English letters (A–Z or a–z) and spaces. Numbers and special characters are not allowed.";
        }
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }
        if (!request.getPhone().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits.";
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }
        String gender = request.getGender().trim().toLowerCase();
        if (!gender.equals("male") && !gender.equals("female")) {
            return "Gender must be male or female.";
        }

        return "";
    }
}
