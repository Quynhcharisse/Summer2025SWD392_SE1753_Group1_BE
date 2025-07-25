package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.dto.requests.CreateExtraTermRequest;
import com.swd392.group1.pes.dto.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.dto.requests.DailyTotalTransactionRequest;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.services.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/admission")
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> createAdmissionTerm(@RequestBody CreateAdmissionTermRequest request) {
        return admissionService.createAdmissionTerm(request);
    }

    @GetMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {
        return admissionService.viewAdmissionTerm();
    }

    @PutMapping("/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> updateTermStatus(@RequestBody UpdateAdmissionTermRequest request) {
        return admissionService.updateTermStatus(request);
    }

    @GetMapping("/default/fee")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> getDefaultFeeByGrade(@RequestParam Grade grade) {
        return admissionService.getDefaultFeeByGrade(grade);
    }

    @PostMapping("/extra/term")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> createExtraTerm(@RequestBody CreateExtraTermRequest request) {
        return admissionService.createExtraTerm(request);
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

    @GetMapping("/years")
    @PreAuthorize("hasAnyRole('admission','education')")
    public ResponseEntity<ResponseObject> getAllYear() {
        return admissionService.getAllYear();
    }

    @GetMapping("/forms/status/summary")
    @PreAuthorize("hasRole('admission')")
    public Map<String, Long> getAdmissionFormStatusSummary() {
        return admissionService.getAdmissionFormStatusSummary();
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ResponseObject> getTransactionList() {
        return admissionService.getTransactionList();
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('admission')")
    public ResponseEntity<ByteArrayResource> exportTransactionsToExcel() {
        return admissionService.exportTransactionsToExcel();
    }

    @PostMapping("/daily/total/transaction")
    public ResponseEntity<ResponseObject> getDailyTotal(@RequestBody DailyTotalTransactionRequest request) {
        return admissionService.getDailyTotal(request);
    }

}
