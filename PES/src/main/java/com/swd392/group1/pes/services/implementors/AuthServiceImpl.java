package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.dto.requests.LoginRequest;
import com.swd392.group1.pes.dto.requests.RegisterRequest;
import com.swd392.group1.pes.dto.requests.ResetPassRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.services.AuthService;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.CookieUtil;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import com.swd392.group1.pes.utils.email.Format;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Value("${security-access-expiration}")
    private long accessExpiration;

    @Value("${security-refresh-expiration}")
    private long refreshExpiration;

    private final AccountRepo accountRepo;

    private final JWTService jwtService;

    private final ParentRepo parentRepo;

    private final MailService mailService;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepo.findByEmailAndPassword(request.getEmail(), request.getPassword()).orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .message("Invalid email or password")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String error = loginValidation(request, accountRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String newAccess = jwtService.generateAccessToken(account);
        String newRefresh = jwtService.generateRefreshToken(account);

        CookieUtil.createCookie(response, newAccess, newRefresh, accessExpiration, refreshExpiration);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Login successfully")
                        .success(true)
                        .data(buildLoginBody(account))
                        .build()
        );
    }

    private Map<String, Object> buildLoginBody(Account account) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", account.getEmail());
        body.put("role", account.getRole().name());
        return body;
    }

    public static String loginValidation(LoginRequest request, AccountRepo accountRepo) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null || !acc.getPassword().equals(request.getPassword()) ||
                acc.getStatus().equalsIgnoreCase(Status.ACCOUNT_BAN.getValue())) {
            return "Email or password is incorrect.";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> logout(HttpServletResponse response) {
        CookieUtil.removeCookie(response);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Logout successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshToken = CookieUtil.getCookie(request, "refresh");

        if (refreshToken != null && jwtService.checkIfNotExpired(refreshToken.getValue())) {

            String email = jwtService.extractEmailFromJWT(refreshToken.getValue());
            Account account = accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

            if (account != null) {
                String newAccessToken = jwtService.generateAccessToken(account);

                CookieUtil.createCookie(response, newAccessToken, refreshToken.getValue(), accessExpiration, refreshExpiration);

                return ResponseEntity.status(HttpStatus.OK).body(
                        ResponseObject.builder()
                                .message("Refresh access token successfully")
                                .success(true)
                                .data(null)
                                .build()
                );
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseObject.builder()
                        .message("Refresh token is invalid or expired")
                        .success(false)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> register(RegisterRequest request) {

        String error = registerValidation(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (accountRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Email is already in use by another account.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        account.setName(request.getName());
        account.setPhone(request.getPhone());
        account.setGender(request.getGender());
        account.setIdentityNumber(request.getIdentityNumber());
        account.setRole(Role.PARENT);
        account.setStatus(Status.ACCOUNT_ACTIVE.getValue());
        account.setCreatedAt(LocalDateTime.now());
        account.setAddress(request.getAddress());

        accountRepo.save(account);

        Parent parent = Parent.builder()
                .job(request.getJob())
                .relationshipToChild(request.getRelationshipToChild())
                .account(account)
                .build();
        parentRepo.save(parent);

        mailService.sendMail(
                account.getEmail(),
                "[PES] Account Registration Successful",
                "🎉 Account Created Successfully",
                Format.getParentRegisterFormat(account.getName(), account.getEmail())
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Register Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String registerValidation(RegisterRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            return "Invalid email format.";
        }

        if (accountRepo.existsByEmail(request.getEmail())) {
            return "This email is already registered.";
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        if (request.getPassword().length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
        Pattern specialPattern = Pattern.compile(".*[^A-Za-z0-9].*");

        if (!digitPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one digit.";
        }
        if (!lowerCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!upperCasePattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!specialPattern.matcher(request.getPassword()).matches()) {
            return "Password must contain at least one special character.";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        if (!request.getConfirmPassword().equals(request.getPassword())) {
            return "Confirm password does not match password.";
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }

        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes.";
        }

        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }

        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid prefix and be 10 digits.";
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }

        if (!request.getGender().trim().equals("male") &&
                !request.getGender().trim().equals("female")) {
            return "Gender must be 'male', 'female'";
        }

        if (request.getIdentityNumber() == null || request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required.";
        }

        Pattern idPattern = Pattern.compile("^\\d{12}$");
        if (!idPattern.matcher(request.getIdentityNumber()).matches()) {
            return "Identity number must be exactly 12 digits.";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> forgotPassword(ForgotPasswordRequest request) {
        String error = forgotPasswordValidation(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = accountRepo.findByEmail(request.getEmail()).orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Account not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Tạo code ngẫu nhiên 7 ký tự
        String code = RandomPasswordUtil.generateRandomString(7);
        account.setCode(code);
        // Set thời gian hết hạn 5 phút
        account.setCodeExpiry(LocalDateTime.now().plusMinutes(5));
        accountRepo.save(account);

        // Gửi email chứa code sử dụng format có sẵn
        String subject = "Reset Your Password";
        String body = Format.getForgotPasswordBody(code);
        mailService.sendMail(account.getEmail(), subject, subject, body);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Reset code has been sent to your email")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String forgotPasswordValidation(ForgotPasswordRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null) {
            return "No active account found with this email.";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> verifyCode(String code) {
        Account account = accountRepo.findByCode(code).orElse(null);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Invalid reset code")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Check if code is expired
        if (account.getCodeExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Reset code has expired")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Code is valid")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> resetPass(ResetPassRequest request) {
        String error = resetPassValidation(request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }


        Account account = accountRepo.findByCode(request.getCode()).orElse(null);

        if (account == null || account.getCodeExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Invalid or expired reset code")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Update password
        account.setPassword(request.getNewPassword()); // chưa cần decode pass
        // Clear reset code
        account.setCode(null);
        account.setCodeExpiry(null);
        accountRepo.save(account);

        mailService.sendMail(
                account.getEmail(),
                "[PES] Password Changed Successfully",
                "🔒 Password Changed",
                Format.getPasswordChangedFormat(account.getName())
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Password has been reset successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String resetPassValidation(ResetPassRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        if (request.getNewPassword().length() < 8) {
            return "Password must be at least 8 characters";
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
            return "Confirm password does not match password.";
        }

        return "";
    }
}
