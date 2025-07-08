package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ClassService {
    ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request);
    ResponseEntity<ResponseObject> deleteClassById(String id);
    ResponseEntity<ResponseObject> viewAllClassesByYearAndGrade(String year, String grade);
    ResponseEntity<ResponseObject> getSchedulesByClassId(String classId);
    ResponseEntity<ResponseObject> getActivitiesByScheduleId(String scheduleId);
    ResponseEntity<ResponseObject> viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(String year, String grade);
    ResponseEntity<ResponseObject> viewListOfStudentsNotAssignedToAnyClassByYearAndGrade(String year, String grade, int page, int size);
    ResponseEntity<ResponseObject> viewAssignedStudentsOfClass(String classId);
    ResponseEntity<Resource> exportStudentListToExcel();
    ResponseEntity<ResponseObject> assignAvailableStudentsAuto(String year, String grade);
    ResponseEntity<ResponseObject> viewListClassesOfChild(String childId);
}
