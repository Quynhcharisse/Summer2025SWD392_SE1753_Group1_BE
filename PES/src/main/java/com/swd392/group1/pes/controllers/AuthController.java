package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.dto.requests.LoginRequest;
import com.swd392.group1.pes.dto.requests.OtpVerifyRequest;
import com.swd392.group1.pes.dto.requests.RegisterRequest;
import com.swd392.group1.pes.dto.requests.ResetPassRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.AuthService;
import com.swd392.group1.pes.services.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    private final EventService eventService;

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

    @PostMapping("/register/otp/verify")
    public ResponseEntity<ResponseObject> registerVerifyEmail(@RequestBody OtpVerifyRequest request) {
        return authService.registerVerifyEmail(request);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ResponseObject> forgotPassword (@RequestBody ForgotPasswordRequest request){
        return authService.forgotPassword(request);
    }

    @PostMapping("/password/forgot/reset")
    public ResponseEntity<ResponseObject> resetPass (@RequestBody ResetPassRequest request){
        return authService.resetPass (request);
    }

    @GetMapping("/event/active")
    public ResponseEntity<ResponseObject> viewActiveEvents() {
        return eventService.viewActiveEvents();
    }

    @GetMapping("/event/detail")
    public ResponseEntity<ResponseObject> viewEventDetail(@RequestParam String id) {
        return eventService.viewEventDetail(id);
    }

}
