package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.CancelEventRequest;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
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
    ResponseEntity<ResponseObject> deleteClassById(String id);
    ResponseEntity<ResponseObject> viewAllClassesByYearAndGrade(String year, String grade);
    ResponseEntity<ResponseObject> getSchedulesByClassId(String classId);
    ResponseEntity<ResponseObject> getActivitiesByScheduleId(String scheduleId);
    ResponseEntity<ResponseObject> assignLessonsToSyllabus(String id, AssignLessonsRequest request);
    ResponseEntity<ResponseObject> viewAssignedSyllabuses(String id);
    ResponseEntity<ResponseObject> viewLessonDetail(String id);
    ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request);
    ResponseEntity<ResponseObject> updateLesson(String id, UpdateLessonRequest request);
    ResponseEntity<ResponseObject> viewLessonList(String searchQuery);
    ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(String id, String searchQuery);
    ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(String id, String searchQuery);
    ResponseEntity<ResponseObject> createEvent(CreateEventRequest request);
    ResponseEntity<ResponseObject> cancelEvent(String id, CancelEventRequest request);
    ResponseEntity<ResponseObject> viewEventList();
    ResponseEntity<ResponseObject> viewEventDetail(String id);
    ResponseEntity<ResponseObject> viewAssignedTeachersOfEvent(String id);
    ResponseEntity<ResponseObject> viewActiveEvents();
    ResponseEntity<ResponseObject> viewAllSyllabusesByGrade(String gradeName);
    ResponseEntity<ResponseObject> viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(String year, String grade);
    ResponseEntity<ResponseObject> assignActivitiesToSchedule(String scheduleId);
}
