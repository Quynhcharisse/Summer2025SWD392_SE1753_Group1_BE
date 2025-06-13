package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AdmissionService {
    ResponseEntity<ResponseObject> createAdmissionTerm(CreateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionTerm();

    ResponseEntity<ResponseObject> updateAdmissionTerm(UpdateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionFormList();

    ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade);

    ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request);
}
