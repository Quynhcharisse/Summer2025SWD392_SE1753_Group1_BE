package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.AddChildRequest;
import com.swd392.group1.pes.dto.requests.CancelAdmissionForm;
import com.swd392.group1.pes.dto.requests.GetPaymentURLRequest;
import com.swd392.group1.pes.dto.requests.InitiateVNPayPaymentRequest;
import com.swd392.group1.pes.dto.requests.RefillFormRequest;
import com.swd392.group1.pes.dto.requests.RegisterEventRequest;
import com.swd392.group1.pes.dto.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateChildRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ParentService {
    ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request);

    ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> refillForm(RefillFormRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> cancelAdmissionForm(CancelAdmissionForm request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> viewChild(HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> addChild(AddChildRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> updateChild(UpdateChildRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> registerEvent(RegisterEventRequest request, HttpServletRequest requestHttp);

    ResponseEntity<ResponseObject> getRegisteredEvents(HttpServletRequest request);

    ResponseEntity<ResponseObject> getPaymentURL(GetPaymentURLRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> initiateVNPayPayment (InitiateVNPayPaymentRequest request, HttpServletRequest httpRequest);
}
