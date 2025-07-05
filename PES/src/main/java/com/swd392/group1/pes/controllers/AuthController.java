package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.ForgotPasswordRequest;
import com.swd392.group1.pes.requests.LoginRequest;
import com.swd392.group1.pes.requests.RegisterRequest;
import com.swd392.group1.pes.requests.ResetPassRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AuthService;
import com.swd392.group1.pes.services.EducationService;
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

    private final EducationService educationService;

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

    @PostMapping("/password/forgot")
    public ResponseEntity<ResponseObject> forgotPassword (@RequestBody ForgotPasswordRequest request){
        return authService.forgotPassword(request);
    }

    @GetMapping("/password/forgot/verify")
    public ResponseEntity<ResponseObject> verifyCode (@RequestParam("code") String code){
        return authService.verifyCode (code);
    }

    @PostMapping("/password/forgot/reset")
    public ResponseEntity<ResponseObject> resetPass (@RequestBody ResetPassRequest request){
        return authService.resetPass (request);
    }

    @GetMapping("/event/active")
    public ResponseEntity<ResponseObject> viewActiveEvents() {
        return educationService.viewActiveEvents();
    }

    @GetMapping("/event/detail")
    public ResponseEntity<ResponseObject> viewEventDetail(@RequestParam String id) {
        return educationService.viewEventDetail(id);
    }

    @PostMapping("/register/otp/send")
    public ResponseEntity<ResponseObject> sendRegisterOtp(@RequestParam String email) {
        return authService.sendRegisterOtp(email);
    }

    @PostMapping("/register/otp/verify")
    public ResponseEntity<ResponseObject> verifyRegisterOtp(@RequestParam String email, @RequestParam String otp) {
        return authService.verifyRegisterOtp(email, otp);
    }

}
