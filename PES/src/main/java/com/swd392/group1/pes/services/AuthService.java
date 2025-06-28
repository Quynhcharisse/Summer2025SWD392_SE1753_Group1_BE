package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.requests.LoginRequest;
import com.swd392.group1.pes.requests.RegisterRequest;
import com.swd392.group1.pes.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ResponseObject> login  (LoginRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> logout(HttpServletResponse response);

    ResponseEntity<ResponseObject> refresh  (HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> register (RegisterRequest request);

    ResponseEntity<ResponseObject> forgotPassword (ForgotPasswordRequest request);

    ResponseEntity<ResponseObject> sendRegisterOtp(String email);

    ResponseEntity<ResponseObject> verifyRegisterOtp(String email, String otp);

}
