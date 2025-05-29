package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {

    @Override
    public ResponseEntity<ResponseObject> createAdmissionTerm(AdmissionTermRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateAdmissionTerm(AdmissionTermRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(int year) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request) {
        return null;
    }
}
