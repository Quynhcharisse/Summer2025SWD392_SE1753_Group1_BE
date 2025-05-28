package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/educationManager")
public class EducationManagerController {

    private final EducationManagerService educationManagerService;

    @PostMapping("/createSyllabus")
    public ResponseEntity<ResponseObject> createSyllabus(@RequestBody CreateSyllabusRequest createSyllabusRequest) {
        return educationManagerService.createSyllabus(createSyllabusRequest);
    }

}
