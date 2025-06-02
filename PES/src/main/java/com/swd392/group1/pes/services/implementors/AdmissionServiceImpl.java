package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.requests.AdmissionTermRequest;
import com.swd392.group1.pes.requests.CreateAdmissionFeeRequest;
import com.swd392.group1.pes.requests.ProcessAdmissionFormRequest;
import com.swd392.group1.pes.requests.UpdateAdmissionFeeRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.AdmissionService;
import com.swd392.group1.pes.validations.AdmissionValidation.CreateTermValidation;
import com.swd392.group1.pes.validations.AdmissionValidation.ProcessAdmissionFormValidation;
import com.swd392.group1.pes.validations.AdmissionValidation.UpdateTermValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionTermRepo admissionTermRepo;

    private final AdmissionFormRepo admissionFormRepo;

    //------------------------------\\ create admission term //------------------------------\\

    @Override
    public ResponseEntity<ResponseObject> createAdmissionTerm(AdmissionTermRequest request) {

        String error = CreateTermValidation.validate(request);

        if (!error.isEmpty()) {
            ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        AdmissionTerm term = admissionTermRepo.save(AdmissionTerm.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .year(request.getYear())
                .maxNumberRegistration(request.getMaxNumberRegistration())
                .grade(Grade.valueOf(request.getGrade().toUpperCase()))
                .status(Status.INACTIVE_TERM.getValue())
                .build());

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Create admission term successfully")
                        .success(true)
                        .data(term)
                        .build()
        );
    }

    //------------------------------\\ view admission term //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> viewAdmissionTerm() {
        return null;
    }


    //------------------------------\\ update admission term //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> updateAdmissionTerm(AdmissionTermRequest request) {

        String error = UpdateTermValidation.validate(request);

        if (!error.isEmpty()) {
            ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }


        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    //------------------------------\\ view admission form list //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFormList(int year) {
        return null;
    }

    //------------------------------\\ process admission form //------------------------------\\
    @Override
    public ResponseEntity<ResponseObject> processAdmissionFormList(ProcessAdmissionFormRequest request) {

        String error = ProcessAdmissionFormValidation.validate(request);

        if (!error.isEmpty()) {
            ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }


        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    //----------------------------- Configuration Admission Fee -------------------------------//
    @Override
    public ResponseEntity<ResponseObject> createAdmissionFee(CreateAdmissionFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateAdmissionFee(UpdateAdmissionFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionFee(int term) {
        return null;
    }
}
