package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionFeeRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AdmissionService {
    ResponseEntity<ResponseObject> createAdmissionTerm(AdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionTerm();

    ResponseEntity<ResponseObject> updateAdmissionTerm(AdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionFormList();

    ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request);

    ResponseEntity<ResponseObject> updateAdmissionFee(UpdateAdmissionFeeRequest request);

    ResponseEntity<ResponseObject> viewAdmissionFee(int term);

}
