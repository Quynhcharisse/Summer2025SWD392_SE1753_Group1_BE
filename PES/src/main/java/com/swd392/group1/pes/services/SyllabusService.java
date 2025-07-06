package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.AssignLessonsRequest;
import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SyllabusService {
    ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request);
    ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request);
    ResponseEntity<ResponseObject> viewSyllabusDetail(String id);
    ResponseEntity<ResponseObject> viewAllSyllabus();

    ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(String id, String searchQuery);
    ResponseEntity<ResponseObject> assignLessonsToSyllabus(String id, AssignLessonsRequest request);
    ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(String id, String searchQuery);
    ResponseEntity<ResponseObject> viewAllSyllabusesByGrade(String gradeName);
}
