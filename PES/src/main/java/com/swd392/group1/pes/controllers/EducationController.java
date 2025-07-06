package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.AssignLessonsRequest;
import com.swd392.group1.pes.dto.requests.CancelEventRequest;
import com.swd392.group1.pes.dto.requests.CreateEventRequest;
import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/syllabus/listByGrade")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAllSyllabusesByGrade(@RequestParam String gradeName) {
        return educationService.viewAllSyllabusesByGrade(gradeName);
    }


    @PostMapping("/classes")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> generateClassesAuto(@RequestBody GenerateClassesRequest request){
        return educationService.generateClassesAuto(request);
    }

    @DeleteMapping("/class")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> deleteClassById (@RequestParam String classId){
        return educationService.deleteClassById(classId);
    }

    @GetMapping("/class/listByGradeAndYear")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAllClassesByYearAndGrade (@RequestParam String year, @RequestParam String grade){
        return educationService.viewAllClassesByYearAndGrade(year, grade);
    }

    @GetMapping("/class/schedule/list")
    @PreAuthorize("hasAnyRole('education', 'parent', 'teacher')")
    public ResponseEntity<ResponseObject> getSchedulesByClassId(@RequestParam String classId){
        return educationService.getSchedulesByClassId(classId);
    }

    @GetMapping("/schedule/activity/list")
    @PreAuthorize("hasAnyRole('education', 'parent', 'teacher')")
    public ResponseEntity<ResponseObject> getActivitiesByScheduleId(@RequestParam String scheduleId){
        return educationService.getActivitiesByScheduleId(scheduleId);
    }

    @PutMapping("/syllabus/assign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> assignLessons(@RequestParam String id, @RequestBody AssignLessonsRequest request) {
        return educationService.assignLessonsToSyllabus(id, request);
    }

    @GetMapping("/syllabus/unassign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(@RequestParam String id, @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return educationService.viewLessonNotAssignedOfSyllabus(id, searchQuery);
    }

    @GetMapping("/syllabus/assign/lessons")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(@RequestParam String id, @RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return educationService.viewLessonAssignedOfSyllabus(id, searchQuery);
    }

    @PostMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createLesson(@RequestBody CreateLessonRequest request) {
        return educationService.createLesson(request);
    }

    @GetMapping("/lesson/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonDetail(@RequestParam String id) {
        return educationService.viewLessonDetail(id);
    }

    @GetMapping("/lesson/assign/syllabuses")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedSyllabuses(@RequestParam String id) {
        return educationService.viewAssignedSyllabuses(id);
    }

    @PutMapping("/lesson")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> updateLesson(@RequestParam String id, @RequestBody UpdateLessonRequest request) {
        return educationService.updateLesson(id, request);
    }

    @GetMapping("/lesson/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewLessonList(@RequestParam(value = "searchQuery", required = false) String searchQuery) {
        return educationService.viewLessonList(searchQuery);
    }


    @PostMapping("/event")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createEvent(@RequestBody CreateEventRequest request) {
        return educationService.createEvent(request);
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

    @PutMapping("/event/cancel")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> cancelEvent(@RequestParam String id, @RequestBody CancelEventRequest cancelEventRequest) {
        return educationService.cancelEvent(id, cancelEventRequest);
    }

    @GetMapping("/event/assign/teachers")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedTeachersOfEvent(@RequestParam String id) {
        return educationService.viewAssignedTeachersOfEvent(id);
    }

    @GetMapping("/numberOfAvailableStudents")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(@RequestParam String year, @RequestParam String grade){
        return educationService.viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(year, grade);
    }

    @GetMapping("/assignedStudentOfClass/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedStudentsOfClass(@RequestParam String classId){
        return educationService.viewAssignedStudentsOfClass(classId);
    }
    @GetMapping("/student/export")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<Resource> exportStudentListToExcel() { return educationService.exportStudentListToExcel(); }
}
