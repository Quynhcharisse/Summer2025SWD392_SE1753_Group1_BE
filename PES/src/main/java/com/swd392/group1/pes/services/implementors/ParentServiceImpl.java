package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.requests.SaveDraftAdmissionFormRequest;
import com.swd392.group1.pes.requests.SubmitAdmissionFormRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.ParentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    @Override
    public ResponseEntity<ResponseObject> saveDraftAdmissionForm(SaveDraftAdmissionFormRequest request, HttpServletRequest httpRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionForm(int id, HttpServletRequest httpRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> submitAdmissionForm(SubmitAdmissionFormRequest request, HttpServletRequest httpRequest) {
        return null;
    }
}
