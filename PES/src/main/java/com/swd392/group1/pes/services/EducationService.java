package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface EducationService {
    ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request);
    ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request);
    ResponseEntity<ResponseObject> viewSyllabusDetail(String id);
    ResponseEntity<ResponseObject> viewAllSyllabus();
    ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request);
    ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request);
    ResponseEntity<ResponseObject> updateLesson(String id, UpdateLessonRequest request);
    ResponseEntity<ResponseObject> viewLessonList();
    ResponseEntity<ResponseObject> deleteLesson(String id);
    ResponseEntity<ResponseObject> createEvent(CreateEventRequest request);
    ResponseEntity<ResponseObject> updateEvent(String id, UpdateEventRequest request);
    ResponseEntity<ResponseObject> viewEventList();
    ResponseEntity<ResponseObject> viewEventDetail(String id);
}
