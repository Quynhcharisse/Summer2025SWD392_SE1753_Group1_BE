package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.AssignLessonsRequest;
import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.SyllabusService;
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
public class SyllabusController {
    private final SyllabusService syllabusService;

    @PostMapping("/syllabus")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createSyllabus(@RequestBody CreateSyllabusRequest request) {
        return syllabusService.createSyllabus(request);
    }

    @PutMapping("/syllabus")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateSyllabus(@RequestParam String id, @RequestBody UpdateSyllabusRequest request) {
        return syllabusService.updateSyllabus(id, request);
    }

    @GetMapping("/syllabus/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewSyllabusDetail(@RequestParam String id) {
        return syllabusService.viewSyllabusDetail(id);
    }

    @GetMapping("/syllabus/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewSyllabusList() {
        return syllabusService.viewAllSyllabus();
    }

    @GetMapping("/syllabus/listByGrade")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAllSyllabusesByGrade(@RequestParam String gradeName) {
        return syllabusService.viewAllSyllabusesByGrade(gradeName);
    }

    @PutMapping("/syllabus/assign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> assignLessons(@RequestParam String id, @RequestBody AssignLessonsRequest request) {
        return syllabusService.assignLessonsToSyllabus(id, request);
    }

    @GetMapping("/syllabus/unassign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(@RequestParam String id, @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return syllabusService.viewLessonNotAssignedOfSyllabus(id, searchQuery);
    }

    @GetMapping("/syllabus/assign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(@RequestParam String id, @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return syllabusService.viewLessonAssignedOfSyllabus(id, searchQuery);
    }

}
