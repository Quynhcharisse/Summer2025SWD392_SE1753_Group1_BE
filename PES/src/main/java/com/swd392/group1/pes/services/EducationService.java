package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface EducationService {
    ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest createSyllabusRequest);
}
