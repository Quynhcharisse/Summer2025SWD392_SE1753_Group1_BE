package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.LessonService;
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
public class LessonController {
    private final LessonService lessonService;

    @PostMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createLesson(@RequestBody CreateLessonRequest request) {
        return lessonService.createLesson(request);
    }

    @GetMapping("/lesson/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonDetail(@RequestParam String id) {
        return lessonService.viewLessonDetail(id);
    }

    @GetMapping("/lesson/assign/syllabuses")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedSyllabuses(@RequestParam String id) {
        return lessonService.viewAssignedSyllabuses(id);
    }

    @PutMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateLesson(@RequestParam String id, @RequestBody UpdateLessonRequest request) {
        return lessonService.updateLesson(id, request);
    }

    @GetMapping("/lesson/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonList(@RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return lessonService.viewLessonList(searchQuery);
    }
}
