package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface EducationService {
    ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request);
    ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request);
    ResponseEntity<ResponseObject> viewSyllabusDetail(String id);
    ResponseEntity<ResponseObject> viewAllSyllabus();
    ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request);
}
