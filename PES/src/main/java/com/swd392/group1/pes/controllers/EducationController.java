package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.UnassignLessonsRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
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
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createSyllabus(@RequestBody CreateSyllabusRequest request) {
        return educationService.createSyllabus(request);
    }

    @PutMapping("/syllabus")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateSyllabus(@RequestParam String id, @RequestBody UpdateSyllabusRequest request) {
        return educationService.updateSyllabus(id, request);
    }

    @GetMapping("/syllabus/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewSyllabusDetail(@RequestParam String id) {
        return educationService.viewSyllabusDetail(id);
    }

    @GetMapping("/syllabus/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewSyllabusList() {
        return educationService.viewAllSyllabus();
    }

    @PostMapping("/classes")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request){
        return educationService.generateClassesAuto(request);
    }

    @PutMapping("/syllabus/assign-lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> assignLessons(@RequestParam String id, @RequestBody AssignLessonsRequest request){
        return educationService.assignLessonsToSyllabus(id, request);
    }

    @PutMapping("/syllabus/unassign-lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> unassignLessons(@RequestParam String id, @RequestBody UnassignLessonsRequest request){
        return educationService.unassignLessonsFromSyllabus(id, request);
    }

    @PostMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createLesson(@RequestBody CreateLessonRequest request) {
        return educationService.createLesson(request);
    }

    @PutMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateLesson(@RequestParam String id, @RequestBody UpdateLessonRequest request) {
        return educationService.updateLesson(id, request);
    }

    @GetMapping("/lesson/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonList() {
        return educationService.viewLessonList();
    }


    @PostMapping("/event")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createEvent(@RequestBody CreateEventRequest request) {
        return educationService.createEvent(request);
    }

    @PutMapping("/event")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateEvent(@RequestParam String id, @RequestBody UpdateEventRequest request) {
        return educationService.updateEvent(id, request);
    }

    @GetMapping("/event/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewEventList() {
        return educationService.viewEventList();
    }

    @GetMapping("/event/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewEventDetail(@RequestParam String id) {
        return educationService.viewEventDetail(id);
    }
}
