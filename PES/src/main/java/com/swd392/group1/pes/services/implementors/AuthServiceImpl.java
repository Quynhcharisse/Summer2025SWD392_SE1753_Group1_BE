package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.dto.requests.LoginRequest;
import com.swd392.group1.pes.dto.requests.OtpVerifyRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepo.findByEmail(request.getEmail()).orElse(null);

        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
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

    private String loginValidation(LoginRequest request, AccountRepo accountRepo) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null || !passwordEncoder.matches(request.getPassword(), acc.getPassword()) ||
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
    public ResponseEntity<ResponseObject> registerVerifyEmail(OtpVerifyRequest request) {
        if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("This email is already in use.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String token = jwtService.generateVerifyToken(request.getEmail());

        String prefix = RandomPasswordUtil.generateRandomString(7);
        String suffix = RandomPasswordUtil.generateRandomString(6);
        String code = prefix + token + suffix;

        String verifyLink = "http://localhost:5173/auth/register?code=" + code + "&email=" + request.getEmail();

        mailService.sendMail(
                request.getEmail(),
                "[PES] Email Verification",
                "Please verify your email to continue registration.",
                Format.getEmailVerificationBody(verifyLink, request.getEmail())
        );

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("A verification email has been sent to " + request.getEmail())
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> register(RegisterRequest request) {
        String error = registerValidation(request);
        if (!error.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String token = extractTokenFromCode(request.getCode());

        String email;
        try {
            if (!jwtService.checkIfNotExpired(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("The verification link has expired.")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            email = jwtService.extractEmailFromJWT(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseObject.builder()
                            .message("Invalid or tampered token.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (accountRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("This email has already been registered.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .address(request.getAddress())
                .identityNumber(request.getIdentityNumber())
                .role(Role.PARENT)
                .status(Status.ACCOUNT_ACTIVE.getValue())
                .build();

        accountRepo.save(account);

        parentRepo.save(Parent.builder()
                .account(account)
                .job(request.getJob())
                .relationshipToChild(request.getRelationshipToChild())
                .build());

        mailService.sendMail(
                account.getEmail(),
                "[PES] Registration Successful",
                "Welcome to PES!",
                Format.getRegistrationSuccessBody(account.getName())
        );

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Registration successful. Your account has been activated.")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private String extractTokenFromCode(String code) {
        if (code.contains("?code=")) {
            code = code.substring(code.indexOf("?code=") + 6);
        }
        return code.substring(7, code.length() - 6);
    }


    private String registerValidation(RegisterRequest request) {
        // 1. Name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes.";
        }
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters.";
        }

        // 2. Phone
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "Phone number is required.";
        }
        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid prefix and be 10 digits.";
        }

        // 3. Gender
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            return "Gender is required.";
        }
        if (!request.getGender().trim().equals("male") &&
                !request.getGender().trim().equals("female")) {
            return "Gender must be 'male', 'female'";
        }

        // 4. Identity Number
        if (request.getIdentityNumber() == null || request.getIdentityNumber().trim().isEmpty()) {
            return "Identity number is required.";
        }
        if (!Pattern.compile("^\\d{12}$").matcher(request.getIdentityNumber()).matches()) {
            return "Identity number must be exactly 12 digits.";
        }

        // 5. Job
        if (request.getJob() == null || request.getJob().trim().isEmpty()) {
            return "Job is required.";
        }
        if (request.getJob().trim().length() > 100) {
            return "Job must not exceed 100 characters.";
        }

        // 6. Relationship to Child
        if (request.getRelationshipToChild() == null || request.getRelationshipToChild().trim().isEmpty()) {
            return "Relationship to child is required.";
        }
        if (!request.getRelationshipToChild().matches("(?i)^(father|mother)$")) {
            return "Relationship to child must be one of the following: father, mother";
        }

        // 7. Password
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

        // 8. Confirm Password
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            return "Confirm password is required.";
        }
        if (!request.getConfirmPassword().equals(request.getPassword())) {
            return "Confirm password does not match password.";
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

        if (accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Account not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        String token = jwtService.generateVerifyToken(request.getEmail());

        String prefix = RandomPasswordUtil.generateRandomString(7);
        String suffix = RandomPasswordUtil.generateRandomString(6);
        String code = prefix + token + suffix;

        String resetLink = "http://localhost:5173/reset-pass?code=" + code;

        mailService.sendMail(
                request.getEmail(),
                "[PES] Password Reset Request",
                "Reset Your Password",
                Format.getForgotPasswordBody(resetLink)
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Reset code has been sent to your email")
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

        try {
            String code = request.getCode();
            if (code.contains("?code=")) {
                code = code.substring(code.indexOf("?code=") + 6);
            }
            String token = code.substring(7, code.length() - 6);

            String email = jwtService.extractEmailFromJWT(token);

            Account account = accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseObject.builder()
                                .message("Account not found")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

            account.setPassword(passwordEncoder.encode(request.getNewPassword()));
            accountRepo.save(account);

            mailService.sendMail(
                    account.getEmail(),
                    "[PES] Password Changed Successfully",
                    "Password Changed",
                    Format.getPasswordChangedFormat(account.getName())
            );

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .message("Password has been reset successfully")
                            .success(true)
                            .data(null)
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Invalid reset code format")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
    }

    private String resetPassValidation(ResetPassRequest request) {
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

    private String forgotPasswordValidation(ForgotPasswordRequest request, AccountRepo accountRepo) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null) {
            return "No active account found with this email.";
        }

        return "";
    }

}
