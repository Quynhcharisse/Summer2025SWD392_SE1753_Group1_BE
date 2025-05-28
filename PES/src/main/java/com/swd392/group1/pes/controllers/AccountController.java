package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.requests.RenewPasswordRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/changePassword")
    public ResponseEntity<ResponseObject> changePassword (@RequestBody RenewPasswordRequest request){
        return accountService.renewPassword(request);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseObject> viewProfile (){
        return accountService.viewProfile();
    }

    @PutMapping("/ban")
    public ResponseEntity<ResponseObject> banAccount(@RequestBody ProcessAccountRequest request){
        return accountService.processAccount(request);
    }

    @PutMapping("/unban")
    public ResponseEntity<ResponseObject> unbanAccount(@RequestBody ProcessAccountRequest request){
        return accountService.processAccount(request);
    }

}
