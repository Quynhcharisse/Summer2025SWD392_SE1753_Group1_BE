package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/education")
public class ClassController {
    private final ClassService classService;

    @PostMapping("/classes")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> generateClassesAuto(@RequestBody GenerateClassesRequest request){
        return classService.generateClassesAuto(request);
    }

    @DeleteMapping("/class")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> deleteClassById (@RequestParam String classId){
        return classService.deleteClassById(classId);
    }

    @GetMapping("/class/listByGradeAndYear")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAllClassesByYearAndGrade (@RequestParam String year, @RequestParam String grade){
        return classService.viewAllClassesByYearAndGrade(year, grade);
    }

    @GetMapping("/class/schedule/list")
    @PreAuthorize("hasAnyRole('education', 'parent', 'teacher')")
    public ResponseEntity<ResponseObject> getSchedulesByClassId(@RequestParam String classId){
        return classService.getSchedulesByClassId(classId);
    }

    @GetMapping("/schedule/activity/list")
    @PreAuthorize("hasAnyRole('education', 'parent', 'teacher')")
    public ResponseEntity<ResponseObject> getActivitiesByScheduleId(@RequestParam String scheduleId){
        return classService.getActivitiesByScheduleId(scheduleId);
    }

    @GetMapping("/numberOfAvailableStudents")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(@RequestParam String year, @RequestParam String grade){
        return classService.viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(year, grade);
    }

    @GetMapping("/assignedStudentOfClass/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedStudentsOfClass(@RequestParam String classId){
        return classService.viewAssignedStudentsOfClass(classId);
    }

    @GetMapping("/student/export")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<Resource> exportStudentListToExcel() {
        return classService.exportStudentListToExcel(); }

}
