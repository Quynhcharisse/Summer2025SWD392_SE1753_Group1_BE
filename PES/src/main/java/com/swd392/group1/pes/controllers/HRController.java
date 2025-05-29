package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.HRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/hr")
public class HRController {

    private final HRService hrService;

    @PutMapping("/ban")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> banAccount(@RequestBody ProcessAccountRequest request) {
        return hrService.processAccount(request, "ban");
    }

    @PutMapping("/unban")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> unbanAccount(@RequestBody ProcessAccountRequest request) {
        return hrService.processAccount(request, "unban");
    }
}
