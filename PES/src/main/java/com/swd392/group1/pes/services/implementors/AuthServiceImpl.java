package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.requests.LoginRequest;
import com.swd392.group1.pes.requests.RegisterRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AuthService;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.utils.CookieUtil;
import com.swd392.group1.pes.validations.AuthValidation.LoginValidation;
import com.swd392.group1.pes.validations.AuthValidation.RegisterValidation;
import com.swd392.group1.pes.validations.AuthValidation.ForgotPasswordValidation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${security-access-expiration}")
    private long accessExpiration;

    @Value("${security-refresh-expiration}")
    private long refreshExpiration;

    private final AccountRepo accountRepo;

    private final JWTService jwtService;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepo.findByEmailAndPassword(request.getEmail(), request.getPassword()).orElse(null);

        assert account != null;

        String error = LoginValidation.validate(request, accountRepo);

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

    private Map<String, Object> buildLoginBody (Account account) {
        Map <String, Object> body = new HashMap<>();
        body.put("email", account.getEmail());
        body.put("role", account.getRole().name());
        return body;
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

            if(account != null) {
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
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ResponseObject.builder()
                        .message("Refresh invalid")
                        .success(false)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> register(RegisterRequest request) {

        String error = RegisterValidation.validate(request, accountRepo);
        if(!error.isEmpty()){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
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
        account.setCreatedAt(LocalDate.now());

        accountRepo.save(account);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Register Successfully")
                        .success(true)
                        .data(account)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> forgotPassword(ForgotPasswordRequest request) {
        String error = ForgotPasswordValidation.validate(request, accountRepo);
        if(!error.isEmpty()){
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
        account.setPassword(request.getPassword());
        accountRepo.save(account);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("Change Password Successfully")
                        .success(true)
                        .data(account)
                        .build()
        );
    }
}
