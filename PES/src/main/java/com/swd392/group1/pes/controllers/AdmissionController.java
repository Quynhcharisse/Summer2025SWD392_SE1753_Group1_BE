package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/admission")
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> createAdmissionTerm(@RequestBody AdmissionTermRequest request) {
        return admissionService.createAdmissionTerm(request);
    }

    @PutMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> updateAdmissionTerm(@RequestBody AdmissionTermRequest request) {
        return admissionService.updateAdmissionTerm(request);
    }

    @GetMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {
        return admissionService.viewAdmissionTerm();
    }

    @GetMapping("/form/list")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> viewAdmissionFormList() {
        return admissionService.viewAdmissionFormList();
    }

    @PutMapping("/form/process")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> processAdmissionFormList(@RequestBody ProcessAdmissionFormRequest request) {
        return admissionService.processAdmissionFormList(request);
    }
}
