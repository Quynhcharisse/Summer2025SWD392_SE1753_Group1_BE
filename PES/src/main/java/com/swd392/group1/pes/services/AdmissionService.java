package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.*;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AdmissionService {
    ResponseEntity<ResponseObject> createAdmissionTerm(CreateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> updateTermStatus(UpdateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionTerm();

    ResponseEntity<ResponseObject> createExtraTerm(CreateExtraTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionFormList();

    ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade);

    ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request);

    ResponseEntity<ResponseObject> getAllYear();
}
