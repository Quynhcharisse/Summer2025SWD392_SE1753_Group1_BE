package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/education")

public class EducationController {

    private final EducationService educationService;

    @PostMapping("/syllabus")
//    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createSyllabus(@RequestBody CreateSyllabusRequest request) {
        return educationService.createSyllabus(request);
    }

    @PutMapping("/syllabus")
    public ResponseEntity<ResponseObject> updateSyllabus(@RequestParam String id, @RequestBody UpdateSyllabusRequest request) {
        return educationService.updateSyllabus(id, request);
    }

    @GetMapping("/syllabus/detail")
    public ResponseEntity<ResponseObject> viewSyllabusDetail(@RequestParam String id) {
        return educationService.viewSyllabusDetail(id);
    }

}
