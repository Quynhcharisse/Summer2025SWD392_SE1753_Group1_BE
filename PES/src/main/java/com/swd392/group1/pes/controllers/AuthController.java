package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.requests.LoginRequest;
import com.swd392.group1.pes.requests.RegisterRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login (@RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseObject> logout ( HttpServletResponse response) {
        return authService.logout(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject> refresh (HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register (@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ResponseObject> forgotPassword (@RequestBody ForgotPasswordRequest request){
        return authService.forgotPassword(request);
    }
}
