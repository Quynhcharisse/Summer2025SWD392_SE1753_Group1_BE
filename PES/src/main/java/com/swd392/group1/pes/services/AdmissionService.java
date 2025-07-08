package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.CreateAdmissionTermRequest;
import com.swd392.group1.pes.dto.requests.CreateExtraTermRequest;
import com.swd392.group1.pes.dto.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.dto.requests.UpdateAdmissionTermRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.dto.requests.DailyTotalTransactionRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AdmissionService {
    ResponseEntity<ResponseObject> createAdmissionTerm(CreateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> updateTermStatus(UpdateAdmissionTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionTerm();

    ResponseEntity<ResponseObject> createExtraTerm(CreateExtraTermRequest request);

    ResponseEntity<ResponseObject> viewAdmissionFormList();

    ResponseEntity<ResponseObject> getDefaultFeeByGrade(String grade);

    ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request);

    ResponseEntity<ResponseObject> getAllYear();

    Map<String, Long> getAdmissionFormStatusSummary();

    ResponseEntity<ByteArrayResource> exportTransactionsToExcel();

    ResponseEntity<ResponseObject> getTransactionList();

    ResponseEntity<ResponseObject> getDailyTotal(DailyTotalTransactionRequest request);
}
