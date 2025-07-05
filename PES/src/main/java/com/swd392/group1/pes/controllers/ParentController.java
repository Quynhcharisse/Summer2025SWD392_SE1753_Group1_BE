package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.AddChildRequest;
import com.swd392.group1.pes.dto.requests.CancelAdmissionForm;
import com.swd392.group1.pes.dto.requests.GetPaymentURLRequest;
import com.swd392.group1.pes.dto.requests.InitiateVNPayPaymentRequest;
import com.swd392.group1.pes.dto.requests.RefillFormRequest;
import com.swd392.group1.pes.dto.requests.RegisterEventRequest;
import com.swd392.group1.pes.dto.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateChildRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.ParentService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("api/v1/parent")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/form/list")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request) {
        return parentService.viewAdmissionFormList(request);
    }

    @PostMapping("/form/submit")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> submitAdmissionForm(@RequestBody SubmitAdmissionFormRequest request, HttpServletRequest httpRequest) {
        return parentService.submitAdmissionForm(request, httpRequest);
    }

    @PostMapping("/form/refill")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> refillForm(@RequestBody RefillFormRequest request, HttpServletRequest httpRequest) {
        return parentService.refillForm(request, httpRequest);
    }

    @PutMapping("/form/cancel")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> cancelAdmissionForm(@RequestBody CancelAdmissionForm request, HttpServletRequest httpRequest) {
        return parentService.cancelAdmissionForm(request, httpRequest);
    }

    @GetMapping("/child")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> viewChild(HttpServletRequest request) {
        return parentService.viewChild(request);
    }

    @PostMapping("/child")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> addChild(@RequestBody AddChildRequest request, HttpServletRequest httpRequest) {
        return parentService.addChild(request, httpRequest);
    }

    @PutMapping("/child")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> updateChild(@RequestBody UpdateChildRequest request, HttpServletRequest httpRequest) {
        return parentService.updateChild(request, httpRequest);
    }


    @PostMapping("/event/register")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> registerEvent(@RequestBody RegisterEventRequest request, HttpServletRequest requestHttp) {
        return parentService.registerEvent(request, requestHttp);
    }

    @GetMapping("/event/register")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> getRegisteredEvents(HttpServletRequest request) {
        return parentService.getRegisteredEvents(request);
    }

    @PostMapping("/payment")
    @PreAuthorize("hasRole('parent')")
    public ResponseEntity<ResponseObject> getPaymentURL(@RequestBody GetPaymentURLRequest request, HttpServletRequest httpRequest) {
        return parentService.getPaymentURL(request, httpRequest);
    }

    @PostMapping("/payment/initiate")
    @PreAuthorize("hasRole('parent')") // Thêm dòng này nếu bạn có Spring Security và muốn giới hạn quyền truy cập
    public ResponseEntity<ResponseObject> initiateVNPayPayment(@RequestBody InitiateVNPayPaymentRequest request, HttpServletRequest httpRequest) {
        return parentService.initiateVNPayPayment(request, httpRequest);
    }
}
