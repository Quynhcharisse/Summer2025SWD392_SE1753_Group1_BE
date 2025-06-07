package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.RestPasswordRequest;
import com.swd392.group1.pes.requests.UpdateProfileRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/pass/reset")
    @PreAuthorize("hasAnyRole('parent', 'hr', 'admission', 'education', 'teacher')")
    public ResponseEntity<ResponseObject> resetPassword (@RequestBody RestPasswordRequest request){
        return accountService.resetPassword(request);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('parent', 'hr', 'admission', 'education', 'teacher')")
    public ResponseEntity<ResponseObject> viewProfile (@RequestParam String email){
        return accountService.viewProfile(email);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('parent', 'hr', 'admission', 'education', 'teacher')")
    public ResponseEntity<ResponseObject> updateProfile (@RequestBody UpdateProfileRequest request){
        return accountService.updateProfile(request);
    }
}
