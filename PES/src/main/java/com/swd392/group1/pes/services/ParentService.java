package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.SaveDraftAdmissionFormRequest;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ParentService {
    ResponseEntity<ResponseObject> saveDraftAdmissionForm(SaveDraftAdmissionFormRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request);

    ResponseEntity<ResponseObject> cancelAdmissionForm(int id, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest);
}
